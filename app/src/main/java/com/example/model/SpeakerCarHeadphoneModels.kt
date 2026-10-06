package com.example.model

enum class SpeakerDspCrossoverSlope(val dbPerOct: Int, val order: Int) {
    SLOPE_12DB(12, 2),
    SLOPE_24DB(24, 4),
    SLOPE_48DB(48, 8)
}

data class SpeakerChannelConfig(
    val name: String, // SUB, WOOFER, MIDRANGE, TWEETER
    var isEnabled: Boolean = true,
    var hpfFreq: Float = 20f,
    var lpfFreq: Float = 20000f,
    var gainDb: Float = 0.0f,
    var delayMs: Float = 0.0f,
    var isPhaseInverted: Boolean = false,
    var limiterThresholdDb: Float = -1.0f
)

data class CarAudioTimeAlignment(
    var frontLeftDelayMs: Float = 0.0f,
    var frontRightDelayMs: Float = 1.2f,
    var rearLeftDelayMs: Float = 2.4f,
    var rearRightDelayMs: Float = 2.8f,
    var subDelayMs: Float = 4.0f,
    var fader: Float = 0.0f, // -1 Front to +1 Rear
    var balance: Float = 0.0f, // -1 Left to +1 Right
    var listeningPosition: String = "Driver" // Driver, Front Center, All
)

data class HeadphoneDspProfile(
    var isCrossfeedEnabled: Boolean = true,
    var crossfeedAmount: Float = 0.35f,
    var bassManagementDb: Float = 2.5f,
    var stereoWidthPct: Float = 100f,
    var eqProfile: String = "Harman Target Reference"
)
