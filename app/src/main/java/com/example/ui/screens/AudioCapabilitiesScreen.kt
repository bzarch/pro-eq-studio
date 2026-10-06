package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CapabilityItem
import com.example.model.CapabilityStatus
import com.example.ui.theme.CyanAccent
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
fun AudioCapabilitiesScreen(viewModel: MainViewModel) {
    val profile = viewModel.deviceProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Device Hardware Summary Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("DEVICE HARDWARE PROFILE", color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Device Model", color = StudioSilverMuted, fontSize = 10.sp)
                Text("${profile.manufacturer} ${profile.model}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("OS Platform", color = StudioSilverMuted, fontSize = 10.sp)
                Text("${profile.osVersion} (API ${profile.apiLevel})", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Active Route Output", color = StudioSilverMuted, fontSize = 10.sp)
                Text(profile.connectedOutput, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Native Audio Sample Rate", color = StudioSilverMuted, fontSize = 10.sp)
                Text("${profile.defaultSampleRate} Hz", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text("ANDROID AUDIO CAPABILITIES MATRIX", color = StudioSilverMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        // Capability Matrix list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(profile.capabilities) { item ->
                CapabilityCard(item)
            }
        }
    }
}

@Composable
fun CapabilityCard(item: CapabilityItem) {
    val statusColor = when (item.status) {
        CapabilityStatus.AVAILABLE -> MeterGreen
        CapabilityStatus.LIMITED -> MeterYellow
        CapabilityStatus.UNAVAILABLE -> MeterRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioCardBgElevated, RoundedCornerShape(8.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.title, color = StudioSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(item.status.name, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(item.detail, color = StudioSilverMuted, fontSize = 10.sp)
        if (item.supportedApi.isNotEmpty()) {
            Text("API Layer: ${item.supportedApi}", color = Color(0xFF6B6B7A), fontSize = 9.sp)
        }
    }
}
