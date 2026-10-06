package com.example.dsp

enum class CrossoverSlope(val dbPerOct: Int, val order: Int) {
    SLOPE_6DB(6, 1),
    SLOPE_12DB(12, 2),
    SLOPE_18DB(18, 3),
    SLOPE_24DB(24, 4),
    SLOPE_48DB(48, 8)
}

enum class CrossoverMode {
    TWO_WAY,
    THREE_WAY
}

/**
 * Multi-band Linkwitz-Riley / Butterworth Crossover Engine with configurable slopes.
 * Splits incoming audio into 2-Way (Low / High) or 3-Way (Low / Mid / High) bands,
 * applies band gain and phase/polarity, and sums accurately.
 */
class CrossoverEngine {
    var isEnabled: Boolean = false
    var mode: CrossoverMode = CrossoverMode.TWO_WAY
    var slope: CrossoverSlope = CrossoverSlope.SLOPE_24DB

    // Crossover frequencies
    var lowFreq: Float = 250f   // Low-to-High (2-way) or Low-to-Mid (3-way)
    var highFreq: Float = 3500f // Mid-to-High (3-way)

    // Band Gains (dB)
    var lowGainDb: Float = 0f
    var midGainDb: Float = 0f
    var highGainDb: Float = 0f

    // Band Mutes
    var muteLow: Boolean = false
    var muteMid: Boolean = false
    var muteHigh: Boolean = false

    // Band Inverts
    var invertLow: Boolean = false
    var invertMid: Boolean = false
    var invertHigh: Boolean = false

    // Cascade filters for steep slopes (up to 4 biquads = 8th order / 48dB/oct)
    private val lowLpf = Array(4) { BiquadFilter() }
    private val highHpf = Array(4) { BiquadFilter() }

    // 3-Way mid filters
    private val midHpf = Array(4) { BiquadFilter() }
    private val midLpf = Array(4) { BiquadFilter() }

    fun update(sampleRate: Int) {
        val stages = (slope.order / 2).coerceAtLeast(1)
        val q = 0.7071f // Butterworth Q for flat passband

        for (i in 0 until 4) {
            val stageActive = i < stages
            lowLpf[i].isEnabled = stageActive
            lowLpf[i].configure(FilterType.LOW_PASS, lowFreq, 0f, q, sampleRate)

            if (mode == CrossoverMode.TWO_WAY) {
                highHpf[i].isEnabled = stageActive
                highHpf[i].configure(FilterType.HIGH_PASS, lowFreq, 0f, q, sampleRate)
            } else {
                // 3-Way
                highHpf[i].isEnabled = stageActive
                highHpf[i].configure(FilterType.HIGH_PASS, highFreq, 0f, q, sampleRate)

                midHpf[i].isEnabled = stageActive
                midHpf[i].configure(FilterType.HIGH_PASS, lowFreq, 0f, q, sampleRate)

                midLpf[i].isEnabled = stageActive
                midLpf[i].configure(FilterType.LOW_PASS, highFreq, 0f, q, sampleRate)
            }
        }
    }

    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        if (!isEnabled) return Pair(inL, inR)

        val stages = (slope.order / 2).coerceAtLeast(1)

        // Process LOW band
        var lowL = inL
        var lowR = inR
        for (i in 0 until stages) {
            lowL = lowLpf[i].processLeft(lowL)
            lowR = lowLpf[i].processRight(lowR)
        }
        if (muteLow) { lowL = 0f; lowR = 0f }
        if (invertLow) { lowL = -lowL; lowR = -lowR }
        val gLow = Math.pow(10.0, (lowGainDb / 20.0).toDouble()).toFloat()
        lowL *= gLow; lowR *= gLow

        // Process HIGH band
        var highL = inL
        var highR = inR
        for (i in 0 until stages) {
            highL = highHpf[i].processLeft(highL)
            highR = highHpf[i].processRight(highR)
        }
        if (muteHigh) { highL = 0f; highR = 0f }
        if (invertHigh) { highL = -highL; highR = -highR }
        val gHigh = Math.pow(10.0, (highGainDb / 20.0).toDouble()).toFloat()
        highL *= gHigh; highR *= gHigh

        return if (mode == CrossoverMode.TWO_WAY) {
            Pair(lowL + highL, lowR + highR)
        } else {
            // Process MID band
            var midL = inL
            var midR = inR
            for (i in 0 until stages) {
                midL = midHpf[i].processLeft(midL)
                midR = midHpf[i].processRight(midR)
                midL = midLpf[i].processLeft(midL)
                midR = midLpf[i].processRight(midR)
            }
            if (muteMid) { midL = 0f; midR = 0f }
            if (invertMid) { midL = -midL; midR = -midR }
            val gMid = Math.pow(10.0, (midGainDb / 20.0).toDouble()).toFloat()
            midL *= gMid; midR *= gMid

            Pair(lowL + midL + highL, lowR + midR + highR)
        }
    }
}
