package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.LatencyProfile
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSilver
import com.example.ui.theme.StudioSilverMuted
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val deviceName by viewModel.engine.currentDeviceName.collectAsState()
    val latencyMs by viewModel.engine.actualLatencyMs.collectAsState()
    val dspLoad by viewModel.engine.dspLoadPercent.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Audio Hardware Diagnostics Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("AUDIO HARDWARE TELEMETRY", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            DiagRow("Connected Audio Device", deviceName)
            DiagRow("Active DSP Sample Rate", "${viewModel.engine.targetSampleRate} Hz")
            DiagRow("Current Buffer Size", "${viewModel.engine.latencyProfile.bufferFrames} frames")
            DiagRow("Estimated Round-Trip Latency", "${"%.2f".format(latencyMs)} ms")
            DiagRow("Real-Time DSP CPU Load", "${"%.1f".format(dspLoad)} %")
            DiagRow("PCM Processing Format", "32-bit Floating Point (PCM_FLOAT)")
            DiagRow("Output Stream Channel Mode", "Stereo Interleaved (2 ch)")
        }

        // Sample Rate Selection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("TARGET SAMPLE RATE", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Filters and biquad transfer functions recalculate automatically.", color = StudioSilverMuted, fontSize = 10.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(44100, 48000, 88200, 96000).forEach { sr ->
                    val isSel = viewModel.engine.targetSampleRate == sr
                    Button(
                        onClick = { viewModel.setSampleRate(sr) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) MaroonPrimary else StudioCardBgElevated
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(32.dp).testTag("sample_rate_$sr")
                    ) {
                        Text(if (sr >= 1000) "${sr / 1000}k" else "$sr", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }

        // Buffer Size & Latency Profile
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("AUDIO BUFFER & LATENCY PROFILE", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Lower buffer reduces latency; higher buffer guarantees glitch-free stability.", color = StudioSilverMuted, fontSize = 10.sp)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LatencyProfile.values().forEach { profile ->
                    val isSel = viewModel.engine.latencyProfile == profile
                    Button(
                        onClick = { viewModel.setLatencyProfile(profile) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) MaroonPrimary else StudioCardBgElevated
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("latency_profile_${profile.name}")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(profile.description, fontSize = 11.sp, color = Color.White)
                            Text("${profile.bufferFrames} frames", fontSize = 11.sp, color = StudioSilverMuted)
                        }
                    }
                }
            }
        }

        // About & Safety Architecture
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("PRO EQ STUDIO v1.0", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                "Professional real-time digital audio workstation featuring 31-Band ISO Graphic EQ, 10-Band Parametric EQ, 48dB/oct Crossover, Dynamics, Mid/Side & True Floating-point Biquad DSP.",
                color = StudioSilverMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = StudioSilverMuted, fontSize = 10.sp)
        Text(value, color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
