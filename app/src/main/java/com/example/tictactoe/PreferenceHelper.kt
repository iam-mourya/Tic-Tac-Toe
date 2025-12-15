package com.example.tictactoe

import android.content.Context
import androidx.core.content.ContextCompat

enum class ThemeOption {
    LIGHT, DARK, NEON
}

data class ThemePalette(
    val background: Int,
    val textPrimary: Int,
    val buttonBackground: Int,
    val buttonText: Int,
    val accent: Int,
    val winStroke: Int
)

class PreferenceHelper(context: Context) {
    private val prefs = context.getSharedPreferences("ttt_prefs", Context.MODE_PRIVATE)

    fun setTheme(option: ThemeOption) {
        prefs.edit().putString("theme", option.name).apply()
    }

    fun getTheme(): ThemeOption {
        val stored = prefs.getString("theme", ThemeOption.LIGHT.name)
        return runCatching { ThemeOption.valueOf(stored ?: ThemeOption.LIGHT.name) }.getOrDefault(ThemeOption.LIGHT)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound", enabled).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean("sound", true)

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration", enabled).apply()
    }

    fun isVibrationEnabled(): Boolean = prefs.getBoolean("vibration", true)
}

object ThemeManager {
    fun palette(context: Context, option: ThemeOption): ThemePalette {
        return when (option) {
            ThemeOption.LIGHT -> ThemePalette(
                background = ContextCompat.getColor(context, R.color.light_bg),
                textPrimary = ContextCompat.getColor(context, R.color.black),
                buttonBackground = ContextCompat.getColor(context, R.color.white),
                buttonText = ContextCompat.getColor(context, R.color.black),
                accent = ContextCompat.getColor(context, R.color.light_accent),
                winStroke = ContextCompat.getColor(context, R.color.purple_500)
            )
            ThemeOption.DARK -> ThemePalette(
                background = ContextCompat.getColor(context, R.color.dark_bg),
                textPrimary = ContextCompat.getColor(context, R.color.white),
                buttonBackground = ContextCompat.getColor(context, R.color.dark_bg),
                buttonText = ContextCompat.getColor(context, R.color.white),
                accent = ContextCompat.getColor(context, R.color.dark_accent),
                winStroke = ContextCompat.getColor(context, R.color.teal_200)
            )
            ThemeOption.NEON -> ThemePalette(
                background = ContextCompat.getColor(context, R.color.neon_bg),
                textPrimary = ContextCompat.getColor(context, R.color.neon_text),
                buttonBackground = ContextCompat.getColor(context, R.color.neon_bg),
                buttonText = ContextCompat.getColor(context, R.color.neon_text),
                accent = ContextCompat.getColor(context, R.color.neon_accent),
                winStroke = ContextCompat.getColor(context, R.color.neon_accent)
            )
        }
    }
}

