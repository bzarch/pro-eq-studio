package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsp.GraphicEqBandCount
import com.example.ui.components.EqCurveVisualizer
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
fun GraphicEqScreen(viewModel: MainViewModel) {
    val bandMode by viewModel.eqBandCount.collectAsState()
    val scrollState = rememberScrollState()
    val freqs = viewModel.engine.graphicEq.getCurrentFrequencies()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Selector: 10, 15, 31 Bands & Preamp Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BandCountButton(
                    title = "10 BANDS",
                    isSelected = bandMode == GraphicEqBandCount.BANDS_10,
                    onClick = { viewModel.setGraphicEqBandCount(GraphicEqBandCount.BANDS_10) },
                    modifier = Modifier.testTag("bands_10_button")
                )
                BandCountButton(
                    title = "15 BANDS",
                    isSelected = bandMode == GraphicEqBandCount.BANDS_15,
                    onClick = { viewModel.setGraphicEqBandCount(GraphicEqBandCount.BANDS_15) },
                    modifier = Modifier.testTag("bands_15_button")
                )
                BandCountButton(
                    title = "31 BANDS",
                    isSelected = bandMode == GraphicEqBandCount.BANDS_31,
                    onClick = { viewModel.setGraphicEqBandCount(GraphicEqBandCount.BANDS_31) },
                    modifier = Modifier.testTag("bands_31_button")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { viewModel.resetGraphicEq() },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCardBgElevated),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(30.dp).testTag("flat_eq_button")
                ) {
                    Text("FLAT", fontSize = 11.sp, color = StudioSilver)
                }
            }
        }

        // Live Calculated Frequency Response Visualizer
        EqCurveVisualizer(
            evaluateGainAt = { freq ->
                viewModel.engine.graphicEq.evaluateGainAt(freq, viewModel.engine.targetSampleRate)
            }
        )

        // Preamp Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PREAMP: ${"%.1f".format(viewModel.engine.graphicEq.preampDb)} dB",
                color = StudioSilver,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(120.dp)
            )
            Slider(
                value = viewModel.engine.graphicEq.preampDb,
                onValueChange = { viewModel.setGraphicPreamp(it) },
                valueRange = -15f..15f,
                colors = SliderDefaults.colors(
                    thumbColor = MaroonPrimaryLight,
                    activeTrackColor = MaroonPrimary
                ),
                modifier = Modifier.weight(1f).testTag("preamp_slider")
            )
        }

        // Vertical Sliders Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(if (bandMode == GraphicEqBandCount.BANDS_31) 4.dp else 10.dp)
            ) {
                for (i in 0 until bandMode.count) {
                    val freqHz = if (i < freqs.size) freqs[i] else 1000f
                    val label = if (freqHz >= 1000f) "${(freqHz / 1000f).toInt()}k" else "${freqHz.toInt()}"

                    VerticalEqSlider(
                        index = i,
                        label = label,
                        currentGain = viewModel.engine.graphicEq.bandGains31[i],
                        onGainChange = { newGain ->
                            viewModel.setGraphicBandGain(i, newGain)
                        },
                        onReset = {
                            viewModel.setGraphicBandGain(i, 0f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BandCountButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaroonPrimary else StudioCardBgElevated
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.height(30.dp)
    ) {
        Text(title, fontSize = 10.sp, color = if (isSelected) Color.White else StudioSilverMuted)
    }
}

@Composable
fun VerticalEqSlider(
    index: Int,
    label: String,
    currentGain: Float,
    onGainChange: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localGain by remember(currentGain) { mutableFloatStateOf(currentGain) }

    Column(
        modifier = modifier
            .width(42.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Gain display (Tap/Double-tap to reset)
        Text(
            text = "${if (localGain > 0) "+" else ""}${"%.1f".format(localGain)}",
            fontSize = 9.sp,
            color = if (localGain != 0f) MaroonPrimaryLight else StudioSilverMuted,
            fontWeight = FontWeight.Bold
        )

        // Vertical rotated slider
        Box(
            modifier = Modifier
                .weight(1f)
                .width(42.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = localGain,
                onValueChange = {
                    localGain = it
                    onGainChange(it)
                },
                valueRange = -15f..15f,
                colors = SliderDefaults.colors(
                    thumbColor = MaroonPrimaryLight,
                    activeTrackColor = MaroonPrimary,
                    inactiveTrackColor = Color(0xFF2B2B38)
                ),
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = 270f
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            Constraints(
                                minWidth = constraints.minHeight,
                                maxWidth = constraints.maxHeight,
                                minHeight = constraints.minWidth,
                                maxHeight = constraints.maxWidth
                            )
                        )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                -(placeable.width - placeable.height) / 2,
                                -(placeable.height - placeable.width) / 2
                            )
                        }
                    }
                    .testTag("eq_slider_$index")
            )
        }

        // Frequency Label
        Text(
            text = label,
            fontSize = 10.sp,
            color = StudioSilver,
            fontWeight = FontWeight.Bold
        )
    }
}
