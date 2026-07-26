package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.example.data.AppThemeOption

fun getThemeColorScheme(option: AppThemeOption): ColorScheme {
    return when (option) {
        AppThemeOption.CYAN -> darkColorScheme(
            primary = Color(0xFF06B6D4),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF083344),
            onPrimaryContainer = Color(0xFFCFFAFE),
            secondary = Color(0xFF22D3EE),
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF1F5F9)
        )
        AppThemeOption.RED -> darkColorScheme(
            primary = Color(0xFFEF4444),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF450A0A),
            onPrimaryContainer = Color(0xFFFEE2E2),
            secondary = Color(0xFFF87171),
            background = Color(0xFF0F0F17),
            surface = Color(0xFF1F1924),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF1F5F9)
        )
        AppThemeOption.GREEN -> darkColorScheme(
            primary = Color(0xFF10B981),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF064E3B),
            onPrimaryContainer = Color(0xFFD1FAE5),
            secondary = Color(0xFF34D399),
            background = Color(0xFF061A14),
            surface = Color(0xFF0D2D23),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF1F5F9)
        )
        AppThemeOption.PURPLE -> darkColorScheme(
            primary = Color(0xFFA855F7),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF3B0764),
            onPrimaryContainer = Color(0xFFF3E8FF),
            secondary = Color(0xFFC084FC),
            background = Color(0xFF12091F),
            surface = Color(0xFF211338),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF1F5F9)
        )
        AppThemeOption.AMBER -> darkColorScheme(
            primary = Color(0xFFF59E0B),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF451A03),
            onPrimaryContainer = Color(0xFFFEF3C7),
            secondary = Color(0xFFFBBF24),
            background = Color(0xFF1A130A),
            surface = Color(0xFF2B2013),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF1F5F9)
        )
        AppThemeOption.BLUE -> darkColorScheme(
            primary = Color(0xFF3B82F6),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF1E3A8A),
            onPrimaryContainer = Color(0xFFDBEAFE),
            secondary = Color(0xFF60A5FA),
            background = Color(0xFF0B132B),
            surface = Color(0xFF1C2541),
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF1F5F9)
        )
    }
}
