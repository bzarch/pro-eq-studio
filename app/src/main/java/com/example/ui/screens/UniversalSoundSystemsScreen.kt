package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioProfileDetail
import com.example.presets.UniversalSoundProfiles
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MaroonPrimaryLight
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSilver
import com.example.ui.theme.StudioSilverMuted
import com.example.viewmodel.MainViewModel

@Composable
fun UniversalSoundSystemsScreen(viewModel: MainViewModel) {
    val activeProfile by viewModel.activeUniversalProfile.collectAsState()

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
                Text("UNIVERSAL SOUND SYSTEM TUNING", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Sound Mewah, Kamar Akustik, IEM, TWS, Headset, PA Lapangan & Car Audio", color = StudioSilverMuted, fontSize = 9.sp)
            }
            Box(
                modifier = Modifier
                    .background(MaroonPrimary.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("8 PROFILES", color = MaroonPrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(UniversalSoundProfiles.profiles) { profile ->
                val isSelected = profile.presetName == activeProfile.presetName
                SoundSystemProfileCard(
                    profile = profile,
                    isSelected = isSelected,
                    onApply = { viewModel.applyUniversalSoundProfile(profile) }
                )
            }
        }
    }
}

@Composable
fun SoundSystemProfileCard(
    profile: AudioProfileDetail,
    isSelected: Boolean,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) StudioCardBgElevated else StudioCardBg, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isSelected) MaroonPrimaryLight else StudioBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onApply() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.category.title,
                    color = if (isSelected) Color.White else StudioSilver,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile.presetName,
                    color = if (isSelected) CyanAccent else StudioSilverMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .background(MeterGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ACTIVE DSP", color = MeterGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaroonPrimary else StudioCardBgElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(28.dp).testTag("apply_profile_${profile.category.name}")
                ) {
                    Text(if (isSelected) "APPLIED" else "APPLY", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        Text(
            text = profile.description,
            color = StudioSilverMuted,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )

        // Telemetry Tags (Bass, Mid, Air, Crossfeed, Stereo Width)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF14141A), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Bass: +${"%.1f".format(profile.bassBoostDb)}dB", color = MaroonPrimaryLight, fontSize = 9.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF14141A), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Mid: +${"%.1f".format(profile.midClarityDb)}dB", color = OrangeAccent, fontSize = 9.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF14141A), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Air: +${"%.1f".format(profile.airPresenceDb)}dB", color = CyanAccent, fontSize = 9.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF14141A), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Width: ${profile.stereoWidthPct.toInt()}%", color = StudioSilver, fontSize = 9.sp)
            }
        }
    }
}
