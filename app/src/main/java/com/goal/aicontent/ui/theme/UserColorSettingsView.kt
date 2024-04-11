package com.goal.aicontent.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import kotlinx.coroutines.flow.StateFlow

@Composable
fun UserColorSettingsView(viewModel: ThemeViewModel) {
    val isEditingDarkTheme by viewModel.isEditingDarkTheme.collectAsState()
    val controller = rememberColorPickerController()


    // Dynamically choose the color list based on the toggle
    val colorList = if (isEditingDarkTheme) {
        listOf(
            "Primary Color" to viewModel.darkPrimaryColor,
            "On Primary Color" to viewModel.darkOnPrimaryColor,
            "Secondary Color" to viewModel.darkSecondaryColor,
            "On Secondary Color" to viewModel.darkOnSecondaryColor,
            "Background Color" to viewModel.darkBackgroundColor,
            "On Background Color" to viewModel.darkOnBackgroundColor,
            "Surface Color" to viewModel.darkSurfaceColor,
            "On Surface Color" to viewModel.darkOnSurfaceColor
            // Include additional dark theme colors as needed
        )
    } else {
        listOf(
            "Primary Color" to viewModel.primaryColor,
            "On Primary Color" to viewModel.onPrimaryColor,
            "Secondary Color" to viewModel.secondaryColor,
            "On Secondary Color" to viewModel.onSecondaryColor,
            "Background Color" to viewModel.backgroundColor,
            "On Background Color" to viewModel.onBackgroundColor,
            "Surface Color" to viewModel.surfaceColor,
            "On Surface Color" to viewModel.onSurfaceColor
            // Include additional light theme colors as needed
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center items horizontally
    ) {
        ThemeModeToggle(viewModel = viewModel)

        colorList.forEach { (label, colorFlow) ->
            ColorPickerSection(label, colorFlow, controller) { newColor ->
                viewModel.updateColorByLabel(label, newColor, isEditingDarkTheme)
            }
        }
        Button(onClick = { viewModel.resetColorsToDefault(isEditingDarkTheme) }) {
            Text("Reset to Default")
        }
    }
}

@Composable
fun ThemeModeToggle(viewModel: ThemeViewModel) {
    val isEditingDarkTheme by viewModel.isEditingDarkTheme.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp) // Add padding around the row for better spacing
    ) {
        Text("Light Theme")

        // Adding a spacer between the text and the switch for better visual separation
        Spacer(modifier = Modifier.width(8.dp))

        // Switch component
        Switch(
            checked = isEditingDarkTheme,
            onCheckedChange = { viewModel.isEditingDarkTheme.value = it }
        )

        // Adding another spacer between the switch and the second text
        Spacer(modifier = Modifier.width(8.dp))

        Text("Dark Theme")
    }
}

@Composable
fun ColorPickerSection(
    title: String,
    colorFlow: StateFlow<Color>,
    controller: ColorPickerController,
    updateColor: (Color) -> Unit
) {
    val color by colorFlow.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(color)
                .border(BorderStroke(2.dp, Color.Black))
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        HsvColorPickerDialog(
            initialColor = color,
            controller = controller,
            onColorSelected = updateColor,
            onDismiss = { showDialog = false } // Here, you pass the action to dismiss the dialog.
        )
    }
}
@Composable
fun HsvColorPickerDialog(
    initialColor: Color,
    controller: ColorPickerController,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit // Accept a lambda to handle dialog dismissal
) {
    Dialog(onDismissRequest = onDismiss) {
        Column {
            Box(
                modifier = Modifier
                    .weight(8f)
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                HsvColorPicker(
                    modifier = Modifier.fillMaxSize(),
                    controller = controller,
                    initialColor = initialColor,
                    onColorChanged = { colorEnvelope ->
                        val selectedColor = Color(android.graphics.Color.parseColor("#${colorEnvelope.hexCode}"))
                        onColorSelected(selectedColor)
                    }
                )
            }
            AlphaSlider(
                modifier = Modifier
                    .testTag("HSV_AlphaSlider")
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(35.dp)
                    .align(Alignment.CenterHorizontally),
                controller = controller,
            )

            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(35.dp)
                    .align(Alignment.CenterHorizontally),
                controller = controller,
            )
        }
    }
}
