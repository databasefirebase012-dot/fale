package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeOption(val displayName: String) {
    CYAN("Cyan Modern"),
    RED("Red Crimson"),
    GREEN("Emerald Green"),
    PURPLE("Royal Purple"),
    AMBER("Amber Gold"),
    BLUE("Neon Blue")
}

enum class GameTargetOption(val displayName: String, val packageName: String) {
    FREE_FIRE_ORI("Free Fire (Original)", "com.dts.freefireth"),
    FREE_FIRE_MAX("Free Fire MAX", "com.dts.freefiremax")
}

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("xrans_fl_prefs", Context.MODE_PRIVATE)

    var themeOption: AppThemeOption
        get() {
            val name = prefs.getString("selected_theme", AppThemeOption.CYAN.name) ?: AppThemeOption.CYAN.name
            return try { AppThemeOption.valueOf(name) } catch (e: Exception) { AppThemeOption.CYAN }
        }
        set(value) {
            prefs.edit().putString("selected_theme", value.name).apply()
        }

    var gameTarget: GameTargetOption
        get() {
            val name = prefs.getString("game_target", GameTargetOption.FREE_FIRE_ORI.name) ?: GameTargetOption.FREE_FIRE_ORI.name
            return try { GameTargetOption.valueOf(name) } catch (e: Exception) { GameTargetOption.FREE_FIRE_ORI }
        }
        set(value) {
            prefs.edit().putString("game_target", value.name).apply()
        }

    var holdDurationMs: Long
        get() = prefs.getLong("hold_duration_ms", 800L)
        set(value) {
            prefs.edit().putLong("hold_duration_ms", value).apply()
        }

    var dataHoldBytes: Int
        get() = prefs.getInt("data_hold_bytes", 90)
        set(value) {
            prefs.edit().putInt("data_hold_bytes", value).apply()
        }
}
