package com.example.presets

import com.example.model.PeqBandModel
import com.example.model.StudioPreset

object BuiltInPresets {
    val presets: List<StudioPreset> = listOf(
        StudioPreset(
            name = "Flat",
            category = "Standard"
        ),
        StudioPreset(
            name = "Neutral Studio",
            category = "Standard",
            bassDb = 0f,
            midDb = 0f,
            presenceDb = 0.5f,
            airDb = 1.0f
        ),
        StudioPreset(
            name = "Vocal Clarity",
            category = "Vocal",
            subDb = -3f,
            bassDb = -2f,
            lowMidDb = 1.0f,
            midDb = 3.0f,
            upperMidDb = 4.0f,
            presenceDb = 3.5f,
            airDb = 2.0f,
            graphicGains = createGains10(
                31 to -4f, 63 to -3f, 125 to -1f, 250 to 1f, 500 to 2f,
                1000 to 3.5f, 2000 to 4f, 4000 to 3f, 8000 to 2f, 16000 to 1f
            )
        ),
        StudioPreset(
            name = "Clear Vocal",
            category = "Vocal",
            subDb = -4f,
            bassDb = -3f,
            lowMidDb = 0.5f,
            midDb = 2.5f,
            upperMidDb = 4.5f,
            presenceDb = 4.0f,
            airDb = 3.0f
        ),
        StudioPreset(
            name = "Podcast & Speech",
            category = "Vocal",
            subDb = -6f,
            bassDb = -2f,
            lowMidDb = 1.5f,
            midDb = 3.5f,
            upperMidDb = 3.0f,
            presenceDb = 1.5f,
            airDb = -1f,
            compEnabled = true,
            compThresholdDb = -18f,
            compRatio = 3.5f
        ),
        StudioPreset(
            name = "Bass Boost",
            category = "Music",
            subDb = 5.0f,
            bassDb = 6.0f,
            lowMidDb = 2.5f,
            midDb = -1.0f,
            upperMidDb = 0f,
            presenceDb = 1.5f,
            airDb = 2.0f,
            graphicGains = createGains10(
                31 to 6f, 63 to 5.5f, 125 to 4f, 250 to 2f, 500 to 0f,
                1000 to -1f, 2000 to 0f, 4000 to 1f, 8000 to 2f, 16000 to 2f
            )
        ),
        StudioPreset(
            name = "Deep Bass",
            category = "Music",
            subDb = 7.0f,
            bassDb = 7.5f,
            lowMidDb = 3.0f,
            midDb = -2.0f,
            airDb = 1.5f
        ),
        StudioPreset(
            name = "Rock & Metal",
            category = "Music",
            subDb = 3.5f,
            bassDb = 4.0f,
            lowMidDb = -1.5f,
            midDb = -2.5f,
            upperMidDb = 2.0f,
            presenceDb = 4.0f,
            airDb = 4.5f
        ),
        StudioPreset(
            name = "Pop Master",
            category = "Music",
            subDb = 2.0f,
            bassDb = 3.0f,
            lowMidDb = 0.5f,
            midDb = 1.0f,
            upperMidDb = 2.5f,
            presenceDb = 3.5f,
            airDb = 3.0f,
            stereoWidth = 1.15f
        ),
        StudioPreset(
            name = "EDM Club",
            category = "Music",
            subDb = 6.0f,
            bassDb = 5.0f,
            lowMidDb = 1.0f,
            midDb = -1.5f,
            upperMidDb = 2.0f,
            presenceDb = 4.5f,
            airDb = 5.0f,
            stereoWidth = 1.25f,
            compEnabled = true,
            compThresholdDb = -15f,
            compRatio = 4f
        ),
        StudioPreset(
            name = "Acoustic & Unplugged",
            category = "Music",
            subDb = -1f,
            bassDb = 1.5f,
            lowMidDb = 1.0f,
            midDb = 0.5f,
            upperMidDb = 2.0f,
            presenceDb = 3.0f,
            airDb = 4.0f
        ),
        StudioPreset(
            name = "Classical & Orchestra",
            category = "Music",
            subDb = 1.5f,
            bassDb = 2.0f,
            lowMidDb = 0.5f,
            midDb = 0f,
            upperMidDb = 1.0f,
            presenceDb = 2.5f,
            airDb = 3.5f,
            stereoWidth = 1.2f
        ),
        StudioPreset(
            name = "Jazz Warmth",
            category = "Music",
            subDb = 2.0f,
            bassDb = 3.0f,
            lowMidDb = 2.0f,
            midDb = 1.0f,
            upperMidDb = -0.5f,
            presenceDb = 1.0f,
            airDb = 1.5f
        ),
        StudioPreset(
            name = "Movie & Cinema",
            category = "Media",
            subDb = 5.0f,
            bassDb = 4.0f,
            lowMidDb = 1.0f,
            midDb = 2.5f,
            upperMidDb = 3.0f,
            presenceDb = 3.5f,
            airDb = 3.0f,
            stereoWidth = 1.3f
        ),
        StudioPreset(
            name = "Gaming FPS Footsteps",
            category = "Media",
            subDb = -3.0f,
            bassDb = -1.0f,
            lowMidDb = 2.0f,
            midDb = 3.5f,
            upperMidDb = 5.0f,
            presenceDb = 6.0f,
            airDb = 2.0f
        ),
        // Speaker Tuning Presets
        StudioPreset(
            name = "Speaker 4 Inch",
            category = "Speaker",
            subDb = -8.0f,
            bassDb = -2.0f,
            lowMidDb = 2.5f,
            midDb = 2.0f,
            upperMidDb = 1.0f,
            presenceDb = 2.0f,
            airDb = 1.0f,
            xoverEnabled = true,
            xoverLowFreq = 90f
        ),
        StudioPreset(
            name = "Speaker 6 Inch",
            category = "Speaker",
            subDb = -4.0f,
            bassDb = 1.0f,
            lowMidDb = 2.0f,
            midDb = 1.0f,
            upperMidDb = 1.5f,
            presenceDb = 2.5f,
            airDb = 2.0f
        ),
        StudioPreset(
            name = "Speaker 8 Inch",
            category = "Speaker",
            subDb = 2.0f,
            bassDb = 3.0f,
            lowMidDb = 1.0f,
            midDb = 0.5f,
            upperMidDb = 1.5f,
            presenceDb = 2.0f,
            airDb = 2.5f
        ),
        StudioPreset(
            name = "Fullrange Box",
            category = "Speaker",
            subDb = -2f,
            bassDb = 2f,
            lowMidDb = 1f,
            midDb = 1.5f,
            upperMidDb = 2f,
            presenceDb = 3f,
            airDb = 3.5f
        ),
        StudioPreset(
            name = "Mid Gacor (Stage Vocal)",
            category = "Speaker",
            subDb = -6f,
            bassDb = -2f,
            lowMidDb = 3.5f,
            midDb = 6.0f,
            upperMidDb = 5.5f,
            presenceDb = 3.0f,
            airDb = 0f
        ),
        StudioPreset(
            name = "TV Speaker Enhancer",
            category = "Speaker",
            subDb = -7f,
            bassDb = -3f,
            lowMidDb = 2.0f,
            midDb = 4.0f,
            upperMidDb = 3.5f,
            presenceDb = 2.5f,
            airDb = 1.0f
        ),
        StudioPreset(
            name = "Headphone Reference",
            category = "Headphone",
            subDb = 1.5f,
            bassDb = 1.0f,
            lowMidDb = 0f,
            midDb = 0f,
            upperMidDb = 0.5f,
            presenceDb = 1.0f,
            airDb = 1.5f,
            stereoWidth = 1.0f
        ),
        StudioPreset(
            name = "PA System Arena",
            category = "PA",
            subDb = 3.0f,
            bassDb = 2.5f,
            lowMidDb = -1.0f,
            midDb = 1.5f,
            upperMidDb = 3.0f,
            presenceDb = 4.0f,
            airDb = 2.5f,
            limiterEnabled = true,
            limiterThresholdDb = -1.0f
        )
    )

    private fun createGains10(vararg pairs: Pair<Int, Float>): List<Float> {
        val list = MutableList(31) { 0f }
        val freqs10 = listOf(31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
        for ((freq, gain) in pairs) {
            val idx = freqs10.indexOf(freq)
            if (idx != -1) {
                list[idx] = gain
            }
        }
        return list
    }
}
