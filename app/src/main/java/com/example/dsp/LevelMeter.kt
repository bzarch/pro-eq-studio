package com.example.dsp

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Ballistics Level and Clip Metering data snapshot.
 */
data class MeterData(
    val peakL: Float = 0.0f,
    val peakR: Float = 0.0f,
    val rmsL: Float = 0.0f,
    val rmsR: Float = 0.0f,
    val peakDbL: Float = -90f,
    val peakDbR: Float = -90f,
    val rmsDbL: Float = -90f,
    val rmsDbR: Float = -90f,
    val isClipping: Boolean = false
)

/**
 * High-precision Peak / RMS Audio Meter with Peak Hold decay.
 */
class LevelMeter {
    private var maxPeakL = 0.0f
    private var maxPeakR = 0.0f
    private var sumSqL = 0.0f
    private var sumSqR = 0.0f
    private var sampleCount = 0

    // Smooth envelope followers
    private var smoothPeakL = 0.0f
    private var smoothPeakR = 0.0f
    private var smoothRmsL = 0.0f
    private var smoothRmsR = 0.0f

    @Volatile
    var snapshot = MeterData()
        private set

    fun accumulate(left: Float, right: Float) {
        val aL = abs(left)
        val aR = abs(right)
        if (aL > maxPeakL) maxPeakL = aL
        if (aR > maxPeakR) maxPeakR = aR
        sumSqL += left * left
        sumSqR += right * right
        sampleCount++
    }

    fun finishBlock(decayFactor: Float = 0.85f) {
        if (sampleCount == 0) return
        val rawRmsL = sqrt(sumSqL / sampleCount)
        val rawRmsR = sqrt(sumSqR / sampleCount)

        smoothPeakL = max(maxPeakL, smoothPeakL * decayFactor)
        smoothPeakR = max(maxPeakR, smoothPeakR * decayFactor)
        smoothRmsL = smoothRmsL * decayFactor + rawRmsL * (1.0f - decayFactor)
        smoothRmsR = smoothRmsR * decayFactor + rawRmsR * (1.0f - decayFactor)

        val pDbL = linearToDb(smoothPeakL)
        val pDbR = linearToDb(smoothPeakR)
        val rDbL = linearToDb(smoothRmsL)
        val rDbR = linearToDb(smoothRmsR)
        val clipping = smoothPeakL >= 0.999f || smoothPeakR >= 0.999f

        snapshot = MeterData(
            peakL = smoothPeakL.coerceIn(0f, 1f),
            peakR = smoothPeakR.coerceIn(0f, 1f),
            rmsL = smoothRmsL.coerceIn(0f, 1f),
            rmsR = smoothRmsR.coerceIn(0f, 1f),
            peakDbL = pDbL,
            peakDbR = pDbR,
            rmsDbL = rDbL,
            rmsDbR = rDbR,
            isClipping = clipping
        )

        maxPeakL = 0.0f
        maxPeakR = 0.0f
        sumSqL = 0.0f
        sumSqR = 0.0f
        sampleCount = 0
    }

    private fun linearToDb(v: Float): Float {
        if (v < 1e-5f) return -90.0f
        return (20.0f * log10(v)).coerceIn(-90f, 6f)
    }
}
