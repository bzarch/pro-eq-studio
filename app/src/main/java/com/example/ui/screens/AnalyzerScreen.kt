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
import com.example.audio.ToneSignalType
import com.example.ui.components.FftSpectrumVisualizer
import com.example.ui.components.StudioMeterBar
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

@Composable
fun AnalyzerScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val fftMags by viewModel.engine.fftMagnitudes.collectAsState()
    val outMeter by viewModel.engine.outputMeterFlow.collectAsState()

    var showOscilloscope by remember { mutableStateOf(false) }

    // Test tone generator state
    val toneGen = viewModel.engine.testTone
    var genActive by remember { mutableStateOf(toneGen.isRunning) }
    var genType by remember { mutableStateOf(toneGen.signalType) }
    var genFreq by remember { mutableFloatStateOf(toneGen.frequency) }
    var genLevel by remember { mutableFloatStateOf(toneGen.levelDb) }

    fun syncGen() {
        toneGen.isRunning = genActive
        toneGen.signalType = genType
        toneGen.frequency = genFreq
        toneGen.levelDb = genLevel
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Switch: FFT Spectrum vs Oscilloscope
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showOscilloscope = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!showOscilloscope) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(32.dp).testTag("btn_fft_mode")
                ) {
                    Text("FFT SPECTRUM", fontSize = 11.sp, color = Color.White)
                }
                Button(
                    onClick = { showOscilloscope = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showOscilloscope) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(32.dp).testTag("btn_scope_mode")
                ) {
                    Text("OSCILLOSCOPE", fontSize = 11.sp, color = Color.White)
                }
            }

            Text(
                text = "${viewModel.engine.targetSampleRate} Hz FFT",
                color = StudioSilverMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Live Real-Time Spectrum or Oscilloscope Display
        FftSpectrumVisualizer(
            magnitudesDb = fftMags,
            waveform = viewModel.engine.waveformBuffer,
            showWaveform = showOscilloscope,
            modifier = Modifier.height(200.dp)
        )

        // Peak / RMS Meter
        StudioMeterBar(title = "SPECTRUM OUTPUT BUS METER", meterData = outMeter)

        // Built-In Precision Signal Generator (Calibration)
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
                    Text("INTERNAL TEST TONE GENERATOR", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Sine, Pink Noise, White Noise, Sweep & Impulse", color = StudioSilverMuted, fontSize = 9.sp)
                }
                Switch(
                    checked = genActive,
                    onCheckedChange = {
                        genActive = it
                        syncGen()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
                )
            }

            // Signal Type Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ToneSignalType.values().forEach { st ->
                    Button(
                        onClick = {
                            genType = st
                            syncGen()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (genType == st) MaroonPrimary else StudioCardBgElevated
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(28.dp)
                    ) {
                        Text(st.name.replace("_", " "), fontSize = 8.sp, color = Color.White, maxLines = 1)
                    }
                }
            }

            // Tone Frequency
            if (genType == ToneSignalType.SINE) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SINE FREQUENCY", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${genFreq.toInt()} Hz", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = genFreq,
                        onValueChange = {
                            genFreq = it
                            syncGen()
                        },
                        valueRange = 20f..20000f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }
            }

            // Tone Level dBFS (Safe default -12 dBFS)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GENERATOR LEVEL", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${"%.1f".format(genLevel)} dBFS", color = StudioSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = genLevel,
                    onValueChange = {
                        genLevel = it
                        syncGen()
                    },
                    valueRange = -48f..0f
                )
            }
        }
    }
}
