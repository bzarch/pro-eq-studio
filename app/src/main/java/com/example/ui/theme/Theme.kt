package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MaroonPrimary,
    onPrimary = Color.White,
    primaryContainer = MaroonPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = StudioSilver,
    onSecondary = Color.Black,
    surface = StudioCardBg,
    onSurface = StudioSilver,
    background = StudioDarkBg,
    onBackground = StudioSilver,
    outline = StudioBorder
)

@Composable
fun ProEqStudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
