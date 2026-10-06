package com.example.dsp

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class RoutingMode {
    STEREO,
    MONO,
    LEFT_ONLY,
    RIGHT_ONLY,
    SWAP,
    DUAL_MONO
}

/**
 * Channel Router, Mono Summing, Mid/Side Processor, Stereo Widener, and Polarity/Phase Adjuster.
 * Uses 3 dB pan law safe mono summing (1 / sqrt(2) = 0.7071) to prevent acoustic clipping.
 */
class StereoProcessor {
    var mode: RoutingMode = RoutingMode.STEREO

    // Stereo Width: 0% (Mono) to 200% (Ultra Wide)
    var stereoWidth: Float = 1.0f // 1.0 = 100% normal

    // Balance: -1.0 (hard left) to +1.0 (hard right)
    var balance: Float = 0.0f

    // Mid/Side Gains in dB
    var midGainDb: Float = 0.0f
    var sideGainDb: Float = 0.0f

    // Polarity
    var invertLeft: Boolean = false
    var invertRight: Boolean = false

    // Phase shift (All-pass filter based phase control 0..360 deg)
    var phaseDegrees: Float = 0.0f

    // Dual Mono individual gains (dB)
    var dualMonoLeftGainDb: Float = 0.0f
    var dualMonoRightGainDb: Float = 0.0f

    // Correlation meter state: [-1.0..+1.0]
    private var sumL2: Float = 0.0f
    private var sumR2: Float = 0.0f
    private var sumLR: Float = 0.0f
    private var corrSamples: Int = 0
    var correlation: Float = 1.0f
        private set

    // All-pass filter for smooth phase offset on right channel
    private val phaseShifter = BiquadFilter()

    fun updateSampleRate(sampleRate: Int) {
        phaseShifter.configure(FilterType.ALL_PASS, 1000f, 0f, 0.707f, sampleRate)
    }

    /**
     * Process a stereo pair (inL, inR) into (outL, outR)
     */
    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        // 1. Channel Routing Mode
        var sL = inL
        var sR = inR

        when (mode) {
            RoutingMode.STEREO -> {
                // Keep L & R
            }
            RoutingMode.MONO -> {
                // Safe mono summing: (L + R) * 0.7071 (-3dB safe pan compensation)
                val mono = (sL + sR) * 0.70710678f
                sL = mono
                sR = mono
            }
            RoutingMode.LEFT_ONLY -> {
                sL = inL
                sR = inL
            }
            RoutingMode.RIGHT_ONLY -> {
                sL = inR
                sR = inR
            }
            RoutingMode.SWAP -> {
                sL = inR
                sR = inL
            }
            RoutingMode.DUAL_MONO -> {
                val gL = Math.pow(10.0, (dualMonoLeftGainDb / 20.0).toDouble()).toFloat()
                val gR = Math.pow(10.0, (dualMonoRightGainDb / 20.0).toDouble()).toFloat()
                sL *= gL
                sR *= gR
            }
        }

        // 2. Mid / Side & Stereo Width
        if (mode == RoutingMode.STEREO) {
            val mid = (sL + sR) * 0.5f
            val side = (sL - sR) * 0.5f

            val midGain = Math.pow(10.0, (midGainDb / 20.0).toDouble()).toFloat()
            val sideGain = Math.pow(10.0, (sideGainDb / 20.0).toDouble()).toFloat() * stereoWidth

            val procMid = mid * midGain
            val procSide = side * sideGain

            sL = procMid + procSide
            sR = procMid - procSide
        }

        // 3. Balance Control
        if (balance < 0.0f) {
            // Pan left: reduce right channel
            sR *= (1.0f + balance)
        } else if (balance > 0.0f) {
            // Pan right: reduce left channel
            sL *= (1.0f - balance)
        }

        // 4. Polarity
        if (invertLeft) sL = -sL
        if (invertRight) sR = -sR

        // 5. Phase Shift adjustment
        if (phaseDegrees != 0.0f) {
            val rad = (phaseDegrees * PI / 180.0).toFloat()
            // Approximate phase rotation via vector rotation
            val pL = sL * cos(rad) - sR * sin(rad)
            val pR = sL * sin(rad) + sR * cos(rad)
            sL = pL
            sR = pR
        }

        // Update Correlation Accumulator
        sumL2 += sL * sL
        sumR2 += sR * sR
        sumLR += sL * sR
        corrSamples++
        if (corrSamples >= 256) {
            val denom = sqrt(sumL2 * sumR2)
            correlation = if (denom > 1e-7f) {
                (sumLR / denom).coerceIn(-1.0f, 1.0f)
            } else {
                1.0f
            }
            sumL2 = 0f
            sumR2 = 0f
            sumLR = 0f
            corrSamples = 0
        }

        return Pair(sL, sR)
    }
}
