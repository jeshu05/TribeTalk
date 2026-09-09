package org.tribetalk.core

import android.util.Log

/**
 * JNI interface bridge between Android Kotlin application and C++17 modular runtime.
 * Manages model initialization, speech synthesis, and bidirectional translation.
 */
object NativePipeline {
    private const val TAG = "NativePipeline"
    private var isLibraryLoaded = false

    init {
        try {
            System.loadLibrary("tribetalk_native")
            isLibraryLoaded = true
            Log.i(TAG, "libtribetalk_native.so loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library 'tribetalk_native' not available in runtime: ${e.message}")
            isLibraryLoaded = false
        }
    }

    val isAvailable: Boolean
        get() = isLibraryLoaded

    fun init(assetDir: String): Boolean {
        return if (isLibraryLoaded) {
            try {
                nativeInit(assetDir)
            } catch (e: Throwable) {
                Log.e(TAG, "Error during nativeInit", e)
                false
            }
        } else {
            false
        }
    }

    fun releaseAll() {
        if (isLibraryLoaded) {
            try {
                nativeReleaseAll()
            } catch (e: Throwable) {
                Log.e(TAG, "Error during nativeReleaseAll", e)
            }
        }
    }

    fun trimMemory() {
        if (isLibraryLoaded) {
            try {
                nativeTrimMemory()
            } catch (e: Throwable) {
                Log.e(TAG, "Error during nativeTrimMemory", e)
            }
        }
    }

    fun getState(): Int {
        return if (isLibraryLoaded) {
            try {
                nativeGetState()
            } catch (e: Throwable) {
                0
            }
        } else {
            0
        }
    }

    fun processText(text: String, direction: Int, synthesize: Boolean): String {
        return if (isLibraryLoaded) {
            try {
                nativeProcessText(text, direction, synthesize)
            } catch (e: Throwable) {
                Log.e(TAG, "Error during nativeProcessText", e)
                ""
            }
        } else {
            ""
        }
    }

    fun processSpeech(
        audio: FloatArray,
        sampleRate: Int = 16000,
        direction: Int,
        synthesize: Boolean = true
    ): FloatArray {
        return if (isLibraryLoaded) {
            try {
                nativeProcessSpeech(audio, sampleRate, direction, synthesize)
            } catch (e: Throwable) {
                Log.e(TAG, "Error during nativeProcessSpeech", e)
                FloatArray(0)
            }
        } else {
            FloatArray(0)
        }
    }

    fun processSpeechFull(
        audio: FloatArray,
        sampleRate: Int = 16000,
        direction: Int,
        synthesize: Boolean = true
    ): TranslationExchange? {
        return if (isLibraryLoaded) {
            try {
                nativeProcessSpeechFull(audio, sampleRate, direction, synthesize)
            } catch (e: Throwable) {
                Log.e(TAG, "Error during nativeProcessSpeechFull", e)
                null
            }
        } else {
            null
        }
    }

    // Native external declarations matching tribetalk_jni.cpp
    private external fun nativeInit(assetDir: String): Boolean
    private external fun nativeReleaseAll()
    private external fun nativeTrimMemory()
    private external fun nativeGetState(): Int
    private external fun nativeProcessText(text: String, direction: Int, synthesize: Boolean): String
    private external fun nativeProcessSpeech(
        audio: FloatArray,
        sampleRate: Int,
        direction: Int,
        synthesize: Boolean
    ): FloatArray
    private external fun nativeProcessSpeechFull(
        audio: FloatArray,
        sampleRate: Int,
        direction: Int,
        synthesize: Boolean
    ): TranslationExchange?
}
