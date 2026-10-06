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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.DspModuleInfo
import com.example.model.ModuleType
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
fun DspRackScreen(viewModel: MainViewModel) {
    val modules by viewModel.rackModules.collectAsState()

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
                Text("DSP MULTI-EFFECT RACK CHAIN", color = StudioSilver, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Real-Time Sequential Audio Processing Pipeline", color = StudioSilverMuted, fontSize = 10.sp)
            }
            Text("${modules.count { it.isEnabled }} ACTIVE", color = MeterGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Module cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(modules) { mod ->
                DspRackModuleCard(
                    module = mod,
                    onToggle = { viewModel.toggleRackModule(mod.id) },
                    onEdit = {
                        when (mod.type) {
                            ModuleType.GRAPHIC_EQ -> viewModel.setTab("EQ")
                            ModuleType.PARAMETRIC_EQ -> viewModel.setTab("PARAMETRIC")
                            ModuleType.MID_PROCESSOR -> viewModel.setTab("TONE")
                            ModuleType.STEREO_WIDENER, ModuleType.MONO_ENGINE -> viewModel.setTab("MONO_STEREO")
                            ModuleType.COMPRESSOR, ModuleType.LIMITER, ModuleType.NOISE_GATE -> viewModel.setTab("DYNAMICS")
                            ModuleType.CROSSOVER -> viewModel.setTab("CROSSOVER")
                            else -> viewModel.setTab("HOME")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DspRackModuleCard(
    module: DspModuleInfo,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (module.isEnabled) StudioCardBgElevated else StudioCardBg, RoundedCornerShape(8.dp))
            .border(1.dp, if (module.isEnabled) MaroonPrimaryLight.copy(alpha = 0.5f) else StudioBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = module.name,
                color = if (module.isEnabled) Color.White else StudioSilverMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (module.isEnabled) "ACTIVE PROCESSING • FLOATING POINT" else "BYPASS / POWER OFF",
                color = if (module.isEnabled) MeterGreen else Color.DarkGray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCardBg),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("EDIT", fontSize = 10.sp, color = CyanAccent)
            }

            Switch(
                checked = module.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = MaroonPrimaryLight)
            )
        }
    }
}
