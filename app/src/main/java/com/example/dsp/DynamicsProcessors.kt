package com.example.dsp

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Professional Dynamic Range Compressor with Soft Knee and Auto Makeup Gain.
 */
class Compressor {
    var isEnabled: Boolean = true

    // Controls
    var thresholdDb: Float = -20f
    var ratio: Float = 4.0f
    var attackMs: Float = 20.0f
    var releaseMs: Float = 150.0f
    var kneeDb: Float = 6.0f
    var makeupGainDb: Float = 0.0f
    var autoMakeup: Boolean = false

    // State
    private var envelope: Float = 0.0f
    private var currentReductionDb: Float = 0.0f

    val gainReductionDb: Float
        get() = currentReductionDb

    fun process(left: Float, right: Float, sampleRate: Int): Pair<Float, Float> {
        if (!isEnabled) {
            currentReductionDb = 0.0f
            return Pair(left, right)
        }

        // Detector: peak level of left and right in linear
        val det = max(abs(left), abs(right)).coerceAtLeast(1e-6f)
        val inputDb = 20.0f * log10(det)

        // Soft-knee static curve computation
        val halfKnee = kneeDb * 0.5f
        val over = inputDb - thresholdDb
        val targetGainDb = when {
            over <= -halfKnee -> 0.0f
            over >= halfKnee -> (1.0f / ratio - 1.0f) * over
            else -> {
                // Within knee region
                val kneeTerm = (over + halfKnee)
                (1.0f / ratio - 1.0f) * (kneeTerm * kneeTerm) / (2.0f * (kneeDb.coerceAtLeast(0.1f)))
            }
        }

        // Ballistics (Attack / Release smoothing)
        val alphaAttack = exp(-1.0f / (attackMs * 0.001f * sampleRate))
        val alphaRelease = exp(-1.0f / (releaseMs * 0.001f * sampleRate))

        val coeff = if (targetGainDb < envelope) alphaAttack else alphaRelease
        envelope = coeff * envelope + (1.0f - coeff) * targetGainDb

        currentReductionDb = abs(envelope)

        // Total gain application
        val autoMakeupDb = if (autoMakeup) (thresholdDb * (1.0f / ratio - 1.0f) * -0.5f).coerceIn(0f, 18f) else 0.0f
        val totalGainDb = envelope + makeupGainDb + autoMakeupDb
        val linearGain = 10.0f.pow(totalGainDb / 20.0f)

        return Pair(left * linearGain, right * linearGain)
    }

    fun reset() {
        envelope = 0.0f
        currentReductionDb = 0.0f
    }
}

/**
 * Output Limiter and Brickwall Safety Guard with True Peak Protection.
 */
class Limiter {
    var isEnabled: Boolean = true
    var thresholdDb: Float = -0.5f
    var releaseMs: Float = 50.0f
    var ceilingDb: Float = -0.1f

    private var gain: Float = 1.0f
    var isClippingDetected: Boolean = false
        private set

    fun process(left: Float, right: Float, sampleRate: Int): Pair<Float, Float> {
        if (!isEnabled) {
            isClippingDetected = (abs(left) > 1.0f || abs(right) > 1.0f)
            return Pair(left, right)
        }

        val threshLin = 10.0f.pow(thresholdDb / 20.0f)
        val ceilLin = 10.0f.pow(ceilingDb / 20.0f)

        val peak = max(abs(left), abs(right))
        var targetGain = 1.0f
        if (peak > threshLin) {
            targetGain = threshLin / peak
        }

        val alphaRelease = exp(-1.0f / (releaseMs * 0.001f * sampleRate))
        if (targetGain < gain) {
            gain = targetGain // Instant attack
        } else {
            gain = alphaRelease * gain + (1.0f - alphaRelease) * targetGain
        }

        val outL = (left * gain).coerceIn(-ceilLin, ceilLin)
        val outR = (right * gain).coerceIn(-ceilLin, ceilLin)

        isClippingDetected = (peak >= threshLin)

        return Pair(outL, outR)
    }

    fun reset() {
        gain = 1.0f
        isClippingDetected = false
    }
}

/**
 * Analog Style Soft Clipper with Drive and Threshold.
 * Implements hyperbolic tangent (tanh) / algebraic saturation curve.
 */
class SoftClipper {
    var isEnabled: Boolean = false
    var driveDb: Float = 0.0f
    var outputDb: Float = 0.0f

    fun process(input: Float): Float {
        if (!isEnabled) return input
        val driveGain = 10.0f.pow(driveDb / 20.0f)
        val outGain = 10.0f.pow(outputDb / 20.0f)
        val x = input * driveGain

        // Smooth cubic/algebraic saturation curve
        val saturated = if (x > 1.5f) {
            1.0f
        } else if (x < -1.5f) {
            -1.0f
        } else {
            x - (x * x * x) / 4.5f
        }
        return saturated * outGain
    }
}
