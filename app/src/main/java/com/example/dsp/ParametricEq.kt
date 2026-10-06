package com.example.dsp

data class PeqBand(
    val id: Int,
    var isEnabled: Boolean = true,
    var type: FilterType = FilterType.PEAK,
    var frequency: Float = 1000f,
    var gainDb: Float = 0.0f,
    var q: Float = 1.0f
)

/**
 * 10-Band Parametric Equalizer Engine.
 * Each band supports fully independent Frequency (20Hz - 20kHz), Gain (-15dB - +15dB),
 * Q factor (0.1 - 20), and 8 filter topologies (Peak, Low Shelf, High Shelf, LPF, HPF, BPF, Notch, All Pass).
 */
class ParametricEq {
    var isBypassed: Boolean = false

    val bands = Array(10) { index ->
        val defaultFreq = when (index) {
            0 -> 60f
            1 -> 120f
            2 -> 250f
            3 -> 500f
            4 -> 1000f
            5 -> 2000f
            6 -> 4000f
            7 -> 8000f
            8 -> 12000f
            else -> 16000f
        }
        val defaultType = when (index) {
            0 -> FilterType.LOW_SHELF
            9 -> FilterType.HIGH_SHELF
            else -> FilterType.PEAK
        }
        PeqBand(
            id = index,
            isEnabled = true,
            type = defaultType,
            frequency = defaultFreq,
            gainDb = 0.0f,
            q = 1.0f
        )
    }

    private val filters = Array(10) { BiquadFilter() }

    fun updateBand(index: Int, sampleRate: Int) {
        if (index in 0 until 10) {
            val b = bands[index]
            filters[index].isEnabled = !isBypassed && b.isEnabled
            filters[index].configure(
                type = b.type,
                frequency = b.frequency,
                gainDb = b.gainDb,
                q = b.q,
                sampleRate = sampleRate
            )
        }
    }

    fun updateAll(sampleRate: Int) {
        for (i in 0 until 10) {
            updateBand(i, sampleRate)
        }
    }

    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        if (isBypassed) return Pair(inL, inR)
        var sL = inL
        var sR = inR
        for (i in 0 until 10) {
            sL = filters[i].processLeft(sL)
            sR = filters[i].processRight(sR)
        }
        return Pair(sL, sR)
    }

    fun evaluateGainAt(freq: Float, sampleRate: Int): Float {
        if (isBypassed) return 0f
        var totalDb = 0f
        for (i in 0 until 10) {
            totalDb += filters[i].evaluateGainAt(freq, sampleRate)
        }
        return totalDb
    }

    fun resetAll(sampleRate: Int) {
        for (i in 0 until 10) {
            bands[i].gainDb = 0f
            bands[i].isEnabled = true
        }
        updateAll(sampleRate)
    }
}
