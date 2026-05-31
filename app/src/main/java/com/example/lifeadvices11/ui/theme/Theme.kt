package com.example.lifeadvices11.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LavenderPrimary,
    secondary = SkyCard,
    tertiary = PeachCard,
    background = LavenderSurface,
    surface = WhiteSoft,
    surfaceVariant = LavenderSoft,
    onPrimary = WhiteSoft,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = MutedInk,
    outline = DividerSoft
)

private val LightColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    secondary = SkyCard,
    tertiary = PeachCard,
    background = LavenderSurface,
    surface = WhiteSoft,
    surfaceVariant = LavenderSoft,
    primaryContainer = LavenderSoft,
    secondaryContainer = SkyCard,
    tertiaryContainer = PeachCard,
    onPrimary = WhiteSoft,
    onSecondary = Ink,
    onTertiary = Ink,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = MutedInk,
    outline = DividerSoft
)

@Composable
fun LifeAdvices11Theme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
