package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StudioMeterBar
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterYellow
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSilver
import com.example.ui.theme.StudioSilverMuted
import com.example.viewmodel.MainViewModel

@Composable
fun DynamicsScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val comp = viewModel.engine.compressor
    val lim = viewModel.engine.limiter
    val clip = viewModel.engine.softClipper

    var compEnabled by remember { mutableStateOf(comp.isEnabled) }
    var compThresh by remember { mutableFloatStateOf(comp.thresholdDb) }
    var compRatio by remember { mutableFloatStateOf(comp.ratio) }
    var compAttack by remember { mutableFloatStateOf(comp.attackMs) }
    var compRelease by remember { mutableFloatStateOf(comp.releaseMs) }
    var compMakeup by remember { mutableFloatStateOf(comp.makeupGainDb) }

    var limEnabled by remember { mutableStateOf(lim.isEnabled) }
    var limThresh by remember { mutableFloatStateOf(lim.thresholdDb) }
    var limCeil by remember { mutableFloatStateOf(lim.ceilingDb) }

    var clipEnabled by remember { mutableStateOf(clip.isEnabled) }
    var clipDrive by remember { mutableFloatStateOf(clip.driveDb) }
    var clipOut by remember { mutableFloatStateOf(clip.outputDb) }

    val outMeter by viewModel.engine.outputMeterFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StudioMeterBar(title = "DYNAMICS BUS METER", meterData = outMeter)

        // 1. Compressor Section
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
                    Text("STUDIO COMPRESSOR", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Gain Reduction: -${"%.1f".format(comp.gainReductionDb)} dB", color = MeterYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = compEnabled,
                    onCheckedChange = {
                        compEnabled = it
                        viewModel.updateCompressor(it, compThresh, compRatio, compAttack, compRelease, compMakeup)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                )
            }

            // Threshold (-60 dB to 0 dB)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("THRESHOLD", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${compThresh.toInt()} dB", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = compThresh,
                    onValueChange = {
                        compThresh = it
                        viewModel.updateCompressor(compEnabled, it, compRatio, compAttack, compRelease, compMakeup)
                    },
                    valueRange = -60f..0f,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
                    modifier = Modifier.testTag("comp_thresh_slider")
                )
            }

            // Ratio (1:1 to 20:1)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("RATIO", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(compRatio)}:1", color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = compRatio,
                    onValueChange = {
                        compRatio = it
                        viewModel.updateCompressor(compEnabled, compThresh, it, compAttack, compRelease, compMakeup)
                    },
                    valueRange = 1.0f..20.0f,
                    colors = SliderDefaults.colors(thumbColor = OrangeAccent, activeTrackColor = OrangeAccent)
                )
            }

            // Attack & Release
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ATTACK: ${compAttack.toInt()} ms", color = StudioSilverMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = compAttack,
                        onValueChange = {
                            compAttack = it
                            viewModel.updateCompressor(compEnabled, compThresh, compRatio, it, compRelease, compMakeup)
                        },
                        valueRange = 1.0f..200.0f
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("RELEASE: ${compRelease.toInt()} ms", color = StudioSilverMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = compRelease,
                        onValueChange = {
                            compRelease = it
                            viewModel.updateCompressor(compEnabled, compThresh, compRatio, compAttack, it, compMakeup)
                        },
                        valueRange = 10.0f..1500.0f
                    )
                }
            }

            // Makeup Gain
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("MAKEUP GAIN", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(compMakeup)} dB", color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = compMakeup,
                    onValueChange = {
                        compMakeup = it
                        viewModel.updateCompressor(compEnabled, compThresh, compRatio, compAttack, compRelease, it)
                    },
                    valueRange = -12f..12f
                )
            }
        }

        // 2. Limiter Section
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
                    Text("BRICKWALL LIMITER", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (lim.isClippingDetected) "CLIP CEILING ACTIVE" else "CLEAN HEADROOM",
                        color = if (lim.isClippingDetected) MeterRed else MeterYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = limEnabled,
                    onCheckedChange = {
                        limEnabled = it
                        viewModel.updateLimiter(it, limThresh, limCeil)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                )
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("THRESHOLD", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(limThresh)} dBFS", color = MaroonPrimaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = limThresh,
                    onValueChange = {
                        limThresh = it
                        viewModel.updateLimiter(limEnabled, it, limCeil)
                    },
                    valueRange = -12f..0f
                )
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CEILING", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(limCeil)} dBFS", color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = limCeil,
                    onValueChange = {
                        limCeil = it
                        viewModel.updateLimiter(limEnabled, limThresh, it)
                    },
                    valueRange = -6f..0f
                )
            }
        }

        // 3. Soft Clipper Section
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
                Text("ANALOG SOFT CLIPPER", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = clipEnabled,
                    onCheckedChange = {
                        clipEnabled = it
                        viewModel.updateClipper(it, clipDrive, clipOut)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DRIVE: ${"%.1f".format(clipDrive)} dB", color = StudioSilverMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = clipDrive,
                        onValueChange = {
                            clipDrive = it
                            viewModel.updateClipper(clipEnabled, it, clipOut)
                        },
                        valueRange = 0f..18f
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("OUTPUT: ${"%.1f".format(clipOut)} dB", color = StudioSilverMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = clipOut,
                        onValueChange = {
                            clipOut = it
                            viewModel.updateClipper(clipEnabled, clipDrive, it)
                        },
                        valueRange = -12f..6f
                    )
                }
            }
        }
    }
}
