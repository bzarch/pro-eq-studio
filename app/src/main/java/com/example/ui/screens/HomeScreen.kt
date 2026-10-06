package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSourceType
import com.example.ui.components.EqCurveVisualizer
import com.example.ui.components.FftSpectrumVisualizer
import com.example.ui.components.StudioMeterBar
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSilver
import com.example.ui.theme.StudioSilverMuted
import com.example.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val isPowerOn by viewModel.isPowerOn.collectAsState()
    val isBypassed by viewModel.isBypassed.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()
    val abSlot by viewModel.currentABSlot.collectAsState()

    val inMeter by viewModel.engine.inputMeterFlow.collectAsState()
    val outMeter by viewModel.engine.outputMeterFlow.collectAsState()
    val fftMags by viewModel.engine.fftMagnitudes.collectAsState()
    val deviceName by viewModel.engine.currentDeviceName.collectAsState()
    val latencyMs by viewModel.engine.actualLatencyMs.collectAsState()
    val dspLoad by viewModel.engine.dspLoadPercent.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Master Status & Hardware Info Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .background(if (isPowerOn) MeterGreen else Color.DarkGray, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPowerOn) "DSP ENGINE ACTIVE" else "DSP ENGINE STANDBY",
                        color = if (isPowerOn) MeterGreen else StudioSilverMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$deviceName • ${viewModel.engine.targetSampleRate} Hz • Latency: ${"%.1f".format(latencyMs)} ms",
                    color = StudioSilverMuted,
                    fontSize = 10.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { viewModel.toggleABCompare() },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCardBgElevated),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(32.dp).testTag("ab_compare_button")
                ) {
                    Text("A/B [$abSlot]", fontSize = 11.sp, color = StudioSilver)
                }

                Button(
                    onClick = { viewModel.toggleBypass() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBypassed) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(32.dp).testTag("bypass_button")
                ) {
                    Text(if (isBypassed) "BYPASS ON" else "BYPASS", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // System DSP Global Processing Banner Card (Spotify, YouTube, Games)
        val isSystemDspOn by viewModel.isSystemDspActive.collectAsState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSystemDspOn) MaroonPrimary.copy(alpha = 0.25f) else StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, if (isSystemDspOn) MaroonPrimaryLight else StudioBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .background(if (isSystemDspOn) MeterGreen else Color.DarkGray, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSystemDspOn) "SYSTEM DSP ACTIVE" else "SYSTEM DSP STANDBY",
                        color = if (isSystemDspOn) MeterGreen else StudioSilverMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Processes background audio from Spotify, YouTube, Games & Music Players.",
                    color = StudioSilverMuted,
                    fontSize = 9.sp
                )
            }

            Button(
                onClick = { viewModel.toggleSystemDsp() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSystemDspOn) MaroonPrimary else StudioCardBgElevated
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(34.dp).testTag("toggle_system_dsp_btn")
            ) {
                Text(
                    text = if (isSystemDspOn) "ACTIVE" else "ENABLE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Real-Time Audio Meters (IN and OUT)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudioMeterBar(
                title = "INPUT PEAK / RMS",
                meterData = inMeter,
                modifier = Modifier.weight(1f)
            )
            StudioMeterBar(
                title = "OUTPUT PEAK / RMS",
                meterData = outMeter,
                modifier = Modifier.weight(1f)
            )
        }

        // EQ Transfer Curve Visualizer
        EqCurveVisualizer(
            evaluateGainAt = { freq ->
                viewModel.engine.graphicEq.evaluateGainAt(freq, viewModel.engine.targetSampleRate) +
                        viewModel.engine.parametricEq.evaluateGainAt(freq, viewModel.engine.targetSampleRate)
            }
        )

        // FFT Spectrum Analyzer
        FftSpectrumVisualizer(
            magnitudesDb = fftMags,
            waveform = viewModel.engine.waveformBuffer
        )

        // Quick Signal Source Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AUDIO SOURCE:",
                color = StudioSilverMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SourceButton(
                    title = "TEST TONE",
                    isSelected = viewModel.engine.sourceType == AudioSourceType.TEST_TONE,
                    onClick = { viewModel.setAudioSource(AudioSourceType.TEST_TONE) },
                    modifier = Modifier.testTag("source_test_tone")
                )
                SourceButton(
                    title = "INTERNAL PLAYER",
                    isSelected = viewModel.engine.sourceType == AudioSourceType.AUDIO_FILE,
                    onClick = { viewModel.setAudioSource(AudioSourceType.AUDIO_FILE) },
                    modifier = Modifier.testTag("source_internal_player")
                )
            }
        }

        // Quick Navigation Grid to Major Processing Modules
        Text(
            text = "STUDIO PROCESSOR MODULES",
            color = StudioSilverMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickNavCard(
                    title = "GRAPHIC EQ",
                    subtitle = "${viewModel.eqBandCount.value.count} Bands ISO",
                    icon = Icons.Default.GraphicEq,
                    onClick = { viewModel.setTab("EQ") },
                    modifier = Modifier.weight(1f).testTag("nav_graphic_eq")
                )
                QuickNavCard(
                    title = "PARAMETRIC EQ",
                    subtitle = "10 Biquad PEQ",
                    icon = Icons.Default.Equalizer,
                    onClick = { viewModel.setTab("PARAMETRIC") },
                    modifier = Modifier.weight(1f).testTag("nav_peq")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickNavCard(
                    title = "MONO / STEREO",
                    subtitle = viewModel.engine.stereoProcessor.mode.name,
                    icon = Icons.Default.Headphones,
                    onClick = { viewModel.setTab("MONO_STEREO") },
                    modifier = Modifier.weight(1f).testTag("nav_mono_stereo")
                )
                QuickNavCard(
                    title = "QUICK TONE",
                    subtitle = "Bass/Mid/Treble",
                    icon = Icons.Default.Tune,
                    onClick = { viewModel.setTab("TONE") },
                    modifier = Modifier.weight(1f).testTag("nav_tone")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickNavCard(
                    title = "DYNAMICS",
                    subtitle = "Comp / Limiter / Clip",
                    icon = Icons.Default.Speaker,
                    onClick = { viewModel.setTab("DYNAMICS") },
                    modifier = Modifier.weight(1f).testTag("nav_dynamics")
                )
                QuickNavCard(
                    title = "CROSSOVER",
                    subtitle = "${viewModel.engine.crossover.slope.dbPerOct} dB/oct",
                    icon = Icons.Default.Equalizer,
                    onClick = { viewModel.setTab("CROSSOVER") },
                    modifier = Modifier.weight(1f).testTag("nav_crossover")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickNavCard(
                    title = "SOUND SYSTEM MEWAH",
                    subtitle = "Kamar, IEM, TWS, PA & Hi-Fi",
                    icon = Icons.Default.Speaker,
                    onClick = { viewModel.setTab("SYSTEMS") },
                    modifier = Modifier.weight(1f).testTag("nav_systems")
                )
                QuickNavCard(
                    title = "CAPABILITIES",
                    subtitle = "Android Hardware Matrix",
                    icon = Icons.Default.Settings,
                    onClick = { viewModel.setTab("CAPABILITIES") },
                    modifier = Modifier.weight(1f).testTag("nav_capabilities")
                )
            }
        }

        // Master Gain Stage Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MASTER GAIN: ${"%.1f".format(viewModel.engine.masterGainDb)} dB",
                    color = StudioSilver,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = viewModel.engine.masterGainDb,
                    onValueChange = { viewModel.setMasterGain(it) },
                    valueRange = -15f..15f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaroonPrimaryLight,
                        activeTrackColor = MaroonPrimary
                    ),
                    modifier = Modifier.testTag("master_gain_slider")
                )
            }

            Button(
                onClick = { viewModel.toggleMute() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMuted) MaroonPrimary else StudioCardBgElevated
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("mute_button")
            ) {
                Text(if (isMuted) "MUTED" else "MUTE", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SourceButton(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) MaroonPrimary else StudioCardBgElevated,
                RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else StudioSilverMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QuickNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(StudioCardBg, RoundedCornerShape(8.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(StudioCardBgElevated, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Icon(icon, contentDescription = title, tint = MaroonPrimaryLight)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = StudioSilverMuted, fontSize = 10.sp)
            }
        }
    }
}
