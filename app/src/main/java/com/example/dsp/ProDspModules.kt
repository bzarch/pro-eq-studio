package com.example.dsp

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max

/**
 * Professional Dynamic EQ (Dynamic Biquad Filter with sidechain envelope detector).
 * Selectively boosts or cuts specific frequency bands dynamically based on signal energy.
 */
class DynamicBiquadEq {
    var isEnabled: Boolean = false
    var frequency: Float = 2500f
    var q: Float = 2.0f
    var staticGainDb: Float = 0.0f
    var thresholdDb: Float = -24.0f
    var ratio: Float = 3.0f
    var attackMs: Float = 15.0f
    var releaseMs: Float = 120.0f
    var rangeDb: Float = 12.0f // max dynamic boost/cut
    var isDynamicCut: Boolean = true // true = Cut dynamically, false = Boost dynamically

    private val filterL = BiquadFilter()
    private val filterR = BiquadFilter()
    private val sidechainL = BiquadFilter()
    private val sidechainR = BiquadFilter()

    private var envelope: Float = 0.0f
    var dynamicReductionDb: Float = 0.0f
        private set

    fun updateCoefficients(sampleRate: Int) {
        val totalGain = (staticGainDb + if (isDynamicCut) -dynamicReductionDb else dynamicReductionDb).coerceIn(-24f, 24f)
        filterL.configure(FilterType.PEAK, frequency, totalGain, q, sampleRate)
        filterR.configure(FilterType.PEAK, frequency, totalGain, q, sampleRate)

        sidechainL.configure(FilterType.BAND_PASS, frequency, 0f, q, sampleRate)
        sidechainR.configure(FilterType.BAND_PASS, frequency, 0f, q, sampleRate)
    }

    fun process(inL: Float, inR: Float, sampleRate: Int): Pair<Float, Float> {
        if (!isEnabled) return Pair(inL, inR)

        // Detect energy specifically in target band via sidechain
        val scL = sidechainL.processLeft(inL)
        val scR = sidechainR.processRight(inR)
        val peak = max(abs(scL), abs(scR)).coerceAtLeast(1e-6f)
        val peakDb = 20.0f * kotlin.math.log10(peak)

        val over = peakDb - thresholdDb
        val targetModDb = if (over > 0f) {
            val unconstrained = (over * (1.0f - 1.0f / ratio)).coerceIn(0f, rangeDb)
            unconstrained
        } else {
            0.0f
        }

        val alphaAttack = exp(-1.0f / (attackMs * 0.001f * sampleRate))
        val alphaRelease = exp(-1.0f / (releaseMs * 0.001f * sampleRate))
        val coeff = if (targetModDb > envelope) alphaAttack else alphaRelease
        envelope = coeff * envelope + (1.0f - coeff) * targetModDb
        dynamicReductionDb = envelope

        // Recompute filter gain on the fly with smooth envelope
        val effGain = if (isDynamicCut) (staticGainDb - envelope) else (staticGainDb + envelope)
        filterL.configure(FilterType.PEAK, frequency, effGain, q, sampleRate)
        filterR.configure(FilterType.PEAK, frequency, effGain, q, sampleRate)

        val outL = filterL.processLeft(inL)
        val outR = filterR.processRight(inR)
        return Pair(outL, outR)
    }
}

/**
 * Headphone Crossfeed and Spatial Cross-Channel acoustic simulator (Jan Meier / Chu Moy algorithm).
 * Simulates binaural acoustic bleeding to eliminate headphone listening fatigue.
 */
class HeadphoneCrossfeedProcessor {
    var isEnabled: Boolean = false
    var amount: Float = 0.35f // 0 to 1

    private val crossfeedFilterL = BiquadFilter()
    private val crossfeedFilterR = BiquadFilter()

    fun updateSampleRate(sr: Int) {
        // High frequencies attenuated (~700Hz shelf) to emulate acoustic head shadowing
        crossfeedFilterL.configure(FilterType.LOW_PASS, 700f, 0f, 0.707f, sr)
        crossfeedFilterR.configure(FilterType.LOW_PASS, 700f, 0f, 0.707f, sr)
    }

    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        if (!isEnabled || amount <= 0.01f) return Pair(inL, inR)

        val crossL = crossfeedFilterL.processLeft(inR) * amount * 0.5f
        val crossR = crossfeedFilterR.processRight(inL) * amount * 0.5f

        val outL = (inL + crossL) / (1.0f + amount * 0.3f)
        val outR = (inR + crossR) / (1.0f + amount * 0.3f)
        return Pair(outL, outR)
    }
}

/**
 * High-precision Multi-channel Speaker Time Alignment Delay Line (0 to 50 milliseconds).
 */
class TimeAlignmentDelay(sampleRate: Int = 48000) {
    var delayMs: Float = 0.0f
    private val maxSamples = (0.060f * 96000).toInt() + 128
    private val ringBuffer = FloatArray(maxSamples)
    private var writeIdx = 0

    fun process(sample: Float, currentSr: Int): Float {
        if (delayMs <= 0.05f) return sample

        ringBuffer[writeIdx] = sample
        val delaySamples = (delayMs * 0.001f * currentSr).toInt().coerceIn(1, maxSamples - 1)
        var readIdx = writeIdx - delaySamples
        if (readIdx < 0) readIdx += maxSamples

        writeIdx++
        if (writeIdx >= maxSamples) writeIdx = 0

        return ringBuffer[readIdx]
    }
}
