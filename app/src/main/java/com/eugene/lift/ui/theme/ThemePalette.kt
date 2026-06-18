package com.eugene.lift.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.eugene.lift.R
import com.eugene.lift.domain.model.AppTheme

@Immutable
data class LiftThemeSpec(
    val theme: AppTheme,
    @StringRes val nameRes: Int,
    val lightScheme: ColorScheme,
    val darkScheme: ColorScheme,
    val previewColors: List<Color>
)

private val orcaTheme = LiftThemeSpec(
    theme = AppTheme.ORCA,
    nameRes = R.string.theme_orca,
    lightScheme = lightColorScheme(
        primary = Color(0xFF17191E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE3E5EA),
        onPrimaryContainer = Color(0xFF17191E),
        secondary = Color(0xFF5A6270),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDDE3EC),
        onSecondaryContainer = Color(0xFF131A24),
        tertiary = Color(0xFF7A8593),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFDDE3EA),
        onTertiaryContainer = Color(0xFF171E27),
        background = Color(0xFFF7F7F8),
        onBackground = Color(0xFF17191E),
        surface = Color(0xFFF7F7F8),
        onSurface = Color(0xFF17191E),
        surfaceVariant = Color(0xFFE1E3E7),
        onSurfaceVariant = Color(0xFF45474E),
        outline = Color(0xFF757780),
        outlineVariant = Color(0xFFC5C7CD)
    ),
    darkScheme = darkColorScheme(
        primary = Color(0xFFF3F4F6),
        onPrimary = Color(0xFF111317),
        primaryContainer = Color(0xFF2B3038),
        onPrimaryContainer = Color(0xFFE5E7EB),
        secondary = Color(0xFFBFC7D4),
        onSecondary = Color(0xFF28313B),
        secondaryContainer = Color(0xFF404955),
        onSecondaryContainer = Color(0xFFDDE3EC),
        tertiary = Color(0xFFC5CCD6),
        onTertiary = Color(0xFF2E3640),
        tertiaryContainer = Color(0xFF454E5A),
        onTertiaryContainer = Color(0xFFDDE3EA),
        background = Color(0xFF111317),
        onBackground = Color(0xFFE3E5EA),
        surface = Color(0xFF111317),
        onSurface = Color(0xFFE3E5EA),
        surfaceVariant = Color(0xFF44474E),
        onSurfaceVariant = Color(0xFFC5C7CD),
        outline = Color(0xFF8F9198),
        outlineVariant = Color(0xFF44474E)
    ),
    previewColors = listOf(
        Color(0xFF17191E),
        Color(0xFF5A6270),
        Color(0xFFE3E5EA),
        Color(0xFFF7F7F8)
    )
)

private val makoTheme = LiftThemeSpec(
    theme = AppTheme.MAKO,
    nameRes = R.string.theme_mako,
    lightScheme = lightColorScheme(
        primary = Color(0xFF0059B3),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8E3FF),
        onPrimaryContainer = Color(0xFF001A41),
        secondary = Color(0xFF006D77),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFB6F0F2),
        onSecondaryContainer = Color(0xFF002022),
        tertiary = Color(0xFF5E5CE6),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE3E0FF),
        onTertiaryContainer = Color(0xFF19155F),
        background = Color(0xFFF6FAFF),
        onBackground = Color(0xFF121C28),
        surface = Color(0xFFF6FAFF),
        onSurface = Color(0xFF121C28),
        surfaceVariant = Color(0xFFDCE4F0),
        onSurfaceVariant = Color(0xFF414B57),
        outline = Color(0xFF717B87),
        outlineVariant = Color(0xFFC0C8D4)
    ),
    darkScheme = darkColorScheme(
        primary = Color(0xFFA9C7FF),
        onPrimary = Color(0xFF002F67),
        primaryContainer = Color(0xFF00458D),
        onPrimaryContainer = Color(0xFFD8E3FF),
        secondary = Color(0xFF80D4D7),
        onSecondary = Color(0xFF003739),
        secondaryContainer = Color(0xFF004F56),
        onSecondaryContainer = Color(0xFFB6F0F2),
        tertiary = Color(0xFFC5C2FF),
        onTertiary = Color(0xFF2E2A86),
        tertiaryContainer = Color(0xFF4644B1),
        onTertiaryContainer = Color(0xFFE3E0FF),
        background = Color(0xFF0D141D),
        onBackground = Color(0xFFDDE3EC),
        surface = Color(0xFF0D141D),
        onSurface = Color(0xFFDDE3EC),
        surfaceVariant = Color(0xFF414B57),
        onSurfaceVariant = Color(0xFFC0C8D4),
        outline = Color(0xFF8B949F),
        outlineVariant = Color(0xFF414B57)
    ),
    previewColors = listOf(
        Color(0xFF0059B3),
        Color(0xFF006D77),
        Color(0xFF5E5CE6),
        Color(0xFFD8E3FF)
    )
)

