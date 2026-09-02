package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

val LocalCompactMode = staticCompositionLocalOf { false }

@Composable
fun YouAndITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.SKY,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isCompactScreen = configuration.screenWidthDp < 380 || configuration.screenHeightDp < 680

    val lightColorScheme = lightColorScheme(
        primary = appTheme.primaryColor,
        onPrimary = Color.White,
        primaryContainer = appTheme.secondaryColor.copy(alpha = 0.25f),
        onPrimaryContainer = appTheme.primaryColor,
        secondary = appTheme.secondaryColor,
        onSecondary = TextPrimary,
        tertiary = LavenderAccent,
        background = appTheme.lightBg,
        onBackground = TextPrimary,
        surface = appTheme.lightSurface,
        onSurface = TextPrimary,
        surfaceVariant = appTheme.lightBg,
        onSurfaceVariant = TextSecondary,
        outline = Color(0xFFE2E8F0)
    )

    val darkColorScheme = darkColorScheme(
        primary = appTheme.primaryColor,
        onPrimary = Color.White,
        primaryContainer = appTheme.darkSurface,
        onPrimaryContainer = appTheme.secondaryColor,
        secondary = appTheme.secondaryColor,
        onSecondary = TextPrimaryDark,
        tertiary = LavenderAccent,
        background = appTheme.darkBg,
        onBackground = TextPrimaryDark,
        surface = appTheme.darkSurface,
        onSurface = TextPrimaryDark,
        surfaceVariant = appTheme.darkSurface,
        onSurfaceVariant = TextSecondaryDark,
        outline = Color(0xFF2E384D)
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    CompositionLocalProvider(
        LocalCompactMode provides isCompactScreen
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

