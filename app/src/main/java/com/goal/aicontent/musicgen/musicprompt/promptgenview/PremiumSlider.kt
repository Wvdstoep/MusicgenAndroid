package com.goal.aicontent.musicgen.musicprompt.promptgenview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.goal.aicontent.R

@Composable
fun PremiumSlider(selectedDuration: Float, onDurationChange: (Float) -> Unit, onCrownClick: () -> Unit) {
    val isPremium = selectedDuration > 9
    // Use LocalDensity.current for conversions
    val density = LocalDensity.current

    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.CenterStart) {
        Slider(
            value = selectedDuration,
            onValueChange = onDurationChange,
            valueRange = 1f..30f, // Assuming 1 to 30 seconds range
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
        )
        if (isPremium) {
            // Dynamically calculate the offset based on the slider value
            val sliderWidth = 300.dp // Assume this is the slider's width
            val halfIconWidth = 12.dp // Half the width of your icon
            val offset = with(density) {
                ((selectedDuration - 1) / 29 * sliderWidth.toPx()) - halfIconWidth.toPx()
            }

            // Using IconButton for clickable crown icon
            IconButton(
                onClick = { onCrownClick() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = with(density) { offset.toDp() }) // Correct conversion back to dp
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_crown),
                    contentDescription = "Premium",
                    tint = Color.Yellow
                )
            }
        }
    }
}