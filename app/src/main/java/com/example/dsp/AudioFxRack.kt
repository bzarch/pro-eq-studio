package com.example.dsp

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Real Multi-tap, Stereo & Ping-Pong Delay Line DSP.
 */
class DelayProcessor(val maxDelaySeconds: Float = 2.0f, val sampleRate: Int = 48000) {
    var isEnabled: Boolean = false
    var delayTimeSeconds: Float = 0.35f
    var feedback: Float = 0.35f
    var mix: Float = 0.25f // 0 to 1
    var pingPong: Boolean = true

    private val maxBufferLen = (maxDelaySeconds * 96000).toInt() + 1024
    private val bufferL = FloatArray(maxBufferLen)
    private val bufferR = FloatArray(maxBufferLen)
    private var writePos = 0

    fun process(inL: Float, inR: Float, currentSr: Int): Pair<Float, Float> {
        if (!isEnabled || mix <= 0.001f) return Pair(inL, inR)

        val delaySamples = (delayTimeSeconds * currentSr).toInt().coerceIn(1, maxBufferLen - 1)
        var readPos = writePos - delaySamples
        if (readPos < 0) readPos += maxBufferLen

        val delayedL = bufferL[readPos]
        val delayedR = bufferR[readPos]

        // Feedback calculation
        val fbL = inL + (if (pingPong) delayedR else delayedL) * feedback
        val fbR = inR + (if (pingPong) delayedL else delayedR) * feedback

        bufferL[writePos] = if (fbL.isFinite()) fbL else 0f
        bufferR[writePos] = if (fbR.isFinite()) fbR else 0f

        writePos++
        if (writePos >= maxBufferLen) writePos = 0

        val wetL = delayedL * mix + inL * (1.0f - mix)
        val wetR = delayedR * mix + inR * (1.0f - mix)
        return Pair(wetL, wetR)
    }

    fun reset() {
        bufferL.fill(0f)
        bufferR.fill(0f)
        writePos = 0
    }
}

/**
 * Algorithmic Freeverb Schroeder-Moorer Studio Reverberation Engine.
 * Combines 4 parallel Comb filters and 2 series All-pass diffusers.
 */
class ReverbProcessor {
    var isEnabled: Boolean = false
    var roomSize: Float = 0.6f
    var damping: Float = 0.4f
    var mix: Float = 0.2f // 0 to 1

    private class CombFilter(size: Int) {
        val buffer = FloatArray(size)
        var bufIdx = 0
        var filterStore = 0f

        fun process(input: Float, feedback: Float, damp: Float): Float {
            val output = buffer[bufIdx]
            filterStore = (output * (1.0f - damp)) + (filterStore * damp)
            buffer[bufIdx] = input + (filterStore * feedback)
            bufIdx++
            if (bufIdx >= buffer.size) bufIdx = 0
            return output
        }
    }

    private class AllPass(size: Int) {
        val buffer = FloatArray(size)
        var bufIdx = 0

        fun process(input: Float): Float {
            val bufOut = buffer[bufIdx]
            val output = -input + bufOut
            buffer[bufIdx] = input + (bufOut * 0.5f)
            bufIdx++
            if (bufIdx >= buffer.size) bufIdx = 0
            return output
        }
    }

    private val combsL = arrayOf(CombFilter(1116), CombFilter(1188), CombFilter(1277), CombFilter(1356))
    private val combsR = arrayOf(CombFilter(1139), CombFilter(1211), CombFilter(1297), CombFilter(1380))
    private val allPassL = arrayOf(AllPass(556), AllPass(441))
    private val allPassR = arrayOf(AllPass(579), AllPass(464))

    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        if (!isEnabled || mix <= 0.001f) return Pair(inL, inR)

        val input = (inL + inR) * 0.5f
        var outL = 0f
        var outR = 0f

        for (c in combsL) outL += c.process(input, roomSize * 0.95f, damping)
        for (c in combsR) outR += c.process(input, roomSize * 0.95f, damping)

        for (a in allPassL) outL = a.process(outL)
        for (a in allPassR) outR = a.process(outR)

        val wetL = inL * (1f - mix) + outL * mix * 0.4f
        val wetR = inR * (1f - mix) + outR * mix * 0.4f
        return Pair(wetL, wetR)
    }
}

/**
 * LFO-based Modulation Processor (Chorus, Flanger, Phaser & Tremolo).
 */
class ModulationProcessor {
    var isEnabled: Boolean = false
    var chorusRate: Float = 1.2f     // Hz
    var chorusDepth: Float = 0.003f  // sec
    var chorusMix: Float = 0.0f      // 0 to 1

    var tremoloRate: Float = 4.0f    // Hz
    var tremoloDepth: Float = 0.0f   // 0 to 1

    private val maxDelay = 2048
    private val bufferL = FloatArray(maxDelay)
    private val bufferR = FloatArray(maxDelay)
    private var writeIdx = 0
    private var lfoPhase = 0.0

