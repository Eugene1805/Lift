package com.eugene.lift.ui.theme

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
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5B21B6), // Darker purple for containers
    onPrimaryContainer = Color(0xFFE9D5FF),

    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0E7490), // Darker teal for containers
    onSecondaryContainer = Color(0xFFCFFAFE),

    tertiary = Color(0xFF8B5CF6), // Lighter purple accent
    onTertiary = Color.White,

    error = WarningOrange,
    onError = Color.White,

    background = DarkBackground, // Pure black
    onBackground = DarkText,

    surface = DarkSurface, // Very dark gray for cards
    onSurface = DarkText,

    surfaceVariant = Color(0xFF2A2A2A), // Slightly lighter than surface
    onSurfaceVariant = Color(0xFFB0B0B0),

    outline = Color(0xFF3D3D3D),
    outlineVariant = Color(0xFF2A2A2A)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9D5FF), // Light purple for containers
    onPrimaryContainer = Color(0xFF312E81),

    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE), // Light teal for containers
    onSecondaryContainer = Color(0xFF164E63),

    tertiary = Color(0xFFA78BFA), // Lighter purple accent
    onTertiary = Color.White,

    error = WarningOrange,
    onError = Color.White,

    background = LightBackground, // Pure white
    onBackground = LightText,

    surface = LightSurface, // Very light gray for cards
    onSurface = LightText,

    surfaceVariant = Color(0xFFF5F5F5), // Slightly darker than surface
    onSurfaceVariant = Color(0xFF5A5A5A),

    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF0F0F0)
)

@Composable
fun LiftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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