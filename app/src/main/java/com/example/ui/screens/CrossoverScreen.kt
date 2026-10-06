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
import com.example.dsp.CrossoverMode
import com.example.dsp.CrossoverSlope
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
fun CrossoverScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val xover = viewModel.engine.crossover

    var isEnabled by remember { mutableStateOf(xover.isEnabled) }
    var mode by remember { mutableStateOf(xover.mode) }
    var slope by remember { mutableStateOf(xover.slope) }

    var lowFreq by remember { mutableFloatStateOf(xover.lowFreq) }
    var highFreq by remember { mutableFloatStateOf(xover.highFreq) }
    var lowGain by remember { mutableFloatStateOf(xover.lowGainDb) }
    var midGain by remember { mutableFloatStateOf(xover.midGainDb) }
    var highGain by remember { mutableFloatStateOf(xover.highGainDb) }

    fun sync() {
        viewModel.updateCrossover(isEnabled, mode, slope, lowFreq, highFreq, lowGain, midGain, highGain)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Curve visualization
        EqCurveVisualizer(
            evaluateGainAt = { freq ->
                if (!isEnabled) 0f
                else {
                    val order = slope.order
                    val lowAtten = -10f * kotlin.math.log10(1f + Math.pow((freq / lowFreq).toDouble(), order.toDouble()).toFloat())
                    val highAtten = -10f * kotlin.math.log10(1f + Math.pow((lowFreq / freq).toDouble(), order.toDouble()).toFloat())
                    (lowAtten + lowGain).coerceAtLeast(-36f)
                }
            }
        )

        // Crossover Switch & Mode Selector
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
                Text("CROSSOVER SPLITTER ENGINE", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        isEnabled = it
                        sync()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                )
            }

            // 2-Way vs 3-Way Mode
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { mode = CrossoverMode.TWO_WAY; sync() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == CrossoverMode.TWO_WAY) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("2-WAY (LOW / HIGH)", fontSize = 11.sp, color = Color.White)
                }
                Button(
                    onClick = { mode = CrossoverMode.THREE_WAY; sync() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == CrossoverMode.THREE_WAY) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text("3-WAY (LOW/MID/HIGH)", fontSize = 11.sp, color = Color.White)
                }
            }

            // Filter Slopes: 6, 12, 18, 24, 48 dB/oct
            Text("FILTER ROLL-OFF SLOPE", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CrossoverSlope.values().forEach { sl ->
                    Button(
                        onClick = { slope = sl; sync() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (slope == sl) MaroonPrimary else StudioCardBgElevated
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(30.dp)
                    ) {
                        Text("${sl.dbPerOct}dB", fontSize = 9.sp, color = Color.White)
                    }
                }
            }
        }

        // Crossover Points & Band Gains
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("CROSSOVER FREQUENCIES & BAND GAINS", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            // Low Freq (Hz)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LOW SPLIT FREQUENCY", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${lowFreq.toInt()} Hz", color = MaroonPrimaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = lowFreq,
                    onValueChange = { lowFreq = it; sync() },
                    valueRange = 50f..1000f,
                    colors = SliderDefaults.colors(thumbColor = MaroonPrimaryLight, activeTrackColor = MaroonPrimary)
                )
            }

            if (mode == CrossoverMode.THREE_WAY) {
                // High Freq (Hz)
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("HIGH SPLIT FREQUENCY", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${highFreq.toInt()} Hz", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = highFreq,
                        onValueChange = { highFreq = it; sync() },
                        valueRange = 1000f..12000f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }
            }

            // Band Gains
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LOW BAND GAIN", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(lowGain)} dB", color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = lowGain,
                    onValueChange = { lowGain = it; sync() },
                    valueRange = -15f..15f
                )
            }

            if (mode == CrossoverMode.THREE_WAY) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("MID BAND GAIN", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${"%.1f".format(midGain)} dB", color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = midGain,
                        onValueChange = { midGain = it; sync() },
                        valueRange = -15f..15f
                    )
                }
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("HIGH BAND GAIN", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(highGain)} dB", color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = highGain,
                    onValueChange = { highGain = it; sync() },
                    valueRange = -15f..15f
                )
            }
        }
    }
}
