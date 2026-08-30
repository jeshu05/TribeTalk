package com.alchemists.tribetalk.voice

import org.junit.Assert.*
import org.junit.Test

class AudioVADProcessorTest {

    @Test
    fun testFrameSizeCalculation() {
        val vad = AudioVADProcessor(sampleRate = 16000)
        // 30ms at 16000 Hz = 480 samples
        assertEquals(480, vad.frameSizeSamples)
    }

    @Test
    fun testSilenceDropping() {
        val vad = AudioVADProcessor(sampleRate = 16000, energyThreshold = 500.0)
        val silentAudio = ShortArray(16000) // 1 second of total silence

        var voiceFramesEmitted = 0
        vad.processSamples(silentAudio) {
            voiceFramesEmitted++
        }
        vad.flush {
            voiceFramesEmitted++
        }

        // Silent frames exceeding 100ms should be dropped, producing 0 voice frames
        assertEquals(0, voiceFramesEmitted)
    }

    @Test
    fun testSpeechFramePassThrough() {
        val vad = AudioVADProcessor(sampleRate = 16000, energyThreshold = 100.0)
        // Synthetic speech wave: 200 Hz tone at 16kHz for 1 second
        val speechAudio = ShortArray(16000)
        for (i in speechAudio.indices) {
            val wave = kotlin.math.sin(2.0 * Math.PI * 200.0 * i / 16000)
            speechAudio[i] = (wave * 5000).toInt().toShort()
        }

        var voiceFramesEmitted = 0
        vad.processSamples(speechAudio) {
            voiceFramesEmitted++
        }
        vad.flush {
            voiceFramesEmitted++
        }

        assertTrue("Speech should trigger voice frames", voiceFramesEmitted >= 1)
    }
}
