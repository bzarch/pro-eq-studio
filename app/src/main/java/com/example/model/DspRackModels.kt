package com.example.model

enum class ModuleType {
    PREAMP,
    GRAPHIC_EQ,
    PARAMETRIC_EQ,
    DYNAMIC_EQ,
    BASS_PROCESSOR,
    MID_PROCESSOR,
    TREBLE_AIR,
    EXCITER,
    SATURATION,
    NOISE_GATE,
    DE_ESSER,
    COMPRESSOR,
    MULTIBAND_COMP,
    TRANSIENT_SHAPER,
    STEREO_WIDENER,
    MONO_ENGINE,
    DELAY,
    REVERB,
    CHORUS,
    FLANGER,
    PHASER,
    TREMOLO,
    PITCH_SHIFTER,
    CROSSOVER,
    LIMITER,
    SOFT_CLIPPER
}

data class DspModuleInfo(
    val id: String,
    val type: ModuleType,
    val name: String,
    var isEnabled: Boolean = true,
    var isBypassed: Boolean = false
)
