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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
fun QuickToneScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val tone = viewModel.engine.quickTone

    var sub by remember { mutableFloatStateOf(tone.subDb) }
    var bass by remember { mutableFloatStateOf(tone.bassDb) }
    var lowMid by remember { mutableFloatStateOf(tone.lowMidDb) }
    var mid by remember { mutableFloatStateOf(tone.midDb) }
    var upperMid by remember { mutableFloatStateOf(tone.upperMidDb) }
    var presence by remember { mutableFloatStateOf(tone.presenceDb) }
    var air by remember { mutableFloatStateOf(tone.airDb) }

    fun sync() {
        viewModel.setQuickTone(sub, bass, lowMid, mid, upperMid, presence, air)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Preset Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Neutral", "Warm", "Bright", "Vocal", "Mid Focus", "Presence").forEach { name ->
                Button(
                    onClick = {
                        viewModel.applyQuickTonePreset(name)
                        sub = tone.subDb
                        bass = tone.bassDb
                        lowMid = tone.lowMidDb
                        mid = tone.midDb
                        upperMid = tone.upperMidDb
                        presence = tone.presenceDb
                        air = tone.airDb
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCardBgElevated),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(30.dp).testTag("tone_preset_$name")
                ) {
                    Text(name, fontSize = 9.sp, color = StudioSilver, maxLines = 1)
                }
            }
        }

        // Live Graphic Curve
        EqCurveVisualizer(
            evaluateGainAt = { freq ->
                tone.subDb * kotlin.math.exp(-freq / 60f) +
                        tone.bassDb * kotlin.math.exp(-kotlin.math.abs(freq - 100f) / 100f) +
                        tone.lowMidDb * kotlin.math.exp(-kotlin.math.abs(freq - 350f) / 250f) +
                        tone.midDb * kotlin.math.exp(-kotlin.math.abs(freq - 1000f) / 600f) +
                        tone.upperMidDb * kotlin.math.exp(-kotlin.math.abs(freq - 3200f) / 1500f) +
                        tone.presenceDb * kotlin.math.exp(-kotlin.math.abs(freq - 6000f) / 2500f) +
                        tone.airDb * (1.0f - kotlin.math.exp(-freq / 12000f))
            }
        )

        // 7 Bands Sliders
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToneBandSlider(
                label = "SUB BASS (45 Hz)",
                value = sub,
                onValueChange = { sub = it; sync() },
                color = MaroonPrimaryLight
            )
            ToneBandSlider(
                label = "BASS (100 Hz)",
                value = bass,
                onValueChange = { bass = it; sync() },
                color = MaroonPrimaryLight
            )
            ToneBandSlider(
                label = "LOW MID (350 Hz)",
                value = lowMid,
                onValueChange = { lowMid = it; sync() },
                color = OrangeAccent
            )
            ToneBandSlider(
                label = "MIDRANGE (1.0 kHz)",
                value = mid,
                onValueChange = { mid = it; sync() },
                color = OrangeAccent
            )
            ToneBandSlider(
                label = "UPPER MID (3.2 kHz)",
                value = upperMid,
                onValueChange = { upperMid = it; sync() },
                color = CyanAccent
            )
            ToneBandSlider(
                label = "PRESENCE (6.0 kHz)",
                value = presence,
                onValueChange = { presence = it; sync() },
                color = CyanAccent
            )
            ToneBandSlider(
                label = "AIR / HIGH TREBLE (12 kHz)",
                value = air,
                onValueChange = { air = it; sync() },
                color = StudioSilver
            )
        }
    }
}

@Composable
fun ToneBandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("${if (value > 0) "+" else ""}${"%.1f".format(value)} dB", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -15f..15f,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color.copy(alpha = 0.8f))
        )
    }
}
