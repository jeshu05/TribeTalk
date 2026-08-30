package com.alchemists.tribetalk.voice

import kotlin.math.abs

/**
 * WebRTC-style Voice Activity Detection (VAD) audio pre-processor.
 *
 * Input: Raw continuous 16kHz 16-bit mono PCM microphone audio.
 * Workflow: Evaluates 30ms audio frames (480 samples / 960 bytes).
 *           Strips silent intervals exceeding 100ms to output clean 1-to-3 second voice frames.
 */
class AudioVADProcessor(
    private val sampleRate: Int = 16000,
    private val energyThreshold: Double = 100.0,
    private val silenceLimitMs: Int = 300
) {
    // 30ms frame at 16kHz = 480 samples
    val frameSizeSamples: Int = (sampleRate * 0.030).toInt()
    private val silenceFramesLimit: Int = silenceLimitMs / 30 // ~10 frames (300ms)

    private var consecutiveSilenceFrames = 0
    private val activeVoiceBuffer = mutableListOf<Short>()

    /**
     * Processes a chunk of 16-bit PCM audio samples.
     * Evaluates in 30ms windows, filters silence, and triggers onVoiceFrame when a 1-3s speech unit is complete.
     */
    fun processSamples(
        samples: ShortArray,
        onVoiceFrame: (ShortArray) -> Unit
    ) {
        var offset = 0
        while (offset + frameSizeSamples <= samples.size) {
            val frame = ShortArray(frameSizeSamples)
            System.arraycopy(samples, offset, frame, 0, frameSizeSamples)
            offset += frameSizeSamples

            val isSpeech = isFrameSpeech(frame)

            if (isSpeech) {
                consecutiveSilenceFrames = 0
                for (s in frame) activeVoiceBuffer.add(s)

                // If voice buffer reaches ~3 seconds (48,000 samples), emit frame
                if (activeVoiceBuffer.size >= sampleRate * 3) {
                    onVoiceFrame(activeVoiceBuffer.toShortArray())
                    activeVoiceBuffer.clear()
                }
            } else {
                consecutiveSilenceFrames++
                if (activeVoiceBuffer.isNotEmpty()) {
                    // Include trailing frame for smooth decay
                    if (consecutiveSilenceFrames <= silenceFramesLimit) {
                        for (s in frame) activeVoiceBuffer.add(s)
                    } else {
                        // Silent interval exceeds limit: emit active voice segment if >= 0.3 sec
                        if (activeVoiceBuffer.size >= sampleRate * 0.3) {
                            onVoiceFrame(activeVoiceBuffer.toShortArray())
                        }
                        activeVoiceBuffer.clear()
                    }
                }
            }
        }
    }

    private fun isFrameSpeech(frame: ShortArray): Boolean {
        var sumEnergy = 0.0
        var zeroCrossings = 0
        for (i in frame.indices) {
            sumEnergy += abs(frame[i].toDouble())
            if (i > 0 && ((frame[i] >= 0 && frame[i - 1] < 0) || (frame[i] < 0 && frame[i - 1] >= 0))) {
                zeroCrossings++
            }
        }
        val avgEnergy = sumEnergy / frame.size
        // Realistic voice characteristics for mobile device microphones
        return avgEnergy > energyThreshold && zeroCrossings >= 3
    }

    fun flush(onVoiceFrame: (ShortArray) -> Unit) {
        if (activeVoiceBuffer.size >= sampleRate * 0.2) {
            onVoiceFrame(activeVoiceBuffer.toShortArray())
        }
        activeVoiceBuffer.clear()
        consecutiveSilenceFrames = 0
    }
}
