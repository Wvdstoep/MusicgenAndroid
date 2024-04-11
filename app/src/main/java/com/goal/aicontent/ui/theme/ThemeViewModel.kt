package com.goal.aicontent.ui.theme

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
    var isDarkTheme = mutableStateOf(false)
    var isEditingDarkTheme = MutableStateFlow(false)

    // Light theme colors
    private val _primaryColor = MutableStateFlow(loadColor("primaryColor", Color.Magenta))
    val primaryColor: StateFlow<Color> = _primaryColor

    private val _onPrimaryColor = MutableStateFlow(loadColor("onPrimaryColor", Color.White))
    val onPrimaryColor: StateFlow<Color> = _onPrimaryColor

    private val _secondaryColor = MutableStateFlow(loadColor("secondaryColor", Color.Magenta))
    val secondaryColor: StateFlow<Color> = _secondaryColor

    private val _onSecondaryColor = MutableStateFlow(loadColor("onSecondaryColor", Color.White))
    val onSecondaryColor: StateFlow<Color> = _onSecondaryColor

    private val _backgroundColor = MutableStateFlow(loadColor("backgroundColor", Color.Magenta))
    val backgroundColor: StateFlow<Color> = _backgroundColor

    private val _onBackgroundColor = MutableStateFlow(loadColor("onBackgroundColor", Color.White))
    val onBackgroundColor: StateFlow<Color> = _onBackgroundColor

    private val _surfaceColor = MutableStateFlow(loadColor("surfaceColor", Color.Magenta))
    val surfaceColor: StateFlow<Color> = _surfaceColor

    private val _onSurfaceColor = MutableStateFlow(loadColor("onSurfaceColor", Color.White))
    val onSurfaceColor: StateFlow<Color> = _onSurfaceColor

    // Dark theme colors
    private val _darkPrimaryColor = MutableStateFlow(loadColor("darkPrimaryColor", Color.Magenta))
    val darkPrimaryColor: StateFlow<Color> = _darkPrimaryColor

    private val _darkOnPrimaryColor = MutableStateFlow(loadColor("darkOnPrimaryColor", Color.White))
    val darkOnPrimaryColor: StateFlow<Color> = _darkOnPrimaryColor

    private val _darkSecondaryColor = MutableStateFlow(loadColor("darkSecondaryColor", Color.Magenta))
    val darkSecondaryColor: StateFlow<Color> = _darkSecondaryColor

    private val _darkOnSecondaryColor = MutableStateFlow(loadColor("darkOnSecondaryColor", Color.White))
    val darkOnSecondaryColor: StateFlow<Color> = _darkOnSecondaryColor

    private val _darkBackgroundColor = MutableStateFlow(loadColor("darkBackgroundColor", Color.Magenta))
    val darkBackgroundColor: StateFlow<Color> = _darkBackgroundColor

    private val _darkOnBackgroundColor = MutableStateFlow(loadColor("darkOnBackgroundColor", Color.White))
    val darkOnBackgroundColor: StateFlow<Color> = _darkOnBackgroundColor

    private val _darkSurfaceColor = MutableStateFlow(loadColor("darkSurfaceColor", Color.Magenta))
    val darkSurfaceColor: StateFlow<Color> = _darkSurfaceColor

    private val _darkOnSurfaceColor = MutableStateFlow(loadColor("darkOnSurfaceColor", Color.White))
    val darkOnSurfaceColor: StateFlow<Color> = _darkOnSurfaceColor

    private fun loadColor(key: String, defaultColor: Color): Color {
        val colorInt = prefs.getInt(key, defaultColor.toArgb())
        return Color(colorInt)
    }
    private val defaultLightColors = mapOf(
        "primaryColor" to LightPrimary.toArgb(),
        "onPrimaryColor" to LightOnPrimary.toArgb(),
        "secondaryColor" to LightSecondary.toArgb(),
        "onSecondaryColor" to LightOnSecondary.toArgb(),
        "backgroundColor" to LightBackground.toArgb(),
        "onBackgroundColor" to LightOnBackground.toArgb(),
        "surfaceColor" to LightSurface.toArgb(),
        "onSurfaceColor" to LightOnSurface.toArgb(),
        // Include additional light theme colors as needed
    )

    private val defaultDarkColors = mapOf(
        "primaryColor" to DarkPrimary.toArgb(),
        "onPrimaryColor" to DarkOnPrimary.toArgb(),
        "secondaryColor" to DarkSecondary.toArgb(),
        "onSecondaryColor" to DarkOnSecondary.toArgb(),
        "backgroundColor" to DarkBackground.toArgb(),
        "onBackgroundColor" to DarkOnBackground.toArgb(),
        "surfaceColor" to DarkSurface.toArgb(),
        "onSurfaceColor" to DarkOnSurface.toArgb(),
        // Include additional dark theme colors as needed
    )
    val currentNightMode = getApplication<Application>().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    init {
        if (!isInitializedWithDefaultColors()) {
            // Check if the system is in dark mode
            val isSystemDarkTheme = isSystemInDarkTheme()
            resetColorsToDefault(isSystemDarkTheme)
            markAsInitializedWithDefaultColors()
        }
    }

    private fun isSystemInDarkTheme(): Boolean {
        val currentNightMode = getApplication<Application>().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
    private fun isInitializedWithDefaultColors(): Boolean {
        return prefs.getBoolean("isInitializedWithDefaultColors", false)
    }

    private fun markAsInitializedWithDefaultColors() {
        prefs.edit().putBoolean("isInitializedWithDefaultColors", true).apply()
    }
    fun resetColorsToDefault(isDarkTheme: Boolean) {
        val defaultColors = if (isDarkTheme) defaultDarkColors else defaultLightColors

        // Resetting and saving default colors
        defaultColors.forEach { (key, value) ->
            val color = Color(value)
            when (key) {
                "primaryColor" -> setPrimaryColor(color)
                "onPrimaryColor" -> setOnPrimaryColor(color)
                "secondaryColor" -> setSecondaryColor(color)
                "onSecondaryColor" -> setOnSecondaryColor(color)
                "backgroundColor" -> setBackgroundColor(color)
                "onBackgroundColor" -> setOnBackgroundColor(color)
                "surfaceColor" -> setSurfaceColor(color)
                "onSurfaceColor" -> setOnSurfaceColor(color)
                // Add more cases as needed
            }
        }
    }

    fun updateColorByLabel(label: String, newColor: Color, isDarkTheme: Boolean) {
        if (isDarkTheme) {
            // Update the dark theme color
            when (label) {
                "Primary Color" -> setDarkPrimaryColor(newColor)
                "On Primary Color" -> setDarkOnPrimaryColor(newColor)
                "Secondary Color" -> setDarkSecondaryColor(newColor)
                "On Secondary Color" -> setDarkOnSecondaryColor(newColor)
                "Background Color" -> setDarkBackgroundColor(newColor)
                "On Background Color" -> setDarkOnBackgroundColor(newColor)
                "Surface Color" -> setDarkSurfaceColor(newColor)
                "On Surface Color" -> setDarkOnSurfaceColor(newColor)
                // Handle other dark theme colors
            }
        } else {
            // Update the light theme color
            when (label) {
                "Primary Color" -> setPrimaryColor(newColor)
                "On Primary Color" -> setOnPrimaryColor(newColor)
                "Secondary Color" -> setSecondaryColor(newColor)
                "On Secondary Color" -> setOnSecondaryColor(newColor)
                "Background Color" -> setBackgroundColor(newColor)
                "On Background Color" -> setOnBackgroundColor(newColor)
                "Surface Color" -> setSurfaceColor(newColor)
                "On Surface Color" -> setOnSurfaceColor(newColor)
                // Handle other light theme colors
            }
        }
    }

    private fun setPrimaryColor(color: Color) {
        val key = if (isEditingDarkTheme.value) "darkPrimaryColor" else "primaryColor"
        saveColorToPrefs(key, color)
        if (isEditingDarkTheme.value) _darkPrimaryColor.value = color else _primaryColor.value = color
    }

    private fun setOnPrimaryColor(color: Color) {
        val key = if (isEditingDarkTheme.value) "darkOnPrimaryColor" else "onPrimaryColor"
        saveColorToPrefs(key, color)
        if (isEditingDarkTheme.value) _darkOnPrimaryColor.value = color else _onPrimaryColor.value = color
    }

    private fun setSecondaryColor(color: Color) {
        val key = if (isEditingDarkTheme.value) "darkSecondaryColor" else "secondaryColor"
        saveColorToPrefs(key, color)
        if (isEditingDarkTheme.value) _darkSecondaryColor.value = color else _secondaryColor.value = color
    }

    private fun setOnSecondaryColor(color: Color) {
        val key = if (isEditingDarkTheme.value) "darkOnSecondaryColor" else "onSecondaryColor"
        saveColorToPrefs(key, color)
        if (isEditingDarkTheme.value) _darkOnSecondaryColor.value = color else _onSecondaryColor.value = color
    }

    private fun setBackgroundColor(color: Color) {
        val key = if (isEditingDarkTheme.value) "darkBackgroundColor" else "backgroundColor"
        saveColorToPrefs(key, color)
        if (isEditingDarkTheme.value) _darkBackgroundColor.value = color else _backgroundColor.value = color
    }

    private fun setOnBackgroundColor(color: Color) {
        val key = if (isEditingDarkTheme.value) "darkOnBackgroundColor" else "onBackgroundColor"
        saveColorToPrefs(key, color)
        if (isEditingDarkTheme.value) _darkOnBackgroundColor.value = color else _onBackgroundColor.value = color
    }

    private fun setSurfaceColor(color: Color) {
        val key = if (isEditingDarkTheme.value) "darkSurfaceColor" else "surfaceColor"
        saveColorToPrefs(key, color)
        if (isEditingDarkTheme.value) _darkSurfaceColor.value = color else _surfaceColor.value = color
    }

    private fun setOnSurfaceColor(color: Color) {
        val key = if (isEditingDarkTheme.value) "darkOnSurfaceColor" else "onSurfaceColor"
        saveColorToPrefs(key, color)
        if (isEditingDarkTheme.value) _darkOnSurfaceColor.value = color else _onSurfaceColor.value = color
    }

    private fun saveColorToPrefs(key: String, color: Color) {
        viewModelScope.launch {
            prefs.edit().putInt(key, color.toArgb()).apply()
        }
    }
    private fun setDarkPrimaryColor(color: Color) {
        _darkPrimaryColor.value = color
        saveColorToPrefs("darkPrimaryColor", color)
    }

    private fun setDarkOnPrimaryColor(color: Color) {
        _darkOnPrimaryColor.value = color
        saveColorToPrefs("darkOnPrimaryColor", color)
    }

    private fun setDarkSecondaryColor(color: Color) {
        _darkSecondaryColor.value = color
        saveColorToPrefs("darkSecondaryColor", color)
    }

    private fun setDarkOnSecondaryColor(color: Color) {
        _darkOnSecondaryColor.value = color
        saveColorToPrefs("darkOnSecondaryColor", color)
    }

    private fun setDarkBackgroundColor(color: Color) {
        _darkBackgroundColor.value = color
        saveColorToPrefs("darkBackgroundColor", color)
    }

    private fun setDarkOnBackgroundColor(color: Color) {
        _darkOnBackgroundColor.value = color
        saveColorToPrefs("darkOnBackgroundColor", color)
    }

    private fun setDarkSurfaceColor(color: Color) {
        _darkSurfaceColor.value = color
        saveColorToPrefs("darkSurfaceColor", color)
    }

    private fun setDarkOnSurfaceColor(color: Color) {
        _darkOnSurfaceColor.value = color
        saveColorToPrefs("darkOnSurfaceColor", color)
    }
}
