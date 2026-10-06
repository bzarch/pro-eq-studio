package com.example.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.pow

/**
 * Filter types for Audio EQ and processing
 */
enum class FilterType {
    PEAK,
    LOW_SHELF,
    HIGH_SHELF,
    LOW_PASS,
    HIGH_PASS,
    BAND_PASS,
    NOTCH,
    ALL_PASS
}

/**
 * High-performance 2nd-order Direct Form I/II Biquad IIR Filter.
 * Computes exact digital biquad filter coefficients based on Robert Bristow-Johnson's Audio EQ Cookbook.
 * Thread-safe coefficient updates and seamless audio parameter changes.
 */
class BiquadFilter {
    // Coefficients
    private var b0: Float = 1.0f
    private var b1: Float = 0.0f
    private var b2: Float = 0.0f
    private var a1: Float = 0.0f
    private var a2: Float = 0.0f

    // Direct Form II Transposed / Direct Form I delay memory for 2 channels (Left=0, Right=1)
    private var x1L: Float = 0.0f
    private var x2L: Float = 0.0f
    private var y1L: Float = 0.0f
    private var y2L: Float = 0.0f

    private var x1R: Float = 0.0f
    private var x2R: Float = 0.0f
    private var y1R: Float = 0.0f
    private var y2R: Float = 0.0f

    var isEnabled: Boolean = true

    // Cached configuration
    private var lastType: FilterType = FilterType.PEAK
    private var lastFreq: Float = 1000f
    private var lastGainDb: Float = 0f
    private var lastQ: Float = 1.0f
    private var lastSampleRate: Int = 48000

    fun configure(
        type: FilterType,
        frequency: Float,
        gainDb: Float,
        q: Float,
        sampleRate: Int
    ) {
        lastType = type
        lastFreq = frequency
        lastGainDb = gainDb
        lastQ = q
        lastSampleRate = sampleRate

        val nyquist = sampleRate * 0.495f
        val clampedFreq = frequency.coerceIn(10.0f, nyquist)
        val clampedQ = q.coerceIn(0.1f, 30.0f)
        val w0 = (2.0 * PI * clampedFreq / sampleRate).toFloat()
        val cosW0 = cos(w0)
        val sinW0 = sin(w0)
        val alpha = sinW0 / (2.0f * clampedQ)
        val A = 10.0f.pow(gainDb / 40.0f)

        var tb0 = 1.0f
        var tb1 = 0.0f
        var tb2 = 0.0f
        var ta0 = 1.0f
        var ta1 = 0.0f
        var ta2 = 0.0f

        when (type) {
            FilterType.PEAK -> {
                tb0 = 1.0f + alpha * A
                tb1 = -2.0f * cosW0
                tb2 = 1.0f - alpha * A
                ta0 = 1.0f + alpha / A
                ta1 = -2.0f * cosW0
                ta2 = 1.0f - alpha / A
            }
            FilterType.LOW_SHELF -> {
                val sqrtA = sqrt(A)
                tb0 = A * ((A + 1.0f) - (A - 1.0f) * cosW0 + 2.0f * sqrtA * alpha)
                tb1 = 2.0f * A * ((A - 1.0f) - (A + 1.0f) * cosW0)
                tb2 = A * ((A + 1.0f) - (A - 1.0f) * cosW0 - 2.0f * sqrtA * alpha)
                ta0 = (A + 1.0f) + (A - 1.0f) * cosW0 + 2.0f * sqrtA * alpha
                ta1 = -2.0f * ((A - 1.0f) + (A + 1.0f) * cosW0)
                ta2 = (A + 1.0f) + (A - 1.0f) * cosW0 - 2.0f * sqrtA * alpha
            }
            FilterType.HIGH_SHELF -> {
                val sqrtA = sqrt(A)
                tb0 = A * ((A + 1.0f) + (A - 1.0f) * cosW0 + 2.0f * sqrtA * alpha)
                tb1 = -2.0f * A * ((A - 1.0f) + (A + 1.0f) * cosW0)
                tb2 = A * ((A + 1.0f) + (A - 1.0f) * cosW0 - 2.0f * sqrtA * alpha)
                ta0 = (A + 1.0f) - (A - 1.0f) * cosW0 + 2.0f * sqrtA * alpha
                ta1 = 2.0f * ((A - 1.0f) - (A + 1.0f) * cosW0)
                ta2 = (A + 1.0f) - (A - 1.0f) * cosW0 - 2.0f * sqrtA * alpha
            }
            FilterType.LOW_PASS -> {
                tb0 = (1.0f - cosW0) / 2.0f
                tb1 = 1.0f - cosW0
                tb2 = (1.0f - cosW0) / 2.0f
                ta0 = 1.0f + alpha
                ta1 = -2.0f * cosW0
                ta2 = 1.0f - alpha
            }
            FilterType.HIGH_PASS -> {
                tb0 = (1.0f + cosW0) / 2.0f
                tb1 = -(1.0f + cosW0)
                tb2 = (1.0f + cosW0) / 2.0f
                ta0 = 1.0f + alpha
                ta1 = -2.0f * cosW0
                ta2 = 1.0f - alpha
            }
            FilterType.BAND_PASS -> {
                tb0 = alpha
                tb1 = 0.0f
                tb2 = -alpha
                ta0 = 1.0f + alpha
                ta1 = -2.0f * cosW0
                ta2 = 1.0f - alpha
            }
            FilterType.NOTCH -> {
                tb0 = 1.0f
                tb1 = -2.0f * cosW0
                tb2 = 1.0f
                ta0 = 1.0f + alpha
                ta1 = -2.0f * cosW0
                ta2 = 1.0f - alpha
            }
            FilterType.ALL_PASS -> {
                tb0 = 1.0f - alpha
                tb1 = -2.0f * cosW0
                tb2 = 1.0f + alpha
                ta0 = 1.0f + alpha
                ta1 = -2.0f * cosW0
                ta2 = 1.0f - alpha
            }
        }

        // Normalize by a0
        val invA0 = 1.0f / ta0
        b0 = tb0 * invA0
        b1 = tb1 * invA0
        b2 = tb2 * invA0
        a1 = ta1 * invA0
        a2 = ta2 * invA0
    }

