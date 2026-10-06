package com.example.model

enum class InputSourceType(val displayName: String, val category: String) {
    SYSTEM_AUDIO("Android System Audio", "SYSTEM"),
    AUX_LINE_IN("AUX / Analog Line-In", "ANALOG"),
    BLUETOOTH_IN("Bluetooth Audio Sink", "WIRELESS"),
    USB_AUDIO_IN("USB Audio Class Interface", "DIGITAL"),
    INTERNAL_PLAYER("Internal Media Player", "STORAGE"),
    TEST_SIGNAL_GEN("Signal Calibration Generator", "SYNTH"),
    SYSTEM_CAPTURE("Official Audio Capture", "SYSTEM")
}

data class SourceChannelStrip(
    val sourceType: InputSourceType,
    var isEnabled: Boolean = true,
    var isMuted: Boolean = false,
    var isSolo: Boolean = false,
    var gainDb: Float = 0.0f,
    var trimDb: Float = 0.0f,
    var pan: Float = 0.0f, // -1.0 to +1.0
    var isPhaseInverted: Boolean = false,
    var isMonoSum: Boolean = false,
    var status: CapabilityStatus = CapabilityStatus.AVAILABLE,
    var statusReason: String = "Ready"
)

enum class OutputDestinationType(val displayName: String) {
    INTERNAL_SPEAKER("Phone Internal Speaker"),
    WIRED_HEADSET("Wired Headphones / Line Out"),
    BLUETOOTH_A2DP("Bluetooth Audio Device"),
    USB_DAC_INTERFACE("USB DAC / Soundcard"),
    HDMI_EXTERNAL("HDMI / External Monitor"),
    CAR_AUDIO_PROFILE("Car Audio DSP Profile")
}

data class OutputRouteState(
    val destinationType: OutputDestinationType,
    var isConnected: Boolean = false,
    var detectedSampleRate: Int = 48000,
    var channelCount: Int = 2,
    var estimatedLatencyMs: Float = 12.0f,
    var isDspEnabled: Boolean = true,
    var outputGainDb: Float = 0.0f,
    var balance: Float = 0.0f,
    var isLimiterActive: Boolean = true,
    var profilePreset: String = "Neutral"
)

data class MatrixRoutingPoint(
    val sourceIndex: Int,
    val busIndex: Int,
    var sendGainDb: Float = 0.0f,
    var isMuted: Boolean = false,
    var isPreFader: Boolean = false
)
