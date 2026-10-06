package com.example.model

import com.example.dsp.CrossoverMode
import com.example.dsp.CrossoverSlope
import com.example.dsp.FilterType
import com.example.dsp.GraphicEqBandCount
import com.example.dsp.RoutingMode

/**
 * Data structures for Preset Serialization (JSON compatible)
 */
data class PeqBandModel(
    val id: Int,
    val enabled: Boolean = true,
    val type: String = "PEAK",
    val freq: Float = 1000f,
    val gainDb: Float = 0f,
    val q: Float = 1.0f
)

data class StudioPreset(
    val name: String,
    val category: String = "General",
    val version: Int = 1,
    val preampDb: Float = 0f,
    val inputGainDb: Float = 0f,
    val masterGainDb: Float = 0f,
    val graphicBandCount: Int = 10,
    val graphicGains: List<Float> = List(31) { 0f },
    val peqBands: List<PeqBandModel> = emptyList(),
    // Quick Tone
    val subDb: Float = 0f,
    val bassDb: Float = 0f,
    val lowMidDb: Float = 0f,
    val midDb: Float = 0f,
    val upperMidDb: Float = 0f,
    val presenceDb: Float = 0f,
    val airDb: Float = 0f,
    // Routing & Stereo
    val routingMode: String = "STEREO",
    val stereoWidth: Float = 1.0f,
    val balance: Float = 0f,
    val midGainDb: Float = 0f,
    val sideGainDb: Float = 0f,
    // Dynamics
    val compEnabled: Boolean = false,
    val compThresholdDb: Float = -20f,
    val compRatio: Float = 4.0f,
    val compAttackMs: Float = 20f,
    val compReleaseMs: Float = 150f,
    val compMakeupDb: Float = 0f,
    val limiterEnabled: Boolean = true,
    val limiterThresholdDb: Float = -0.5f,
    val limiterCeilingDb: Float = -0.1f,
    // Crossover
    val xoverEnabled: Boolean = false,
    val xoverMode: String = "TWO_WAY",
    val xoverSlope: String = "SLOPE_24DB",
    val xoverLowFreq: Float = 250f,
    val xoverHighFreq: Float = 3500f,
    val xoverLowGainDb: Float = 0f,
    val xoverMidGainDb: Float = 0f,
    val xoverHighGainDb: Float = 0f
)
