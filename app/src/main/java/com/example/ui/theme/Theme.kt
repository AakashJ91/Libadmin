package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LibraryPrimaryDark,
    onPrimary = LibraryOnPrimaryDark,
    primaryContainer = LibraryPrimaryContainerDark,
    onPrimaryContainer = LibraryOnPrimaryContainerDark,
    secondary = LibrarySecondaryDark,
    onSecondary = LibraryOnSecondaryDark,
    secondaryContainer = LibrarySecondaryContainerDark,
    onSecondaryContainer = LibraryOnSecondaryContainerDark,
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

private val LightColorScheme = lightColorScheme(
    primary = LibraryPrimaryLight,
    onPrimary = LibraryOnPrimaryLight,
    primaryContainer = LibraryPrimaryContainerLight,
    onPrimaryContainer = LibraryOnPrimaryContainerLight,
    secondary = LibrarySecondaryLight,
    onSecondary = LibraryOnSecondaryLight,
    secondaryContainer = LibrarySecondaryContainerLight,
    onSecondaryContainer = LibraryOnSecondaryContainerLight,
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our rich dedicated Library theme by default
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
