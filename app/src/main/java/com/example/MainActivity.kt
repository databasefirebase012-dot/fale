package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.data.AppPreferences
import com.example.ui.MainScreen
import com.example.ui.theme.getThemeColorScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = AppPreferences(this)

        setContent {
            var currentTheme by remember { mutableStateOf(prefs.themeOption) }

            MaterialTheme(
                colorScheme = getThemeColorScheme(currentTheme)
            ) {
                MainScreen(
                    prefs = prefs,
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme ->
                        currentTheme = newTheme
                        prefs.themeOption = newTheme
                    }
                )
            }
        }
    }
}
