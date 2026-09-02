package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.*
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AppTheme
import com.example.ui.theme.YouAndITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("you_and_i_prefs", Context.MODE_PRIVATE)
        val savedThemeName = prefs.getString("app_theme", AppTheme.SKY.name) ?: AppTheme.SKY.name
        val initialTheme = try { AppTheme.valueOf(savedThemeName) } catch (e: Exception) { AppTheme.SKY }

        val db = AppDatabase.getDatabase(applicationContext)
        val authRepo = AuthRepository(db)
        val discoveryRepo = DiscoveryRepository(db)
        val socialRepo = SocialRepository(db)
        val chatRepo = ChatRepository(db)
        val safetyRepo = SafetyRepository(db)

        setContent {
            val systemDark = isSystemInDarkTheme()
            val initialDark = prefs.getBoolean("is_dark_mode", systemDark)
            val initialCompact = prefs.getBoolean("is_compact_mode", false)

            var currentTheme by remember { mutableStateOf(initialTheme) }
            var isDarkMode by remember { mutableStateOf(initialDark) }
            var isCompactMode by remember { mutableStateOf(initialCompact) }

            YouAndITheme(
                appTheme = currentTheme,
                darkTheme = isDarkMode,
                compactMode = isCompactMode
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        authRepo = authRepo,
                        discoveryRepo = discoveryRepo,
                        socialRepo = socialRepo,
                        chatRepo = chatRepo,
                        safetyRepo = safetyRepo,
                        currentTheme = currentTheme,
                        isDarkMode = isDarkMode,
                        isCompactMode = isCompactMode,
                        onSelectTheme = { selectedTheme ->
                            currentTheme = selectedTheme
                            prefs.edit().putString("app_theme", selectedTheme.name).apply()
                        },
                        onToggleDarkMode = { darkMode ->
                            isDarkMode = darkMode
                            prefs.edit().putBoolean("is_dark_mode", darkMode).apply()
                        },
                        onToggleCompactMode = { compact ->
                            isCompactMode = compact
                            prefs.edit().putBoolean("is_compact_mode", compact).apply()
                        }
                    )
                }
            }
        }
    }
}