private val foxTheme = LiftThemeSpec(
    theme = AppTheme.FOX,
    nameRes = R.string.theme_fox,
    lightScheme = lightColorScheme(
        primary = Color(0xFFBC4D2E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBD1),
        onPrimaryContainer = Color(0xFF3F0D00),
        secondary = Color(0xFF9F5A00),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDDB6),
        onSecondaryContainer = Color(0xFF321A00),
        tertiary = Color(0xFFAA3362),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD8E6),
        onTertiaryContainer = Color(0xFF3E001D),
        background = Color(0xFFFFF8F6),
        onBackground = Color(0xFF251814),
        surface = Color(0xFFFFF8F6),
        onSurface = Color(0xFF251814),
        surfaceVariant = Color(0xFFF1DED8),
        onSurfaceVariant = Color(0xFF53433E),
        outline = Color(0xFF85736D),
        outlineVariant = Color(0xFFD4C3BD)
    ),
    darkScheme = darkColorScheme(
        primary = Color(0xFFFFB59F),
        onPrimary = Color(0xFF6D1F05),
        primaryContainer = Color(0xFF8E3418),
        onPrimaryContainer = Color(0xFFFFDBD1),
        secondary = Color(0xFFFFB95C),
        onSecondary = Color(0xFF542B00),
        secondaryContainer = Color(0xFF784000),
        onSecondaryContainer = Color(0xFFFFDDB6),
        tertiary = Color(0xFFFFB0CF),
        onTertiary = Color(0xFF67002F),
        tertiaryContainer = Color(0xFF8A1D4A),
        onTertiaryContainer = Color(0xFFFFD8E6),
        background = Color(0xFF1C110D),
        onBackground = Color(0xFFF1DFD8),
        surface = Color(0xFF1C110D),
        onSurface = Color(0xFFF1DFD8),
        surfaceVariant = Color(0xFF53433E),
        onSurfaceVariant = Color(0xFFD4C3BD),
        outline = Color(0xFF9E8C86),
        outlineVariant = Color(0xFF53433E)
    ),
    previewColors = listOf(
        Color(0xFFBC4D2E),
        Color(0xFF9F5A00),
        Color(0xFFAA3362),
        Color(0xFFFFDBD1)
    )
)

private val viperTheme = LiftThemeSpec(
    theme = AppTheme.VIPER,
    nameRes = R.string.theme_viper,
    lightScheme = lightColorScheme(
        primary = Color(0xFF2E7D32),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC8F0C8),
        onPrimaryContainer = Color(0xFF002106),
        secondary = Color(0xFF0E7462),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF9EF2DD),
        onSecondaryContainer = Color(0xFF002019),
        tertiary = Color(0xFF6A59C7),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE4DFFF),
        onTertiaryContainer = Color(0xFF23155F),
        background = Color(0xFFF7FCF6),
        onBackground = Color(0xFF132015),
        surface = Color(0xFFF7FCF6),
        onSurface = Color(0xFF132015),
        surfaceVariant = Color(0xFFDCE8D9),
        onSurfaceVariant = Color(0xFF414D41),
        outline = Color(0xFF717D71),
        outlineVariant = Color(0xFFC0CCBE)
    ),
    darkScheme = darkColorScheme(
        primary = Color(0xFFAEE5AC),
        onPrimary = Color(0xFF093911),
        primaryContainer = Color(0xFF1B6322),
        onPrimaryContainer = Color(0xFFC8F0C8),
        secondary = Color(0xFF82D6C2),
        onSecondary = Color(0xFF00382C),
        secondaryContainer = Color(0xFF005142),
        onSecondaryContainer = Color(0xFF9EF2DD),
        tertiary = Color(0xFFC7BFFF),
        onTertiary = Color(0xFF382B91),
        tertiaryContainer = Color(0xFF5142AD),
        onTertiaryContainer = Color(0xFFE4DFFF),
        background = Color(0xFF0D150F),
        onBackground = Color(0xFFDCE8D9),
        surface = Color(0xFF0D150F),
        onSurface = Color(0xFFDCE8D9),
        surfaceVariant = Color(0xFF414D41),
        onSurfaceVariant = Color(0xFFC0CCBE),
        outline = Color(0xFF8A968A),
        outlineVariant = Color(0xFF414D41)
    ),
    previewColors = listOf(
        Color(0xFF2E7D32),
        Color(0xFF0E7462),
        Color(0xFF6A59C7),
        Color(0xFFC8F0C8)
    )
)

