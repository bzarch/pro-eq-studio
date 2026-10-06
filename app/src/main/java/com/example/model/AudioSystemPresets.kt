package com.example.model

enum class AudioSystemCategory(val title: String, val subtitle: String) {
    IEM_AUDIOPHILE("Audiophile IEM / Earphones", "Harman Curve, High Clarity, Pinpoint Soundstage"),
    TWS_EARBUDS("TWS / Wireless Earbuds", "Bluetooth Codec Comp, Bass Punch, Vocal Lift"),
    OVER_EAR_HEADSET("Studio Headphones / Headset", "Binaural Crossfeed, Flat Master, Anti-Fatigue"),
    ROOM_BEDROOM("Kamar Tidur / Mini Studio (Nearfield)", "Room Acoustic Modes, Desk Reflection Control"),
    HIGH_END_HIFI("Sound System Mewah / Hi-Fi Living Room", "Audiophile Transparency, True Sub-Bass, Ultra-Low THD"),
    PA_STAGE_OUTDOOR("Pro Audio PA / Panggung / Lapangan", "High SPL Limiting, 48dB Crossover, Horn EQ"),
    CAR_AUDIO_DSP("Car Audio DSP Management", "Time Alignment, Off-Center Staging, Subwoofer Control"),
    PARTY_SPEAKER("Party Box / Bluetooth Boombox", "Maximum Loudness, Bass Boost, Dynamic Excursion Clamping")
}

data class AudioProfileDetail(
    val category: AudioSystemCategory,
    val presetName: String,
    val bassBoostDb: Float,
    val subHarmonics: Float,
    val midClarityDb: Float,
    val airPresenceDb: Float,
    val crossfeedEnabled: Boolean,
    val crossfeedAmount: Float,
    val limiterCeilingDb: Float,
    val crossoverHpfHz: Float,
    val stereoWidthPct: Float,
    val description: String
)
