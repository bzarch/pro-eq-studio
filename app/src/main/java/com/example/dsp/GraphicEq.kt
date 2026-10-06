package com.example.dsp

enum class GraphicEqBandCount(val count: Int) {
    BANDS_10(10),
    BANDS_15(15),
    BANDS_31(31)
}

data class EqBandConfig(
    val id: Int,
    var frequency: Float,
    var gainDb: Float = 0.0f,
    var isEnabled: Boolean = true
)

/**
 * Professional Graphic Equalizer Engine supporting standard ISO 1/1 octave (10 bands),
 * 2/3 octave (15 bands), and 1/3 octave (31 bands).
 * Real-time biquad peaking filter bank with Q factor adapted per band count.
 */
class GraphicEq {
    var isBypassed: Boolean = false
    var preampDb: Float = 0.0f
    var bandMode: GraphicEqBandCount = GraphicEqBandCount.BANDS_10

    // Standard ISO 31-Band 1/3-octave center frequencies (Hz)
    val isoFrequencies31 = floatArrayOf(
        20f, 25f, 31.5f, 40f, 50f, 63f, 80f, 100f, 125f, 160f,
        200f, 250f, 315f, 400f, 500f, 630f, 800f, 1000f, 1250f, 1600f,
        2000f, 2500f, 3150f, 4000f, 5000f, 6300f, 8000f, 10000f, 12500f, 16000f, 20000f
    )

    // Standard ISO 15-Band 2/3-octave center frequencies (Hz)
    val isoFrequencies15 = floatArrayOf(
        25f, 40f, 63f, 100f, 160f, 250f, 400f, 630f, 1000f, 1600f,
        2500f, 4000f, 6300f, 10000f, 16000f
    )

    // Standard 10-Band 1-octave center frequencies (Hz)
    val isoFrequencies10 = floatArrayOf(
        31f, 63f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f
    )

    // Gains array for 31 bands (-15dB to +15dB)
    val bandGains31 = FloatArray(31) { 0.0f }
    val bandEnabled31 = BooleanArray(31) { true }

    // Max 31 biquad filters
    private val filters = Array(31) { BiquadFilter() }

    fun updateFilters(sampleRate: Int) {
        val freqs = when (bandMode) {
            GraphicEqBandCount.BANDS_10 -> isoFrequencies10
            GraphicEqBandCount.BANDS_15 -> isoFrequencies15
            GraphicEqBandCount.BANDS_31 -> isoFrequencies31
        }
        val q = when (bandMode) {
            GraphicEqBandCount.BANDS_10 -> 1.414f // 1 octave Q
            GraphicEqBandCount.BANDS_15 -> 2.14f  // 2/3 octave Q
            GraphicEqBandCount.BANDS_31 -> 4.318f // 1/3 octave Q
        }

        for (i in 0 until 31) {
            if (i < freqs.size) {
                filters[i].isEnabled = !isBypassed && bandEnabled31[i]
                filters[i].configure(
                    type = FilterType.PEAK,
                    frequency = freqs[i],
                    gainDb = bandGains31[i],
                    q = q,
                    sampleRate = sampleRate
                )
            } else {
                filters[i].isEnabled = false
            }
        }
    }

    fun setBandGain(bandIdx: Int, gainDb: Float, sampleRate: Int) {
        if (bandIdx in 0 until 31) {
            bandGains31[bandIdx] = gainDb.coerceIn(-15f, 15f)
            val freqs = getCurrentFrequencies()
            val q = getCurrentQ()
            if (bandIdx < freqs.size) {
                filters[bandIdx].configure(
                    type = FilterType.PEAK,
                    frequency = freqs[bandIdx],
                    gainDb = bandGains31[bandIdx],
                    q = q,
                    sampleRate = sampleRate
                )
            }
        }
    }

    fun getCurrentFrequencies(): FloatArray = when (bandMode) {
        GraphicEqBandCount.BANDS_10 -> isoFrequencies10
        GraphicEqBandCount.BANDS_15 -> isoFrequencies15
        GraphicEqBandCount.BANDS_31 -> isoFrequencies31
    }

    fun getCurrentQ(): Float = when (bandMode) {
        GraphicEqBandCount.BANDS_10 -> 1.414f
        GraphicEqBandCount.BANDS_15 -> 2.14f
        GraphicEqBandCount.BANDS_31 -> 4.318f
    }

    fun resetAll(sampleRate: Int) {
        for (i in 0 until 31) {
            bandGains31[i] = 0f
            bandEnabled31[i] = true
        }
        preampDb = 0f
        updateFilters(sampleRate)
    }

    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        if (isBypassed) return Pair(inL, inR)

        val preampLinear = Math.pow(10.0, (preampDb / 20.0).toDouble()).toFloat()
        var sL = inL * preampLinear
        var sR = inR * preampLinear

        val count = bandMode.count
        for (i in 0 until count) {
            sL = filters[i].processLeft(sL)
            sR = filters[i].processRight(sR)
        }
        return Pair(sL, sR)
    }

    /**
     * Compute exact theoretical combined frequency response (in dB) across active bands
     */
    fun evaluateGainAt(freq: Float, sampleRate: Int): Float {
        if (isBypassed) return 0f
        var totalDb = preampDb
        val count = bandMode.count
        for (i in 0 until count) {
            totalDb += filters[i].evaluateGainAt(freq, sampleRate)
        }
        return totalDb
    }
}