private val lionTheme = LiftThemeSpec(
    theme = AppTheme.LION,
    nameRes = R.string.theme_lion,
    lightScheme = lightColorScheme(
        primary = Color(0xFFA46300),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDDB7),
        onPrimaryContainer = Color(0xFF331D00),
        secondary = Color(0xFF8C6F00),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF6E18A),
        onSecondaryContainer = Color(0xFF2B2100),
        tertiary = Color(0xFF8A5CB0),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF2DAFF),
        onTertiaryContainer = Color(0xFF321047),
        background = Color(0xFFFFF9F1),
        onBackground = Color(0xFF221A0F),
        surface = Color(0xFFFFF9F1),
        onSurface = Color(0xFF221A0F),
        surfaceVariant = Color(0xFFEDE0C9),
        onSurfaceVariant = Color(0xFF504638),
        outline = Color(0xFF827768),
        outlineVariant = Color(0xFFD0C4AD)
    ),
    darkScheme = darkColorScheme(
        primary = Color(0xFFFFB95E),
        onPrimary = Color(0xFF552D00),
        primaryContainer = Color(0xFF7A4700),
        onPrimaryContainer = Color(0xFFFFDDB7),
        secondary = Color(0xFFDAC55F),
        onSecondary = Color(0xFF473800),
        secondaryContainer = Color(0xFF665200),
        onSecondaryContainer = Color(0xFFF6E18A),
        tertiary = Color(0xFFDDB7FF),
        onTertiary = Color(0xFF4E2D73),
        tertiaryContainer = Color(0xFF683F92),
        onTertiaryContainer = Color(0xFFF2DAFF),
        background = Color(0xFF17120B),
        onBackground = Color(0xFFEDE0C9),
        surface = Color(0xFF17120B),
        onSurface = Color(0xFFEDE0C9),
        surfaceVariant = Color(0xFF504638),
        onSurfaceVariant = Color(0xFFD0C4AD),
        outline = Color(0xFF9B8F7F),
        outlineVariant = Color(0xFF504638)
    ),
    previewColors = listOf(
        Color(0xFFA46300),
        Color(0xFF8C6F00),
        Color(0xFF8A5CB0),
        Color(0xFFFFDDB7)
    )
)

private val themeSpecs = listOf(orcaTheme, makoTheme, foxTheme, viperTheme, lionTheme)

fun liftThemeSpecFor(theme: AppTheme): LiftThemeSpec =
    themeSpecs.firstOrNull { it.theme == theme } ?: orcaTheme

fun AppTheme.colorScheme(darkTheme: Boolean, highContrast: Boolean = false): ColorScheme {
    val spec = liftThemeSpecFor(this)
    val scheme = if (darkTheme) spec.darkScheme else spec.lightScheme
    return if (highContrast) scheme.asHighContrast(darkTheme) else scheme
}

private fun ColorScheme.asHighContrast(darkTheme: Boolean): ColorScheme = copy(
    onSurface = if (darkTheme) Color(0xFFFFFFFF) else Color(0xFF111111),
    onSurfaceVariant = if (darkTheme) Color(0xFFF3F4F6) else Color(0xFF1B1B1D),
    outline = if (darkTheme) Color(0xFFE3E5EA) else Color(0xFF26272B)
)
