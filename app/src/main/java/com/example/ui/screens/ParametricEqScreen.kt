package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
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
import com.example.dsp.FilterType
import com.example.ui.components.EqCurveVisualizer
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSilver
import com.example.ui.theme.StudioSilverMuted
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametricEqScreen(viewModel: MainViewModel) {
    val selectedIndex by viewModel.selectedPeqBand.collectAsState()
    val scrollState = rememberScrollState()
    val bandsScrollState = rememberScrollState()

    val currentBand = viewModel.engine.parametricEq.bands[selectedIndex]

    var bandFreq by remember(selectedIndex, currentBand.frequency) { mutableFloatStateOf(currentBand.frequency) }
    var bandGain by remember(selectedIndex, currentBand.gainDb) { mutableFloatStateOf(currentBand.gainDb) }
    var bandQ by remember(selectedIndex, currentBand.q) { mutableFloatStateOf(currentBand.q) }
    var bandType by remember(selectedIndex, currentBand.type) { mutableStateOf(currentBand.type) }
    var bandEnabled by remember(selectedIndex, currentBand.isEnabled) { mutableStateOf(currentBand.isEnabled) }

    var typeMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Curve visualization
        EqCurveVisualizer(
            evaluateGainAt = { freq ->
                viewModel.engine.parametricEq.evaluateGainAt(freq, viewModel.engine.targetSampleRate)
            }
        )

        // Band Selector Buttons Strip (Bands 1 to 10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(bandsScrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until 10) {
                val b = viewModel.engine.parametricEq.bands[i]
                val isSel = i == selectedIndex
                Box(
                    modifier = Modifier
                        .background(
                            if (isSel) MaroonPrimary else StudioCardBgElevated,
                            RoundedCornerShape(6.dp)
                        )
                        .border(1.dp, if (isSel) MaroonPrimaryLight else StudioBorder, RoundedCornerShape(6.dp))
                        .clickable { viewModel.selectPeqBand(i) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("peq_band_btn_$i")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "B${i + 1}",
                            color = if (isSel) Color.White else StudioSilver,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (b.frequency >= 1000f) "${(b.frequency / 1000f).toInt()}k" else "${b.frequency.toInt()}Hz",
                            color = if (isSel) Color.White.copy(alpha = 0.8f) else StudioSilverMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Inspector & Fine Adjustment Panel for Selected Band
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BAND ${selectedIndex + 1} INSPECTOR",
                    color = StudioSilver,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ACTIVE", color = StudioSilverMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = bandEnabled,
                        onCheckedChange = {
                            bandEnabled = it
                            viewModel.updatePeqBand(selectedIndex, bandFreq, bandGain, bandQ, bandType, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                    )
                }
            }

            // Filter Type Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = bandType.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter Topology", color = StudioSilverMuted) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    FilterType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.replace("_", " ")) },
                            onClick = {
                                bandType = type
                                typeMenuExpanded = false
                                viewModel.updatePeqBand(selectedIndex, bandFreq, bandGain, bandQ, type, bandEnabled)
                            }
                        )
                    }
                }
            }

            // Frequency Slider (20 Hz - 20,000 Hz Logarithmic mapping)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FREQUENCY", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${bandFreq.toInt()} Hz", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = bandFreq,
                    onValueChange = {
                        bandFreq = it
                        viewModel.updatePeqBand(selectedIndex, it, bandGain, bandQ, bandType, bandEnabled)
                    },
                    valueRange = 20f..20000f,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
                    modifier = Modifier.testTag("peq_freq_slider")
                )
            }

            // Gain Slider (-15 dB to +15 dB)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GAIN", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(bandGain)} dB", color = MaroonPrimaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = bandGain,
                    onValueChange = {
                        bandGain = it
                        viewModel.updatePeqBand(selectedIndex, bandFreq, it, bandQ, bandType, bandEnabled)
                    },
                    valueRange = -15f..15f,
                    colors = SliderDefaults.colors(thumbColor = MaroonPrimaryLight, activeTrackColor = MaroonPrimary),
                    modifier = Modifier.testTag("peq_gain_slider")
                )
            }

            // Q Factor (Bandwidth) Slider (0.1 to 20)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Q FACTOR / BANDWIDTH", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.2f".format(bandQ)} Q", color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = bandQ,
                    onValueChange = {
                        bandQ = it
                        viewModel.updatePeqBand(selectedIndex, bandFreq, bandGain, it, bandType, bandEnabled)
                    },
                    valueRange = 0.1f..15.0f,
                    colors = SliderDefaults.colors(thumbColor = StudioSilver, activeTrackColor = Color.Gray),
                    modifier = Modifier.testTag("peq_q_slider")
                )
            }

            // Reset Button
            Button(
                onClick = {
                    bandGain = 0f
                    bandQ = 1.0f
                    viewModel.updatePeqBand(selectedIndex, bandFreq, 0f, 1.0f, bandType, true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioCardBgElevated),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().testTag("peq_reset_band")
            ) {
                Text("RESET BAND ${selectedIndex + 1}", fontSize = 11.sp, color = StudioSilver)
            }
        }
    }
}
