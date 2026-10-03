package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

fun getLightColorSchemeForAccent(accent: AppAccent): ColorScheme {
    return lightColorScheme(
        primary = accent.lightPrimary,
        onPrimary = Color.White,
        primaryContainer = accent.lightPrimaryContainer,
        onPrimaryContainer = accent.lightOnPrimaryContainer,
        secondary = accent.lightSecondary,
        onSecondary = Color.White,
        secondaryContainer = accent.lightPrimaryContainer.copy(alpha = 0.65f),
        onSecondaryContainer = accent.lightOnPrimaryContainer,
        tertiary = LibraryTertiaryLight,
        onTertiary = LibraryOnTertiaryLight,
        tertiaryContainer = LibraryTertiaryContainerLight,
        onTertiaryContainer = LibraryOnTertiaryContainerLight,
        background = LibraryBackgroundLight,
        onBackground = LibraryOnBackgroundLight,
        surface = LibrarySurfaceLight,
        onSurface = LibraryOnSurfaceLight,
        surfaceVariant = LibrarySurfaceVariantLight,
        onSurfaceVariant = LibraryOnSurfaceVariantLight,
        outline = LibraryOutlineLight
    )
}

fun getDarkColorSchemeForAccent(accent: AppAccent): ColorScheme {
    return darkColorScheme(
        primary = accent.darkPrimary,
        onPrimary = Color(0xFF0F172A),
        primaryContainer = accent.darkPrimaryContainer,
        onPrimaryContainer = accent.darkOnPrimaryContainer,
        secondary = accent.darkSecondary,
        onSecondary = Color(0xFF0F172A),
        secondaryContainer = accent.darkPrimaryContainer.copy(alpha = 0.65f),
        onSecondaryContainer = accent.darkOnPrimaryContainer,
        tertiary = LibraryTertiaryDark,
        onTertiary = LibraryOnTertiaryDark,
        tertiaryContainer = LibraryTertiaryContainerDark,
        onTertiaryContainer = LibraryOnTertiaryContainerDark,
        background = LibraryBackgroundDark,
        onBackground = LibraryOnBackgroundDark,
        surface = LibrarySurfaceDark,
        onSurface = LibraryOnSurfaceDark,
        surfaceVariant = LibrarySurfaceVariantDark,
        onSurfaceVariant = LibraryOnSurfaceVariantDark,
        outline = LibraryOutlineDark
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accent: AppAccent = AppAccent.OCEAN,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        accent.isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> getDarkColorSchemeForAccent(accent)
        else -> getLightColorSchemeForAccent(accent)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Overload for backward compatibility with existing callers.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val mode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT
    val accent = if (dynamicColor) AppAccent.DYNAMIC else AppAccent.OCEAN
    MyApplicationTheme(themeMode = mode, accent = accent, content = content)
}

