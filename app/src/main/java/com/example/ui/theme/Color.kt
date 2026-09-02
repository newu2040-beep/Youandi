package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Baseline Colors
val PrimaryBlue = Color(0xFF4F75FF)
val SecondaryBlue = Color(0xFF8DA8FF)
val IceBackground = Color(0xFFF3F7FF)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF17213D)
val TextSecondary = Color(0xFF69738A)
val LavenderAccent = Color(0xFFDCDFFF)
val MintAccent = Color(0xFFCDEFE5)
val SoftPink = Color(0xFFFFD1DC)

// Dark Baseline Colors
val DarkBackground = Color(0xFF0D1220)
val DarkSurface = Color(0xFF171D2D)
val DarkSurfaceVariant = Color(0xFF22293D)
val TextPrimaryDark = Color(0xFFF0F4FF)
val TextSecondaryDark = Color(0xFFA0AABF)

// Theme Variations with distinct light and dark backgrounds & surfaces
enum class AppTheme(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val lightBg: Color,
    val lightSurface: Color,
    val darkBg: Color,
    val darkSurface: Color
) {
    SKY(
        displayName = "Sky",
        primaryColor = Color(0xFF4F75FF),
        secondaryColor = Color(0xFF8DA8FF),
        lightBg = Color(0xFFF3F7FF),
        lightSurface = Color(0xFFFFFFFF),
        darkBg = Color(0xFF0B1021),
        darkSurface = Color(0xFF151C33)
    ),
    ICE(
        displayName = "Ice",
        primaryColor = Color(0xFF0284C7),
        secondaryColor = Color(0xFF38BDF8),
        lightBg = Color(0xFFF0F9FF),
        lightSurface = Color(0xFFFFFFFF),
        darkBg = Color(0xFF081726),
        darkSurface = Color(0xFF0F243A)
    ),
    LAVENDER(
        displayName = "Lavender",
        primaryColor = Color(0xFF7C3AED),
        secondaryColor = Color(0xFFA78BFA),
        lightBg = Color(0xFFFAF5FF),
        lightSurface = Color(0xFFFFFFFF),
        darkBg = Color(0xFF140D24),
        darkSurface = Color(0xFF201638)
    ),
    MINT(
        displayName = "Mint",
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF34D399),
        lightBg = Color(0xFFECFDF5),
        lightSurface = Color(0xFFFFFFFF),
        darkBg = Color(0xFF061A14),
        darkSurface = Color(0xFF0E2E23)
    ),
    CLOUD(
        displayName = "Cloud",
        primaryColor = Color(0xFF475569),
        secondaryColor = Color(0xFF94A3B8),
        lightBg = Color(0xFFF8FAFC),
        lightSurface = Color(0xFFFFFFFF),
        darkBg = Color(0xFF0F172A),
        darkSurface = Color(0xFF1E293B)
    ),
    MIDNIGHT(
        displayName = "Midnight",
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFFA5B4FC),
        lightBg = Color(0xFFEEF2FF),
        lightSurface = Color(0xFFFFFFFF),
        darkBg = Color(0xFF0A0C27),
        darkSurface = Color(0xFF141842)
    )
}

