package org.tribetalk.core.memory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Policy tests for [MemoryGovernor] using a fake [MemoryProbe] — no device needed.
 * These encode the 2 GB tablet acceptance rules from the memory plan.
 */
class MemoryGovernorTest {

    private class FakeProbe(private var snap: MemorySnapshot?) : MemoryProbe {
        fun set(s: MemorySnapshot?) { snap = s }
        override fun snapshot(): MemorySnapshot? = snap

        companion object {
            fun healthy(native: Int = 60, java: Int = 40) = MemorySnapshot(
                nativePssMb = native, javaHeapUsedMb = java, javaHeapMaxMb = 256,
                systemAvailMb = 800, systemThresholdMb = 128,
                lowMemory = false, timestampMs = 0L
            )
        }
    }

    private fun snap(native: Int, avail: Long, threshold: Long = 128, low: Boolean = false) =
        MemorySnapshot(
            nativePssMb = native, javaHeapUsedMb = 40, javaHeapMaxMb = 256,
            systemAvailMb = avail, systemThresholdMb = threshold,
            lowMemory = low, timestampMs = 0L
        )

    // --- canLoad rules -------------------------------------------------------

    @Test
    fun `denies when system reports low memory`() {
        val probe = FakeProbe(snap(60, avail = 800, low = true))
        val gov = MemoryGovernor(probe)
        val d = gov.canLoad(90)
        assertTrue(d is LoadDecision.Deny)
    }

    @Test
    fun `denies when load would enter LMK kill zone`() {
        // avail 400, threshold 128 -> headroom 272; loading 340 SLM leaves -68 < 120 required
        val probe = FakeProbe(snap(60, avail = 400))
        val gov = MemoryGovernor(probe)
        val d = gov.canLoad(340)
        assertTrue(d is LoadDecision.Deny)
    }

    @Test
    fun `denies when app footprint would exceed steady budget`() {
        // native 400 + java 40 = 440; +210 NMT = 650 > 520 budget
        val probe = FakeProbe(snap(400, avail = 900))
        val gov = MemoryGovernor(probe)
        val d = gov.canLoad(210)
        assertTrue(d is LoadDecision.Deny)
    }

    @Test
    fun `allows reasonable load with healthy headroom`() {
        val probe = FakeProbe(FakeProbe.healthy())
        val gov = MemoryGovernor(probe)
        val d = gov.canLoad(90)
        assertTrue(d is LoadDecision.Allow)
    }

    @Test
    fun `allows optimistically when no measurement available by default`() {
        val probe = FakeProbe(null)
        val gov = MemoryGovernor(probe)
        assertTrue(gov.canLoad(340) is LoadDecision.Allow)
        assertTrue(gov.canLoad(340, allowWithoutMeasurement = false) is LoadDecision.Deny)
    }

    // --- residency + eviction ------------------------------------------------

    @Test
    fun `acquire evicts resident stage before load (unload-before-load)`() {
        val probe = FakeProbe(FakeProbe.healthy())
        val gov = MemoryGovernor(probe)
        val evicted = mutableListOf<GovernorStage>()
        gov.evictHandler = { evicted.add(it) }

        val leaseA = gov.tryAcquire(GovernorStage.ASR)
        assertNotNull(leaseA)
        assertEquals(listOf<GovernorStage>(), evicted)

        val leaseB = gov.tryAcquire(GovernorStage.SLM)
        assertNotNull(leaseB)
        assertEquals(listOf(GovernorStage.ASR), evicted)
        assertEquals(listOf(GovernorStage.SLM), gov.residentStages)
    }

    @Test
    fun `release removes stage from resident set`() {
        val probe = FakeProbe(FakeProbe.healthy())
        val gov = MemoryGovernor(probe)
        val lease = gov.tryAcquire(GovernorStage.TTS)!!
        assertEquals(listOf(GovernorStage.TTS), gov.residentStages)
        lease.release()
        assertTrue(gov.residentStages.isEmpty())
    }

    @Test
    fun `evictAll clears residency and reports victims`() {
        val probe = FakeProbe(FakeProbe.healthy())
        val gov = MemoryGovernor(probe)
        val evicted = mutableListOf<GovernorStage>()
        gov.evictHandler = { evicted.add(it) }
        gov.tryAcquire(GovernorStage.NMT)
        gov.tryAcquire(GovernorStage.TTS) // evicts NMT, holds TTS

        val all = gov.evictAll()
        assertEquals(listOf(GovernorStage.TTS), all)
        assertTrue(gov.residentStages.isEmpty())
        // NMT evicted during acquire, TTS during evictAll
        assertEquals(listOf(GovernorStage.NMT, GovernorStage.TTS), evicted)
    }

    @Test
    fun `eviction handler failure does not corrupt state`() {
        val probe = FakeProbe(FakeProbe.healthy())
        val gov = MemoryGovernor(probe)
        gov.evictHandler = { throw RuntimeException("unload crashed") }
        gov.tryAcquire(GovernorStage.ASR)
        // Second acquire must still proceed and record the new resident stage.
        gov.tryAcquire(GovernorStage.NMT)
        assertEquals(listOf(GovernorStage.NMT), gov.residentStages)
    }

    @Test
    fun `stage estimates stay within the 2GB mode budgets`() {
        // Translate mode: ASR + NMT + TTS sequential, never co-resident; worst
        // single stage + baseline must fit under the app budget.
        val translatePeak = GovernorStage.NMT.estimateMb + 120 // baseline UI/audio
        assertTrue(translatePeak <= MemoryGovernor.DEFAULT_APP_BUDGET_MB)
        // Worksheet mode: SLM + TTS must fit too.
        val worksheetPeak = GovernorStage.SLM.estimateMb + GovernorStage.TTS.estimateMb + 60
        assertTrue(worksheetPeak <= MemoryGovernor.DEFAULT_APP_BUDGET_MB + 130)
    }
}