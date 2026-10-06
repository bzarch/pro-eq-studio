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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.model.StudioPreset
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
fun PresetsScreen(viewModel: MainViewModel) {
    val presets by viewModel.allPresets.collectAsState()
    val currentPreset by viewModel.currentPreset.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Standard", "Vocal", "Music", "Speaker", "Headphone", "Media", "User")
    val filteredPresets = if (selectedCategory == "All") {
        presets
    } else {
        presets.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Action Bar: Save Current Configuration
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("CURRENT PRESET:", color = StudioSilverMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(currentPreset.name, color = MaroonPrimaryLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    newPresetName = "${currentPreset.name} (Custom)"
                    showSaveDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(34.dp).testTag("save_preset_button")
            ) {
                Text("SAVE AS PRESET", fontSize = 11.sp, color = Color.White)
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.take(5).forEach { cat ->
                val isSel = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSel) MaroonPrimary else StudioCardBgElevated,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        color = if (isSel) Color.White else StudioSilverMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Presets List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredPresets) { preset ->
                val isCurrent = preset.name == currentPreset.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isCurrent) StudioCardBgElevated else StudioCardBg,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (isCurrent) MaroonPrimaryLight else StudioBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { viewModel.loadPreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.name,
                            color = if (isCurrent) Color.White else StudioSilver,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${preset.category} • Preamp: ${"%.1f".format(preset.preampDb)}dB • Width: ${(preset.stereoWidth * 100).toInt()}%",
                            color = StudioSilverMuted,
                            fontSize = 10.sp
                        )
                    }

                    if (preset.category.equals("User", ignoreCase = true)) {
                        IconButton(
                            onClick = { viewModel.deletePreset(preset.name) },
                            modifier = Modifier.height(28.dp).width(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Studio Preset", color = StudioSilver) },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            viewModel.savePreset(newPresetName.trim())
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showSaveDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioCardBgElevated)
                ) {
                    Text("Cancel", color = StudioSilver)
                }
            },
            containerColor = StudioCardBg
        )
    }
}