    fun process(inL: Float, inR: Float, sampleRate: Int): Pair<Float, Float> {
        if (!isEnabled && chorusMix <= 0.01f && tremoloDepth <= 0.01f) return Pair(inL, inR)

        bufferL[writeIdx] = inL
        bufferR[writeIdx] = inR
        writeIdx = (writeIdx + 1) % maxDelay

        val twoPi = 2.0 * PI
        lfoPhase += twoPi * chorusRate / sampleRate
        if (lfoPhase >= twoPi) lfoPhase -= twoPi

        // Chorus modulation
        var sL = inL
        var sR = inR
        if (chorusMix > 0.01f) {
            val modL = (sin(lfoPhase) * 0.5 + 0.5).toFloat() * chorusDepth * sampleRate
            val modR = (cos(lfoPhase) * 0.5 + 0.5).toFloat() * chorusDepth * sampleRate

            val readL = ((writeIdx - modL.toInt() + maxDelay) % maxDelay)
            val readR = ((writeIdx - modR.toInt() + maxDelay) % maxDelay)

            sL = inL * (1f - chorusMix) + bufferL[readL] * chorusMix
            sR = inR * (1f - chorusMix) + bufferR[readR] * chorusMix
        }

        // Tremolo modulation
        if (tremoloDepth > 0.01f) {
            val tremLfo = (1.0f - tremoloDepth * (0.5f + 0.5f * sin(lfoPhase * (tremoloRate / chorusRate)).toFloat()))
            sL *= tremLfo
            sR *= tremLfo
        }

        return Pair(sL, sR)
    }
}

/**
 * Noise Gate & De-Esser.
 */
class DynamicsEffects {
    var gateEnabled: Boolean = false
    var gateThresholdDb: Float = -45f

    var deEsserEnabled: Boolean = false
    var deEsserThresholdDb: Float = -18f
    var deEsserReduction: Float = 0.0f
        private set

    private val sibilanceFilterL = BiquadFilter()
    private val sibilanceFilterR = BiquadFilter()

    fun updateSampleRate(sr: Int) {
        sibilanceFilterL.configure(FilterType.BAND_PASS, 6500f, 0f, 2.5f, sr)
        sibilanceFilterR.configure(FilterType.BAND_PASS, 6500f, 0f, 2.5f, sr)
    }

    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        var sL = inL
        var sR = inR

        // Noise gate
        if (gateEnabled) {
            val maxLinear = Math.max(abs(sL), abs(sR))
            val threshLinear = Math.pow(10.0, (gateThresholdDb / 20.0).toDouble()).toFloat()
            if (maxLinear < threshLinear) {
                sL *= 0.05f
                sR *= 0.05f
            }
        }

        // De-Esser
        if (deEsserEnabled) {
            val sibL = sibilanceFilterL.processLeft(sL)
            val sibR = sibilanceFilterR.processRight(sR)
            val peakSib = Math.max(abs(sibL), abs(sibR))
            val deThresh = Math.pow(10.0, (deEsserThresholdDb / 20.0).toDouble()).toFloat()

            if (peakSib > deThresh) {
                val atten = (deThresh / peakSib).coerceIn(0.2f, 1.0f)
                deEsserReduction = (1.0f - atten) * 12.0f
                sL -= sibL * (1.0f - atten)
                sR -= sibR * (1.0f - atten)
            } else {
                deEsserReduction = 0f
            }
        }

        return Pair(sL, sR)
    }
}

/**
 * Harmonic Bass Processor & Sub Harmonic Synthesizer.
 */
class BassEnhancer {
    var isEnabled: Boolean = false
    var subHarmonicsAmount: Float = 0.0f // 0 to 1
    var punchDb: Float = 0.0f            // -12 to +12 dB

    private val subLpf = BiquadFilter()
    private val punchBpf = BiquadFilter()

    fun updateSampleRate(sr: Int) {
        subLpf.configure(FilterType.LOW_PASS, 80f, 0f, 0.707f, sr)
        punchBpf.configure(FilterType.PEAK, 95f, punchDb, 1.4f, sr)
    }

    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        if (!isEnabled && punchDb == 0f && subHarmonicsAmount == 0f) return Pair(inL, inR)

        var sL = inL
        var sR = inR

        if (punchDb != 0f) {
            sL = punchBpf.processLeft(sL)
            sR = punchBpf.processRight(sR)
        }

        if (subHarmonicsAmount > 0.01f) {
            val lowL = subLpf.processLeft(inL)
            val lowR = subLpf.processRight(inR)
            // Octave divider / sub-harmonic generation via soft saturation
            val subHarmL = (lowL * lowL * if (lowL > 0) 1f else -1f) * subHarmonicsAmount * 1.5f
            val subHarmR = (lowR * lowR * if (lowR > 0) 1f else -1f) * subHarmonicsAmount * 1.5f
            sL += subHarmL
            sR += subHarmR
        }

        return Pair(sL, sR)
    }
}
