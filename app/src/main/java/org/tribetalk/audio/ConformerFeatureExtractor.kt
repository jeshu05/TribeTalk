package org.tribetalk.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance audio feature extractor for NeMo / Sherpa IndicConformer models.
 * Computes 80-channel log-mel filterbank spectrogram with per-feature normalization.
 */
class ConformerFeatureExtractor(
    val sampleRate: Int = 16000,
    val nFft: Int = 512,
    val winLength: Int = 400,
    val hopLength: Int = 160,
    val nMels: Int = 80,
    val padTo: Int = 16,
    val logZeroGuard: Float = 5.9604645e-8f
) {
    private val numFftBins = (nFft / 2) + 1
    private val window = FloatArray(winLength)
    private val melFilters = Array(nMels) { FloatArray(numFftBins) }

    // Precomputed FFT twiddle factors for 512-point FFT
    private val cosTable = FloatArray(nFft / 2)
    private val sinTable = FloatArray(nFft / 2)
    private val bitRev = IntArray(nFft)

    init {
        initHannWindow()
        initMelFilterbanks()
        initFftTables()
    }

    private fun initHannWindow() {
        for (i in 0 until winLength) {
            window[i] = (0.5f * (1.0f - cos(2.0f * PI.toFloat() * i / winLength.toFloat())))
        }
    }

    private fun hzToMel(hz: Float): Float = 2595.0f * log10(1.0f + hz / 700.0f)
    private fun melToHz(mel: Float): Float = 700.0f * (10.0f.pow(mel / 2595.0f) - 1.0f)

    private fun initMelFilterbanks() {
        val lowMel = hzToMel(0.0f)
        val highMel = hzToMel(sampleRate / 2.0f)

        val melPoints = FloatArray(nMels + 2)
        for (i in 0 until nMels + 2) {
            melPoints[i] = lowMel + (highMel - lowMel) * i.toFloat() / (nMels + 1).toFloat()
        }

        val binPoints = IntArray(nMels + 2)
        for (i in 0 until nMels + 2) {
            val hz = melToHz(melPoints[i])
            binPoints[i] = floor((nFft + 1) * hz / sampleRate).toInt().coerceIn(0, numFftBins - 1)
        }

        for (m in 0 until nMels) {
            val left = binPoints[m]
            val center = binPoints[m + 1]
            val right = binPoints[m + 2]

            for (k in left until center) {
                if (center != left) {
                    melFilters[m][k] = (k - left).toFloat() / (center - left).toFloat()
                }
            }
            for (k in center until right) {
                if (right != center) {
                    melFilters[m][k] = (right - k).toFloat() / (right - center).toFloat()
                }
            }
        }
    }

    private fun initFftTables() {
        for (i in 0 until nFft / 2) {
            val angle = -2.0f * PI.toFloat() * i / nFft.toFloat()
            cosTable[i] = cos(angle)
            sinTable[i] = sin(angle)
        }
        val bits = 9 // 2^9 = 512
        for (i in 0 until nFft) {
            var rev = 0
            var temp = i
            for (j in 0 until bits) {
                rev = (rev shl 1) or (temp and 1)
                temp = temp shr 1
            }
            bitRev[i] = rev
        }
    }

    /**
     * In-place Radix-2 Cooley-Tukey FFT for 512 points.
     */
    private fun fft512(real: FloatArray, imag: FloatArray) {
        // Bit-reversal permutation
        for (i in 0 until nFft) {
            val j = bitRev[i]
            if (j > i) {
                val tempR = real[i]; real[i] = real[j]; real[j] = tempR
                val tempI = imag[i]; imag[i] = imag[j]; imag[j] = tempI
            }
        }

        // Cooley-Tukey butterflies
        var len = 2
        while (len <= nFft) {
            val halfLen = len / 2
            val step = nFft / len
            var k = 0
            while (k < nFft) {
                var w = 0
                for (j in 0 until halfLen) {
                    val c = cosTable[w]
                    val s = sinTable[w]
                    val uR = real[k + j]
                    val uI = imag[k + j]
                    val vR = real[k + j + halfLen] * c - imag[k + j + halfLen] * s
                    val vI = real[k + j + halfLen] * s + imag[k + j + halfLen] * c

                    real[k + j] = uR + vR
                    imag[k + j] = uI + vI
                    real[k + j + halfLen] = uR - vR
                    imag[k + j + halfLen] = uI - vI
                    w += step
                }
                k += len
            }
            len = len shl 1
        }
    }

    /**
     * Extracts normalized 80-channel log mel features.
     * @param audio 1D normalized float32 audio [-1.0, 1.0]
     * @return Pair of flattened features array [1 * 80 * paddedFrames] and paddedFrames count.
     */
    fun extractFeatures(audio: FloatArray): Pair<FloatArray, Int> {
        val pad = nFft / 2
        val paddedAudio = FloatArray(audio.size + 2 * pad)
        System.arraycopy(audio, 0, paddedAudio, pad, audio.size)

        var numFrames = 0
        if (paddedAudio.size >= winLength) {
            numFrames = ((paddedAudio.size - winLength) / hopLength) + 1
        }
        if (numFrames <= 0) numFrames = 1

        val melSpec = Array(nMels) { FloatArray(numFrames) }
        val realBuf = FloatArray(nFft)
        val imagBuf = FloatArray(nFft)
        val powerSpec = FloatArray(numFftBins)

        for (t in 0 until numFrames) {
            val start = t * hopLength
            realBuf.fill(0f)
            imagBuf.fill(0f)

            for (i in 0 until winLength) {
                if (start + i < paddedAudio.size) {
                    realBuf[i] = paddedAudio[start + i] * window[i]
                }
            }

            fft512(realBuf, imagBuf)

            for (k in 0 until numFftBins) {
                val r = realBuf[k]
                val im = imagBuf[k]
                powerSpec[k] = r * r + im * im
            }

            for (m in 0 until nMels) {
                var energy = 0f
                val filter = melFilters[m]
                for (k in 0 until numFftBins) {
                    energy += powerSpec[k] * filter[k]
                }
                melSpec[m][t] = log(energy + logZeroGuard, kotlin.math.E.toFloat())
            }
        }

        // Per-feature normalization along time: (x - mean) / (std + 1e-5)
        for (m in 0 until nMels) {
            var sum = 0.0
            for (t in 0 until numFrames) {
                sum += melSpec[m][t]
            }
            val mean = (sum / numFrames).toFloat()

            var sqSum = 0.0
            for (t in 0 until numFrames) {
                val diff = melSpec[m][t] - mean
                sqSum += diff * diff
            }
            val std = sqrt(sqSum / numFrames).toFloat()

            for (t in 0 until numFrames) {
                melSpec[m][t] = (melSpec[m][t] - mean) / (std + 1e-5f)
            }
        }

        // Pad time frames to multiple of padTo (16)
        val padAmount = (padTo - (numFrames % padTo)) % padTo
        val paddedFrames = numFrames + padAmount

        val flatFeatures = FloatArray(nMels * paddedFrames)
        for (m in 0 until nMels) {
            for (t in 0 until numFrames) {
                flatFeatures[m * paddedFrames + t] = melSpec[m][t]
            }
        }

        return Pair(flatFeatures, paddedFrames)
    }
}
