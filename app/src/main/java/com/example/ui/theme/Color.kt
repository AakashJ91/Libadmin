package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemeMode(val displayName: String, val subtitle: String) {
    SYSTEM("System Default", "Follows device light/dark appearance"),
    LIGHT("Light Theme", "Crisp paper & clean contrast"),
    DARK("Dark Theme", "OLED midnight slate & low glare")
}

enum class AppAccent(
    val title: String,
    val previewColor: Color,
    val lightPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val lightSecondary: Color,
    val darkPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
    val darkSecondary: Color,
    val isDynamic: Boolean = false
) {
    OCEAN(
        title = "Ocean Scholar",
        previewColor = Color(0xFF0E607A),
        lightPrimary = Color(0xFF0E607A),
        lightPrimaryContainer = Color(0xFFC3E8F6),
        lightOnPrimaryContainer = Color(0xFF001F2A),
        lightSecondary = Color(0xFFB45309),
        darkPrimary = Color(0xFF72D5F6),
        darkPrimaryContainer = Color(0xFF004D63),
        darkOnPrimaryContainer = Color(0xFFC3E8F6),
        darkSecondary = Color(0xFFFBBF24)
    ),
    INDIGO(
        title = "Royal Indigo",
        previewColor = Color(0xFF4338CA),
        lightPrimary = Color(0xFF4338CA),
        lightPrimaryContainer = Color(0xFFE0E7FF),
        lightOnPrimaryContainer = Color(0xFF1E1B4B),
        lightSecondary = Color(0xFF0284C7),
        darkPrimary = Color(0xFFA5B4FC),
        darkPrimaryContainer = Color(0xFF312E81),
        darkOnPrimaryContainer = Color(0xFFE0E7FF),
        darkSecondary = Color(0xFF38BDF8)
    ),
    EMERALD(
        title = "Forest Emerald",
        previewColor = Color(0xFF059669),
        lightPrimary = Color(0xFF059669),
        lightPrimaryContainer = Color(0xFFD1FAE5),
        lightOnPrimaryContainer = Color(0xFF064E3B),
        lightSecondary = Color(0xFF0D9488),
        darkPrimary = Color(0xFF6EE7B7),
        darkPrimaryContainer = Color(0xFF065F46),
        darkOnPrimaryContainer = Color(0xFFD1FAE5),
        darkSecondary = Color(0xFF2DD4BF)
    ),
    AMBER(
        title = "Sunset Bronze",
        previewColor = Color(0xFFD97706),
        lightPrimary = Color(0xFFD97706),
        lightPrimaryContainer = Color(0xFFFEF3C7),
        lightOnPrimaryContainer = Color(0xFF78350F),
        lightSecondary = Color(0xFFDC2626),
        darkPrimary = Color(0xFFFCD34D),
        darkPrimaryContainer = Color(0xFF78350F),
        darkOnPrimaryContainer = Color(0xFFFEF3C7),
        darkSecondary = Color(0xFFF87171)
    ),
    ROSE(
        title = "Crimson Rose",
        previewColor = Color(0xFFE11D48),
        lightPrimary = Color(0xFFE11D48),
        lightPrimaryContainer = Color(0xFFFFE4E6),
        lightOnPrimaryContainer = Color(0xFF881337),
        lightSecondary = Color(0xFF9333EA),
        darkPrimary = Color(0xFFFDA4AF),
        darkPrimaryContainer = Color(0xFF881337),
        darkOnPrimaryContainer = Color(0xFFFFE4E6),
        darkSecondary = Color(0xFFC084FC)
    ),
    VIOLET(
        title = "Royal Violet",
        previewColor = Color(0xFF7C3AED),
        lightPrimary = Color(0xFF7C3AED),
        lightPrimaryContainer = Color(0xFFEDE9FE),
        lightOnPrimaryContainer = Color(0xFF4C1D95),
        lightSecondary = Color(0xFF2563EB),
        darkPrimary = Color(0xFFC4B5FD),
        darkPrimaryContainer = Color(0xFF5B21B6),
        darkOnPrimaryContainer = Color(0xFFEDE9FE),
        darkSecondary = Color(0xFF60A5FA)
    ),
    DYNAMIC(
        title = "System Dynamic",
        previewColor = Color(0xFF0284C7),
        lightPrimary = Color(0xFF0284C7),
        lightPrimaryContainer = Color(0xFFBAE6FD),
        lightOnPrimaryContainer = Color(0xFF0369A1),
        lightSecondary = Color(0xFF0D9488),
        darkPrimary = Color(0xFF7DD3FC),
        darkPrimaryContainer = Color(0xFF0369A1),
        darkOnPrimaryContainer = Color(0xFFBAE6FD),
        darkSecondary = Color(0xFF2DD4BF),
        isDynamic = true
    )
}

