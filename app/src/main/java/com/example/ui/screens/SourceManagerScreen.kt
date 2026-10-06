package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.model.CapabilityStatus
import com.example.model.InputSourceType
import com.example.model.SourceChannelStrip
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.MeterGreen
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
fun SourceManagerScreen(viewModel: MainViewModel) {
    val sources by viewModel.sourceStrips.collectAsState()
    val activeSource by viewModel.selectedSource.collectAsState()

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
                Text("AUDIO SOURCE & INPUT ROUTER", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Select & Configure Real Audio Ingest Paths", color = StudioSilverMuted, fontSize = 10.sp)
            }
            Box(
                modifier = Modifier
                    .background(MaroonPrimary.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(activeSource.category, color = MaroonPrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sources) { strip ->
                val isSelected = strip.sourceType == activeSource
                SourceChannelStripCard(
                    strip = strip,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectSource(strip.sourceType) }
                )
            }
        }
    }
}

@Composable
fun SourceChannelStripCard(
    strip: SourceChannelStrip,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var gain by remember { mutableFloatStateOf(strip.gainDb) }
    var isMuted by remember { mutableStateOf(strip.isMuted) }

    val statusColor = when (strip.status) {
        CapabilityStatus.AVAILABLE -> MeterGreen
        CapabilityStatus.LIMITED -> MeterYellow
        CapabilityStatus.UNAVAILABLE -> MeterRed
    }

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
                    text = strip.sourceType.displayName,
                    color = if (isSelected) Color.White else StudioSilver,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strip.statusReason,
                    color = StudioSilverMuted,
                    fontSize = 9.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(strip.status.name, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(28.dp).testTag("select_source_${strip.sourceType.name}")
                ) {
                    Text(if (isSelected) "ROUTED" else "ROUTE", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        if (isSelected) {
            // Ingest Trim / Gain slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("PRE-AMP TRIM: ${"%.1f".format(gain)} dB", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = gain,
                    onValueChange = { gain = it; strip.gainDb = it },
                    valueRange = -18f..18f,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { isMuted = !isMuted; strip.isMuted = isMuted },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMuted) MaroonPrimary else StudioCardBg
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(if (isMuted) "MUTED" else "MUTE", fontSize = 9.sp, color = Color.White)
                }
            }
        }
    }
}
