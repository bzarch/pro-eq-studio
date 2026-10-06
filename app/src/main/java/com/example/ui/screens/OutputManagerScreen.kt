package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OutputDestinationType
import com.example.model.OutputRouteState
import com.example.ui.theme.CyanAccent
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
fun OutputManagerScreen(viewModel: MainViewModel) {
    val routes by viewModel.outputRoutes.collectAsState()
    val activeOutput by viewModel.selectedOutput.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("OUTPUT DEVICE ROUTER & BUS", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Hardware Sink Detection & Per-Output DSP Profiles", color = StudioSilverMuted, fontSize = 10.sp)
            }
            Text(activeOutput.displayName, color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(routes) { route ->
                val isSelected = route.destinationType == activeOutput
                OutputRouteCard(
                    route = route,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectOutput(route.destinationType) }
                )
            }
        }
    }
}

@Composable
fun OutputRouteCard(
    route: OutputRouteState,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var gain by remember { mutableFloatStateOf(route.outputGainDb) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) StudioCardBgElevated else StudioCardBg, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isSelected) MaroonPrimaryLight else StudioBorder,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.destinationType.displayName,
                    color = if (isSelected) Color.White else StudioSilver,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Profile: ${route.profilePreset} • ${route.detectedSampleRate} Hz • 2 ch",
                    color = StudioSilverMuted,
                    fontSize = 9.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (route.isConnected) {
                    Box(
                        modifier = Modifier
                            .background(MeterGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ACTIVE", color = MeterGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(28.dp).testTag("select_output_${route.destinationType.name}")
                ) {
                    Text(if (isSelected) "PRIMARY" else "ASSIGN", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        if (isSelected) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("BUS GAIN: ${"%.1f".format(gain)} dB", color = MaroonPrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = gain,
                    onValueChange = { gain = it; route.outputGainDb = it },
                    valueRange = -18f..12f,
                    colors = SliderDefaults.colors(thumbColor = MaroonPrimaryLight, activeTrackColor = MaroonPrimary),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