// Library Scholar Palette - Light
val LibraryPrimaryLight = Color(0xFF0E607A)
val LibraryOnPrimaryLight = Color(0xFFFFFFFF)
val LibraryPrimaryContainerLight = Color(0xFFC3E8F6)
val LibraryOnPrimaryContainerLight = Color(0xFF001F2A)

val LibrarySecondaryLight = Color(0xFFB45309) // Warm brass/amber
val LibraryOnSecondaryLight = Color(0xFFFFFFFF)
val LibrarySecondaryContainerLight = Color(0xFFFED7AA)
val LibraryOnSecondaryContainerLight = Color(0xFF451A03)

val LibraryTertiaryLight = Color(0xFF1E6F50) // Forest green
val LibraryOnTertiaryLight = Color(0xFFFFFFFF)
val LibraryTertiaryContainerLight = Color(0xFFB7F2D2)
val LibraryOnTertiaryContainerLight = Color(0xFF002114)

val LibraryBackgroundLight = Color(0xFFF8FAFC)
val LibraryOnBackgroundLight = Color(0xFF0F172A)
val LibrarySurfaceLight = Color(0xFFFFFFFF)
val LibraryOnSurfaceLight = Color(0xFF0F172A)
val LibrarySurfaceVariantLight = Color(0xFFF1F5F9)
val LibraryOnSurfaceVariantLight = Color(0xFF475569)
val LibraryOutlineLight = Color(0xFFCBD5E1)

// Library Scholar Palette - Dark
val LibraryPrimaryDark = Color(0xFF72D5F6)
val LibraryOnPrimaryDark = Color(0xFF003544)
val LibraryPrimaryContainerDark = Color(0xFF004D63)
val LibraryOnPrimaryContainerDark = Color(0xFFC3E8F6)

val LibrarySecondaryDark = Color(0xFFFBBF24)
val LibraryOnSecondaryDark = Color(0xFF451A03)
val LibrarySecondaryContainerDark = Color(0xFF78350F)
val LibraryOnSecondaryContainerDark = Color(0xFFFED7AA)

val LibraryTertiaryDark = Color(0xFF86E2BA)
val LibraryOnTertiaryDark = Color(0xFF003824)
val LibraryTertiaryContainerDark = Color(0xFF005236)
val LibraryOnTertiaryContainerDark = Color(0xFFB7F2D2)

val LibraryBackgroundDark = Color(0xFF0B1321)
val LibraryOnBackgroundDark = Color(0xFFF1F5F9)
val LibrarySurfaceDark = Color(0xFF131F33)
val LibraryOnSurfaceDark = Color(0xFFF1F5F9)
val LibrarySurfaceVariantDark = Color(0xFF1E2D47)
val LibraryOnSurfaceVariantDark = Color(0xFF94A3B8)
val LibraryOutlineDark = Color(0xFF334155)

// Status Colors
val StatusActive = Color(0xFF16A34A)
val StatusActiveContainer = Color(0xFFDCFCE7)
val StatusExpiringSoon = Color(0xFFD97706)
val StatusExpiringSoonContainer = Color(0xFFFEF3C7)
val StatusExpired = Color(0xFFDC2626)
val StatusExpiredContainer = Color(0xFFFEE2E2)

