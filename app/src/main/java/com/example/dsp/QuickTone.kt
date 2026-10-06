package com.example.dsp

/**
 * 7-Band Studio Quick Tone Controller:
 * Sub (40Hz), Bass (100Hz), Low-Mid (350Hz), Mid (1kHz), Upper-Mid (3kHz), Presence (6kHz), Air/Treble (12kHz).
 */
class QuickTone {
    var isEnabled: Boolean = true

    var subDb: Float = 0.0f
    var bassDb: Float = 0.0f
    var lowMidDb: Float = 0.0f
    var midDb: Float = 0.0f
    var upperMidDb: Float = 0.0f
    var presenceDb: Float = 0.0f
    var airDb: Float = 0.0f

    private val subFilter = BiquadFilter()
    private val bassFilter = BiquadFilter()
    private val lowMidFilter = BiquadFilter()
    private val midFilter = BiquadFilter()
    private val upperMidFilter = BiquadFilter()
    private val presenceFilter = BiquadFilter()
    private val airFilter = BiquadFilter()

    fun update(sampleRate: Int) {
        subFilter.isEnabled = isEnabled
        subFilter.configure(FilterType.LOW_SHELF, 45f, subDb, 0.707f, sampleRate)

        bassFilter.isEnabled = isEnabled
        bassFilter.configure(FilterType.PEAK, 100f, bassDb, 1.0f, sampleRate)

        lowMidFilter.isEnabled = isEnabled
        lowMidFilter.configure(FilterType.PEAK, 350f, lowMidDb, 1.2f, sampleRate)

        midFilter.isEnabled = isEnabled
        midFilter.configure(FilterType.PEAK, 1000f, midDb, 1.0f, sampleRate)

        upperMidFilter.isEnabled = isEnabled
        upperMidFilter.configure(FilterType.PEAK, 3200f, upperMidDb, 1.2f, sampleRate)

        presenceFilter.isEnabled = isEnabled
        presenceFilter.configure(FilterType.PEAK, 6000f, presenceDb, 1.4f, sampleRate)

        airFilter.isEnabled = isEnabled
        airFilter.configure(FilterType.HIGH_SHELF, 12000f, airDb, 0.707f, sampleRate)
    }

    fun process(inL: Float, inR: Float): Pair<Float, Float> {
        if (!isEnabled) return Pair(inL, inR)
        var l = subFilter.processLeft(inL)
        var r = subFilter.processRight(inR)

        l = bassFilter.processLeft(l)
        r = bassFilter.processRight(r)

        l = lowMidFilter.processLeft(l)
        r = lowMidFilter.processRight(r)

        l = midFilter.processLeft(l)
        r = midFilter.processRight(r)

        l = upperMidFilter.processLeft(l)
        r = upperMidFilter.processRight(r)

        l = presenceFilter.processLeft(l)
        r = presenceFilter.processRight(r)

        l = airFilter.processLeft(l)
        r = airFilter.processRight(r)

        return Pair(l, r)
    }

    fun applyPreset(presetName: String, sampleRate: Int) {
        when (presetName) {
            "Warm" -> {
                subDb = 2.0f; bassDb = 3.5f; lowMidDb = 1.5f; midDb = 0f; upperMidDb = -1f; presenceDb = -2f; airDb = -2.5f
            }
            "Bright" -> {
                subDb = -1f; bassDb = -1f; lowMidDb = 0f; midDb = 1f; upperMidDb = 2.5f; presenceDb = 4.0f; airDb = 4.5f
            }
            "Vocal" -> {
                subDb = -3f; bassDb = -2f; lowMidDb = 1.0f; midDb = 2.5f; upperMidDb = 3.5f; presenceDb = 3.0f; airDb = 1.5f
            }
            "Mid Focus" -> {
                subDb = -2f; bassDb = -1f; lowMidDb = 2.0f; midDb = 4.0f; upperMidDb = 3.0f; presenceDb = 0.5f; airDb = -1f
            }
            "Presence" -> {
                subDb = 0f; bassDb = 0.5f; lowMidDb = 0f; midDb = 1.5f; upperMidDb = 3.0f; presenceDb = 5.0f; airDb = 3.0f
            }
            else -> {
                // Neutral
                subDb = 0f; bassDb = 0f; lowMidDb = 0f; midDb = 0f; upperMidDb = 0f; presenceDb = 0f; airDb = 0f
            }
        }
        update(sampleRate)
    }
}
