package org.tribetalk.core.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log
import java.util.ArrayDeque

/**
 * Immutable measurement of what the memory governor needs to make decisions.
 *
 * All values in MB. [appFootprintMb] is the number that actually matters on a
 * 2 GB tablet: native PSS (ONNX sessions, arenas, model weights) plus the Java
 * heap actually committed by the app process.
 */
data class MemorySnapshot(
    val nativePssMb: Int,
    val javaHeapUsedMb: Int,
    val javaHeapMaxMb: Int,
    val systemAvailMb: Long,
    val systemThresholdMb: Long,
    val systemTotalMb: Long = 2048L,
    val lowMemory: Boolean,
    val timestampMs: Long
) {
    /** Total RAM attributed to this app process (native + Java). */
    val appFootprintMb: Int
        get() = nativePssMb + javaHeapUsedMb

    /**
     * How much the system can take from us before LMK starts killing apps.
     * Negative means we are already inside the kill zone.
     */
    val systemHeadroomMb: Long
        get() = systemAvailMb - systemThresholdMb

    val systemUsedMb: Long
        get() = (systemTotalMb - systemAvailMb).coerceAtLeast(0L)

    val systemUsedPercent: Int
        get() = if (systemTotalMb > 0) ((systemUsedMb * 100) / systemTotalMb).toInt() else 0

    override fun toString(): String =
        "Memory(native=${nativePssMb}MB, java=$javaHeapUsedMb/$javaHeapMaxMb MB, " +
            "sysAvail=${systemAvailMb}/${systemTotalMb}MB, threshold=${systemThresholdMb}MB, " +
            "headroom=${systemHeadroomMb}MB, low=$lowMemory)"
}

/**
 * Abstraction over the platform memory sources so the policy logic in
 * [MemoryGovernor] is unit-testable without an Android device.
 */
interface MemoryProbe {
    /** Returns null when measurement is temporarily unavailable. */
    fun snapshot(): MemorySnapshot?
}

/** Production probe backed by Debug.MemoryInfo and ActivityManager.MemoryInfo. */
class AndroidMemoryProbe(private val context: Context) : MemoryProbe {

    override fun snapshot(): MemorySnapshot? {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return null

            val debugInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(debugInfo)
            val nativePss = debugInfo.nativePss / 1024  // kB -> MB

            val runtime = Runtime.getRuntime()
            val javaUsed = ((runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L)).toInt()
            val javaMax = (runtime.maxMemory() / (1024L * 1024L)).toInt()

            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            val totalMemMb = if (memInfo.totalMem > 0) memInfo.totalMem / (1024L * 1024L) else 2048L

            MemorySnapshot(
                nativePssMb = nativePss,
                javaHeapUsedMb = javaUsed,
                javaHeapMaxMb = javaMax,
                systemAvailMb = memInfo.availMem / (1024L * 1024L),
                systemThresholdMb = memInfo.threshold / (1024L * 1024L),
                systemTotalMb = totalMemMb,
                lowMemory = memInfo.lowMemory,
                timestampMs = System.currentTimeMillis()
            )
        } catch (t: Throwable) {
            null
        }
    }
}

/** Heavy pipeline stages with worst-case resident RAM estimates (MB). */
enum class GovernorStage(val estimateMb: Int) {
    ASR(150),   // IndicConformer INT8 session (131 MB weights) + features + arena
    NMT(260),   // IndicTrans2 fp32 fallback (308 MB); INT8 diet -> revisit later
    TTS(130),   // Piper/VITS session + synthesis buffers
    SLM(340)    // Qwen 0.5B INT4 session + bounded KV cache
}

sealed class LoadDecision {
    data class Allow(val reason: String) : LoadDecision()
    data class Deny(val reason: String) : LoadDecision()
}

/**
 * The memory governor for TribeTalk on 2 GB tablets.
 *
 * 1. Measures the app's real footprint (native PSS + Java heap) and the
 *    system's headroom before the LMK kill zone — NOT the Java heap alone.
 * 2. Enforces "one heavy stage resident" via [tryAcquire], evicting the
 *    least-recently-used stage through [evictHandler] before each load.
 * 3. Refuses loads that would push the device into the LMK kill zone so
 *    callers can fall back to lightweight paths.
 *
 * The policy is unit-tested against a fake [MemoryProbe];
 * [AndroidMemoryProbe] is the production implementation.
 */
