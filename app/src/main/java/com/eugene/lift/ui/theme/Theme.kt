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
    onPrimaryContainer = DarkText,

    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0E7490), // Darker teal for containers
    onSecondaryContainer = DarkText,

    tertiary = Color(0xFF8B5CF6), // Lighter purple accent
    onTertiary = Color.White,

    error = WarningOrange,
    onError = Color.White,

    background = DarkBackground,
    onBackground = DarkText,

    surface = DarkSurface,
    onSurface = DarkText,

    surfaceVariant = Color(0xFF3D2E6B), // Slightly lighter surface
    onSurfaceVariant = Color(0xFFCAC4D0),

    outline = Color(0xFF6D5F8D),
    outlineVariant = Color(0xFF4A3B6E)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9D5FF), // Light purple for containers
    onPrimaryContainer = LightText,

    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE), // Light teal for containers
    onSecondaryContainer = Color(0xFF164E63),

    tertiary = Color(0xFFA78BFA), // Lighter purple accent
    onTertiary = Color.White,

    error = WarningOrange,
    onError = Color.White,

    background = LightBackground,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,

    surfaceVariant = Color(0xFFF5F3FF), // Very light purple
    onSurfaceVariant = Color(0xFF49454F),

    outline = Color(0xFFCAC4D0),
    outlineVariant = Color(0xFFE7E0EC)
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