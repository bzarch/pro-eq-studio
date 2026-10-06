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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.RoutingMode
import com.example.ui.components.StudioMeterBar
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterYellow
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSilver
import com.example.ui.theme.StudioSilverMuted
import com.example.viewmodel.MainViewModel

@Composable
fun MonoStereoScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val outMeter by viewModel.engine.outputMeterFlow.collectAsState()

    var activeMode by remember { mutableStateOf(viewModel.engine.stereoProcessor.mode) }
    var stereoWidth by remember { mutableFloatStateOf(viewModel.engine.stereoProcessor.stereoWidth) }
    var balance by remember { mutableFloatStateOf(viewModel.engine.stereoProcessor.balance) }
    var midGain by remember { mutableFloatStateOf(viewModel.engine.stereoProcessor.midGainDb) }
    var sideGain by remember { mutableFloatStateOf(viewModel.engine.stereoProcessor.sideGainDb) }
    var invL by remember { mutableStateOf(viewModel.engine.stereoProcessor.invertLeft) }
    var invR by remember { mutableStateOf(viewModel.engine.stereoProcessor.invertRight) }
    var phaseDeg by remember { mutableFloatStateOf(viewModel.engine.stereoProcessor.phaseDegrees) }

    val correlation = viewModel.engine.stereoProcessor.correlation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Output Meter for channel monitoring
        StudioMeterBar(title = "CHANNEL BUS OUTPUT", meterData = outMeter)

        // Phase Correlation Meter (-1.0 to +1.0)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PHASE CORRELATION METER", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                val statusText = when {
                    correlation > 0.4f -> "MONO COMPATIBLE"
                    correlation >= 0.0f -> "ACCEPTABLE STEREO"
                    correlation >= -0.3f -> "CAUTION (WIDE)"
                    else -> "PHASE CANCELLATION DETECTED"
                }
                val statusColor = when {
                    correlation > 0.4f -> MeterGreen
                    correlation >= 0.0f -> MeterYellow
                    else -> MeterRed
                }
                Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Visual meter [-1.0 .. 0.0 .. +1.0]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(Color(0xFF0C0C10), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFF282836), RoundedCornerShape(4.dp))
            ) {
                // Marker position: norm from -1..1 to 0..1
                val normX = ((correlation + 1.0f) / 2.0f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(normX)
                        .height(14.dp)
                        .background(
                            if (correlation < 0f) MeterRed.copy(alpha = 0.7f) else MeterGreen.copy(alpha = 0.7f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("-1.0 (Out of Phase)", color = StudioSilverMuted, fontSize = 9.sp)
                Text("0 (Stereo)", color = StudioSilverMuted, fontSize = 9.sp)
                Text("+1.0 (Mono)", color = StudioSilverMuted, fontSize = 9.sp)
            }
        }

        // Channel Routing Mode Grid (6 Big Buttons)
        Text("ROUTING ENGINE MODE", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoutingButton(
                    title = "STEREO",
                    subtitle = "L / R Normal",
                    isSelected = activeMode == RoutingMode.STEREO,
                    onClick = {
                        activeMode = RoutingMode.STEREO
                        viewModel.setRoutingMode(RoutingMode.STEREO)
                    },
                    modifier = Modifier.weight(1f).testTag("route_stereo")
                )
                RoutingButton(
                    title = "MONO",
                    subtitle = "Safe 3dB Summing",
                    isSelected = activeMode == RoutingMode.MONO,
                    onClick = {
                        activeMode = RoutingMode.MONO
                        viewModel.setRoutingMode(RoutingMode.MONO)
                    },
                    modifier = Modifier.weight(1f).testTag("route_mono")
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoutingButton(
                    title = "LEFT ONLY",
                    subtitle = "L -> Both Out",
                    isSelected = activeMode == RoutingMode.LEFT_ONLY,
                    onClick = {
                        activeMode = RoutingMode.LEFT_ONLY
                        viewModel.setRoutingMode(RoutingMode.LEFT_ONLY)
                    },
                    modifier = Modifier.weight(1f).testTag("route_left")
                )
                RoutingButton(
                    title = "RIGHT ONLY",
                    subtitle = "R -> Both Out",
                    isSelected = activeMode == RoutingMode.RIGHT_ONLY,
                    onClick = {
                        activeMode = RoutingMode.RIGHT_ONLY
                        viewModel.setRoutingMode(RoutingMode.RIGHT_ONLY)
                    },
                    modifier = Modifier.weight(1f).testTag("route_right")
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoutingButton(
                    title = "SWAP L/R",
                    subtitle = "Inverted Channels",
                    isSelected = activeMode == RoutingMode.SWAP,
                    onClick = {
                        activeMode = RoutingMode.SWAP
                        viewModel.setRoutingMode(RoutingMode.SWAP)
                    },
                    modifier = Modifier.weight(1f).testTag("route_swap")
                )
                RoutingButton(
                    title = "DUAL MONO",
                    subtitle = "Split Processing",
                    isSelected = activeMode == RoutingMode.DUAL_MONO,
                    onClick = {
                        activeMode = RoutingMode.DUAL_MONO
                        viewModel.setRoutingMode(RoutingMode.DUAL_MONO)
                    },
                    modifier = Modifier.weight(1f).testTag("route_dual_mono")
                )
            }
        }

        // Stereo Width & Balance
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("STEREO SPATIAL CONTROLS", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            // Stereo Width (0% to 200%)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("STEREO WIDTH", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${(stereoWidth * 100).toInt()}%", color = MaroonPrimaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = stereoWidth,
                    onValueChange = {
                        stereoWidth = it
                        viewModel.setStereoWidth(it)
                    },
                    valueRange = 0.0f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = MaroonPrimaryLight, activeTrackColor = MaroonPrimary),
                    modifier = Modifier.testTag("stereo_width_slider")
                )
            }

            // Balance
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("L/R BALANCE", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    val balText = when {
                        balance < -0.05f -> "L ${(-balance * 100).toInt()}%"
                        balance > 0.05f -> "R ${(balance * 100).toInt()}%"
                        else -> "CENTER"
                    }
                    Text(balText, color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = balance,
                    onValueChange = {
                        balance = it
                        viewModel.setBalance(it)
                    },
                    valueRange = -1.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = StudioSilver, activeTrackColor = Color.Gray),
                    modifier = Modifier.testTag("balance_slider")
                )
            }

            // Mid / Side Gains
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("MID GAIN: ${"%.1f".format(midGain)} dB", color = StudioSilverMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = midGain,
                        onValueChange = {
                            midGain = it
                            viewModel.setMidSideGain(it, sideGain)
                        },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("SIDE GAIN: ${"%.1f".format(sideGain)} dB", color = StudioSilverMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = sideGain,
                        onValueChange = {
                            sideGain = it
                            viewModel.setMidSideGain(midGain, it)
                        },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }
            }
        }

        // Polarity Inversion & Phase Shift
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("POLARITY & PHASE ROTATION", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("INVERT LEFT (180°)", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = invL,
                        onCheckedChange = {
                            invL = it
                            viewModel.setPolarity(it, invR)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("INVERT RIGHT (180°)", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = invR,
                        onCheckedChange = {
                            invR = it
                            viewModel.setPolarity(invL, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                    )
                }
            }

            // Phase rotation 0° – 360°
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PHASE ROTATION", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${phaseDeg.toInt()}°", color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = phaseDeg,
                    onValueChange = {
                        phaseDeg = it
                        viewModel.setPhase(it)
                    },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(thumbColor = StudioSilver, activeTrackColor = Color.DarkGray)
                )
            }
        }
    }
}

@Composable
fun RoutingButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaroonPrimary else StudioCardBgElevated
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.height(56.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) Color.White else StudioSilver,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else StudioSilverMuted,
                fontSize = 9.sp
            )
        }
    }
}
