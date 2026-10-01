package com.canoezequiel.moodflow.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MimunGreen,
    secondary = MimunSage,
    tertiary = MimunAccent,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF242424),
    onBackground = MimunBackground,
    onSurface = MimunSurface
)

private val LightColorScheme = lightColorScheme(
    primary = MimunGreen,
    onPrimary = Color.White,
    primaryContainer = MimunPrimaryContainer,
    onPrimaryContainer = MimunTextPrimary,
    secondary = MimunSage,
    onSecondary = Color.White,
    secondaryContainer = MimunSageLight,
    onSecondaryContainer = MimunTextPrimary,
    tertiary = MimunAccent,
    onTertiary = MimunTextPrimary,
    background = MimunBackground,
    onBackground = MimunTextPrimary,
    surface = MimunSurface,
    onSurface = MimunTextPrimary,
    surfaceVariant = MimunSurfaceVariant,
    onSurfaceVariant = MimunTextSecondary,
    outline = MimunBorder,
    outlineVariant = MimunBorder
)

@Composable
fun MoodFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled by default to preserve Mimun's official brand palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}