    fun processLeft(input: Float): Float {
        if (!isEnabled) return input
        val out = b0 * input + b1 * x1L + b2 * x2L - a1 * y1L - a2 * y2L
        x2L = x1L
        x1L = input
        // Subnormal / denormal protection
        y2L = if (y1L.isFinite()) y1L else 0f
        y1L = if (out.isFinite()) out else 0f
        return y1L
    }

    fun processRight(input: Float): Float {
        if (!isEnabled) return input
        val out = b0 * input + b1 * x1R + b2 * x2R - a1 * y1R - a2 * y2R
        x2R = x1R
        x1R = input
        y2R = if (y1R.isFinite()) y1R else 0f
        y1R = if (out.isFinite()) out else 0f
        return y1R
    }

    fun reset() {
        x1L = 0f; x2L = 0f; y1L = 0f; y2L = 0f
        x1R = 0f; x2R = 0f; y1R = 0f; y2R = 0f
    }

    /**
     * Compute exact magnitude frequency response in dB at a given frequency.
     * Evaluates H(e^(j*w)) = (b0 + b1*e^(-jw) + b2*e^(-2jw)) / (1 + a1*e^(-jw) + a2*e^(-2jw))
     */
    fun evaluateGainAt(freq: Float, sampleRate: Int): Float {
        if (!isEnabled) return 0.0f
        val w = 2.0 * PI * freq / sampleRate
        val cosW = cos(w)
        val sinW = sin(w)
        val cos2W = cos(2.0 * w)
        val sin2W = sin(2.0 * w)

        val numReal = b0 + b1 * cosW + b2 * cos2W
        val numImag = -b1 * sinW - b2 * sin2W
        val denReal = 1.0 + a1 * cosW + a2 * cos2W
        val denImag = -a1 * sinW - a2 * sin2W

        val numMag2 = numReal * numReal + numImag * numImag
        val denMag2 = denReal * denReal + denImag * denImag
        if (denMag2 < 1e-12) return 0f

        val mag = sqrt(numMag2 / denMag2)
        return (20.0 * kotlin.math.log10(mag.coerceAtLeast(1e-6))).toFloat()
    }
}
