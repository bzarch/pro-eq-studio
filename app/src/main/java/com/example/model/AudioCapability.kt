package com.example.model

enum class CapabilityStatus {
    AVAILABLE,
    LIMITED,
    UNAVAILABLE
}

data class CapabilityItem(
    val title: String,
    val status: CapabilityStatus,
    val detail: String,
    val supportedApi: String = ""
)

data class DeviceAudioProfile(
    val osVersion: String,
    val apiLevel: Int,
    val manufacturer: String,
    val model: String,
    val defaultSampleRate: Int,
    val optimalBufferSize: Int,
    val connectedOutput: String,
    val isSystemDspCapable: Boolean,
    val capabilities: List<CapabilityItem>
)
