package com.goal.aicontent.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel


val LightPrimary = Color(0xFF6202EE)  // A vibrant purple
val LightOnPrimary = Color.White
val LightSecondary = Color(0xFF03DAC6)  // A teal accent
val LightOnSecondary = Color.White
val LightBackground = Color(0xFFF6F6F6)  // A very light gray
val LightOnBackground = Color(0xFF232323)  // Almost black
val LightSurface = Color.White
val LightOnSurface = Color(0xFF232323)

// Modern Dark Theme Colors
val DarkPrimary = Color(0xFFD32F2F)  // A deep, vibrant red
val DarkOnPrimary = Color(0xFFFFFFFF)  // White text on primary color
val DarkSecondary = Color(0xFF00BCD4)  // A bright teal for accents
val DarkOnSecondary = Color(0xFF000000)  // Black text on secondary color
val DarkBackground = Color(0xFF2C2C2C)  // Very dark gray, softer than black
val DarkOnBackground = Color(0xDEFFFFFF)  // Slightly transparent white for texts
val DarkSurface = Color(0xFF37474F)  // Dark blue-grey for surfaces like cards
val DarkOnSurface = Color(0xDEFFFFFF)  // Similar to onBackground for consistency


private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    // You can define other colors as needed, like error colors, tertiary, etc.
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    // Additional color overrides as needed
)
@Composable
fun AicontentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeViewModel: ThemeViewModel = viewModel()

    // Dynamic color selection based on the theme
    val colorScheme = if (darkTheme) {
        colorSchemeFromViewModel(themeViewModel, true)
    } else {
        colorSchemeFromViewModel(themeViewModel, false)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    ).also {
        // Update system UI components, such as the status bar color, based on the theme
        UpdateSystemUI(colorScheme.primary, darkTheme)
    }
}

@Composable
private fun colorSchemeFromViewModel(viewModel: ThemeViewModel, darkTheme: Boolean): ColorScheme {
    // Use viewModel to get the colors
    return if (darkTheme) {
        darkColorScheme(
            primary = viewModel.darkPrimaryColor.collectAsState().value,
            onPrimary = viewModel.darkOnPrimaryColor.collectAsState().value,
            secondary = viewModel.darkSecondaryColor.collectAsState().value,
            onSecondary = viewModel.darkOnSecondaryColor.collectAsState().value,
            background = viewModel.darkBackgroundColor.collectAsState().value,
            onBackground = viewModel.darkOnBackgroundColor.collectAsState().value,
            surface = viewModel.darkSurfaceColor.collectAsState().value,
            onSurface = viewModel.darkOnSurfaceColor.collectAsState().value
            // Add more colors as needed
        )
    } else {
        lightColorScheme(
            primary = viewModel.primaryColor.collectAsState().value,
            onPrimary = viewModel.onPrimaryColor.collectAsState().value,
            secondary = viewModel.secondaryColor.collectAsState().value,
            onSecondary = viewModel.onSecondaryColor.collectAsState().value,
            background = viewModel.backgroundColor.collectAsState().value,
            onBackground = viewModel.onBackgroundColor.collectAsState().value,
            surface = viewModel.surfaceColor.collectAsState().value,
            onSurface = viewModel.onSurfaceColor.collectAsState().value
            // Add more colors as needed
        )
    }
}

@Composable
private fun UpdateSystemUI(color: Color, darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = color.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
}
