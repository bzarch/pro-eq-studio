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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EqCurveVisualizer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSilver
import com.example.ui.theme.StudioSilverMuted
import com.example.viewmodel.MainViewModel

@Composable
fun SpeakerProcessorScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()

    var activeTab by remember { mutableStateOf("SPEAKER") } // SPEAKER, CAR, HEADPHONE
    val carAlign by viewModel.carAlignment.collectAsState()
    val hpProfile by viewModel.headphoneProfile.collectAsState()

    var frontLDelay by remember { mutableFloatStateOf(carAlign.frontLeftDelayMs) }
    var frontRDelay by remember { mutableFloatStateOf(carAlign.frontRightDelayMs) }
    var crossfeedOn by remember { mutableStateOf(hpProfile.isCrossfeedEnabled) }
    var crossfeedAmount by remember { mutableFloatStateOf(hpProfile.crossfeedAmount) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Selector: Speaker Management, Car Audio DSP, Headphone Processor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("SPEAKER", "CAR AUDIO", "HEADPHONE").forEach { mode ->
                val isSel = activeTab == mode
                Button(
                    onClick = { activeTab = mode },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Text(mode, fontSize = 10.sp, color = if (isSel) Color.White else StudioSilverMuted, fontWeight = FontWeight.Bold)
                }
            }
        }

        when (activeTab) {
            "SPEAKER" -> {
                // Speaker Management: Crossover, Subsonic HPF, Driver EQ, Limiter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardBg, RoundedCornerShape(8.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("SPEAKER MANAGEMENT SYSTEM (PA & STUDIO)", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Multi-way driver crossover, subsonic protection, and brickwall peak clamping.", color = StudioSilverMuted, fontSize = 10.sp)

                    EqCurveVisualizer(
                        evaluateGainAt = { freq ->
                            viewModel.engine.crossover.lowGainDb * kotlin.math.exp(-freq / 250f) +
                                    viewModel.engine.crossover.highGainDb * (1.0f - kotlin.math.exp(-freq / 250f))
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SUBSONIC HIGHPASS FILTER (30 Hz)", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DRIVER PEAK LIMITER CLAMP", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = viewModel.engine.limiter.isEnabled,
                            onCheckedChange = { viewModel.updateLimiter(it, viewModel.engine.limiter.thresholdDb, viewModel.engine.limiter.ceilingDb) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                        )
                    }
                }
            }
            "CAR AUDIO" -> {
                // Car Audio Time Alignment & Speaker Delay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardBg, RoundedCornerShape(8.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("CAR AUDIO TIME ALIGNMENT (DISTANCE DELAYS)", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Compensates off-center driver position acoustic arrival times.", color = StudioSilverMuted, fontSize = 10.sp)

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FRONT LEFT DELAY", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${"%.2f".format(frontLDelay)} ms", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = frontLDelay,
                            onValueChange = {
                                frontLDelay = it
                                viewModel.updateTimeAlignment(it, frontRDelay)
                            },
                            valueRange = 0f..25f,
                            colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FRONT RIGHT DELAY", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${"%.2f".format(frontRDelay)} ms", color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = frontRDelay,
                            onValueChange = {
                                frontRDelay = it
                                viewModel.updateTimeAlignment(frontLDelay, it)
                            },
                            valueRange = 0f..25f,
                            colors = SliderDefaults.colors(thumbColor = OrangeAccent, activeTrackColor = OrangeAccent)
                        )
                    }
                }
            }
            "HEADPHONE" -> {
                // Headphone Crossfeed & Spatial fatigue eliminator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioCardBg, RoundedCornerShape(8.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("BINAURAL CROSSFEED PROCESSOR", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Eliminates extreme stereo separation fatigue.", color = StudioSilverMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = crossfeedOn,
                            onCheckedChange = {
                                crossfeedOn = it
                                viewModel.updateCrossfeed(it, crossfeedAmount)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CROSSFEED BLEND AMOUNT", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${(crossfeedAmount * 100).toInt()}%", color = MaroonPrimaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = crossfeedAmount,
                            onValueChange = {
                                crossfeedAmount = it
                                viewModel.updateCrossfeed(crossfeedOn, it)
                            },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = MaroonPrimaryLight, activeTrackColor = MaroonPrimary)
                        )
                    }
                }
            }
        }
    }
}
