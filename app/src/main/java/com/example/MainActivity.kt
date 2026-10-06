package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.AnalyzerScreen
import com.example.ui.screens.CrossoverScreen
import com.example.ui.screens.DynamicsScreen
import com.example.ui.screens.GraphicEqScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonoStereoScreen
import com.example.ui.screens.ParametricEqScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.QuickToneScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.ProEqStudioTheme
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBgElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSilver
import com.example.ui.theme.StudioSilverMuted
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestRecordPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.engine.updateConnectedDeviceInfo()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestRecordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            ProEqStudioTheme {
                StudioAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudioAppScaffold(viewModel: MainViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val isPowerOn by viewModel.isPowerOn.collectAsState()
    val navScrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = StudioDarkBg,
        topBar = {
            // Studio Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioCardBg)
                    .border(1.dp, StudioBorder)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = "Studio Logo",
                        tint = MaroonPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "PRO EQ STUDIO",
                            color = StudioSilver,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "MASTERING DSP WORKSTATION",
                            color = StudioSilverMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Main Master Power Switch
                Button(
                    onClick = { viewModel.togglePower() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPowerOn) MeterGreen else Color(0xFF2A2A36)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(34.dp).testTag("master_power_button")
                ) {
                    Icon(
                        Icons.Default.PowerSettingsNew,
                        contentDescription = "Power",
                        tint = if (isPowerOn) Color.Black else Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = if (isPowerOn) "ON" else "STANDBY",
                        color = if (isPowerOn) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        },
        bottomBar = {
            // Horizontal Studio Navigation Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioCardBg)
                    .border(1.dp, StudioBorder)
                    .horizontalScroll(navScrollState)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StudioNavTab("HOME", "HOME", Icons.Default.Home, activeTab) { viewModel.setTab("HOME") }
                StudioNavTab("EQ", "EQ", Icons.Default.GraphicEq, activeTab) { viewModel.setTab("EQ") }
                StudioNavTab("PARAMETRIC", "PEQ", Icons.Default.Equalizer, activeTab) { viewModel.setTab("PARAMETRIC") }
                StudioNavTab("MONO_STEREO", "M/S", Icons.Default.Headphones, activeTab) { viewModel.setTab("MONO_STEREO") }
                StudioNavTab("TONE", "TONE", Icons.Default.Tune, activeTab) { viewModel.setTab("TONE") }
                StudioNavTab("DYNAMICS", "DYN", Icons.Default.Speaker, activeTab) { viewModel.setTab("DYNAMICS") }
                StudioNavTab("CROSSOVER", "XOVER", Icons.Default.Equalizer, activeTab) { viewModel.setTab("CROSSOVER") }
                StudioNavTab("ANALYZER", "FFT", Icons.Default.Speed, activeTab) { viewModel.setTab("ANALYZER") }
                StudioNavTab("PRESETS", "PRESET", Icons.Default.List, activeTab) { viewModel.setTab("PRESETS") }
                StudioNavTab("SETTINGS", "SETUP", Icons.Default.Settings, activeTab) { viewModel.setTab("SETTINGS") }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "HOME" -> HomeScreen(viewModel = viewModel)
                "EQ" -> GraphicEqScreen(viewModel = viewModel)
                "PARAMETRIC" -> ParametricEqScreen(viewModel = viewModel)
                "MONO_STEREO" -> MonoStereoScreen(viewModel = viewModel)
                "TONE" -> QuickToneScreen(viewModel = viewModel)
                "DYNAMICS" -> DynamicsScreen(viewModel = viewModel)
                "CROSSOVER" -> CrossoverScreen(viewModel = viewModel)
                "ANALYZER" -> AnalyzerScreen(viewModel = viewModel)
                "PRESETS" -> PresetsScreen(viewModel = viewModel)
                "SETTINGS" -> SettingsScreen(viewModel = viewModel)
                else -> HomeScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudioNavTab(
    tabId: String,
    title: String,
    icon: ImageVector,
    activeTab: String,
    onClick: () -> Unit
) {
    val isSelected = tabId == activeTab
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaroonPrimary else StudioCardBgElevated
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(40.dp).testTag("tab_$tabId")
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = if (isSelected) Color.White else StudioSilverMuted,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else StudioSilverMuted,
            fontWeight = FontWeight.Bold
        )
    }
}