class MemoryGovernor(
    private val probe: MemoryProbe,
    private val appSteadyBudgetMb: Int = DEFAULT_APP_BUDGET_MB,
    private val safetyHeadroomMb: Long = DEFAULT_SAFETY_HEADROOM_MB
) {
    private val resident = ArrayDeque<GovernorStage>()

    var evictHandler: (GovernorStage) -> Unit = {}

    @Volatile
    var lastSnapshot: MemorySnapshot? = null
        private set

    val residentStages: List<GovernorStage>
        get() = synchronized(resident) { resident.toList() }

    val currentAppBudgetMb: Int
        get() {
            val total = lastSnapshot?.systemTotalMb ?: 2048L
            return if (appSteadyBudgetMb == DEFAULT_APP_BUDGET_MB && total > 0L) {
                computeAdaptiveBudget(total).first
            } else {
                appSteadyBudgetMb
            }
        }

    val currentSafetyHeadroomMb: Long
        get() {
            val total = lastSnapshot?.systemTotalMb ?: 2048L
            return if (safetyHeadroomMb == DEFAULT_SAFETY_HEADROOM_MB && total > 0L) {
                computeAdaptiveBudget(total).second
            } else {
                safetyHeadroomMb
            }
        }

    fun refresh(): MemorySnapshot? {
        lastSnapshot = probe.snapshot()
        return lastSnapshot
    }

    fun canLoad(estimateMb: Int, allowWithoutMeasurement: Boolean = true): LoadDecision {
        val snap = refresh()
            ?: return if (allowWithoutMeasurement) {
                LoadDecision.Allow("no measurement; allowing optimistically")
            } else {
                LoadDecision.Deny("no measurement available")
            }

        val budget = currentAppBudgetMb
        val headroom = currentSafetyHeadroomMb

        if (snap.lowMemory) {
            return LoadDecision.Deny("system low memory: $snap")
        }
        val headroomAfter = snap.systemHeadroomMb - estimateMb
        if (headroomAfter < headroom) {
            return LoadDecision.Deny(
                "headroom after load ${headroomAfter}MB < required ${headroom}MB: $snap"
            )
        }
        val footprintAfter = snap.appFootprintMb + estimateMb
        if (footprintAfter > budget) {
            return LoadDecision.Deny(
                "footprint after load ${footprintAfter}MB > budget ${budget}MB: $snap"
            )
        }
        return LoadDecision.Allow("ok: $snap")
    }

    /** Acquires exclusive residency for [stage]; evicts all resident stages (LRU) first. */
    fun tryAcquire(stage: GovernorStage): Lease? {
        val decision = canLoad(stage.estimateMb)
        if (decision is LoadDecision.Deny) {
            Log.w(TAG, "ACQUIRE DENIED ${stage.name}: ${decision.reason}")
            return null
        }
        val evicted = mutableListOf<GovernorStage>()
        synchronized(resident) {
            while (resident.isNotEmpty()) {
                val victim = resident.removeFirst()
                evicted.add(victim)
                runCatching { evictHandler(victim) }
                    .onFailure { Log.e(TAG, "EVICTION FAILED ${victim.name}: ${it.message}") }
            }
            resident.addLast(stage)
        }
        Log.i(
            TAG,
            "ACQUIRED ${stage.name} (~${stage.estimateMb}MB)" +
                (if (evicted.isNotEmpty()) "; evicted ${evicted.joinToString { it.name }}" else "")
        )
        return Lease(stage)
    }

    /** Emergency trim (onTrimMemory): evict everything. Returns evicted stages. */
    fun evictAll(): List<GovernorStage> {
        val evicted = mutableListOf<GovernorStage>()
        synchronized(resident) {
            while (resident.isNotEmpty()) {
                val victim = resident.removeFirst()
                evicted.add(victim)
                runCatching { evictHandler(victim) }
                    .onFailure { Log.e(TAG, "EVICTION FAILED ${victim.name}: ${it.message}") }
            }
        }
        if (evicted.isNotEmpty()) Log.i(TAG, "EVICT ALL: ${evicted.joinToString { it.name }}")
        return evicted
    }

    inner class Lease internal constructor(private val stage: GovernorStage) {
        fun release() {
            synchronized(resident) { resident.remove(stage) }
            Log.i(TAG, "RELEASED ${stage.name}")
        }
    }

    companion object {
        private const val TAG = "MemoryGovernor"

        /** Steady-state app footprint ceiling for 2 GB tabs (native + Java). */
        const val DEFAULT_APP_BUDGET_MB = 520

        /** Free RAM we must keep above the LMK threshold after any load. */
        const val DEFAULT_SAFETY_HEADROOM_MB = 120L

        /** Computes app budget and safety headroom scaled dynamically to device physical RAM. */
        fun computeAdaptiveBudget(systemTotalMb: Long): Pair<Int, Long> {
            return when {
                systemTotalMb <= 0L -> Pair(DEFAULT_APP_BUDGET_MB, DEFAULT_SAFETY_HEADROOM_MB)
                systemTotalMb <= 2200L -> Pair(520, 120L)    // 2 GB device
                systemTotalMb <= 3500L -> Pair(800, 200L)    // 3 GB device (e.g. MediaPad BAH2-L09)
                systemTotalMb <= 5000L -> Pair(1200, 300L)   // 4 GB device
                systemTotalMb <= 7000L -> Pair(1800, 450L)   // 6 GB device
                else -> Pair(((systemTotalMb * 0.28).toInt()).coerceAtLeast(2000), 500L) // 8+ GB device
            }
        }

        @Volatile
        private var shared: MemoryGovernor? = null

        fun get(context: Context): MemoryGovernor {
            return shared ?: synchronized(this) {
                shared ?: MemoryGovernor(AndroidMemoryProbe(context.applicationContext)).also {
                    shared = it
                }
            }
        }
    }
}
