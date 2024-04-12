package com.goal.aicontent.musicgen.musicprompt.homepage

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color

@Composable
fun animatedBorderColor(isFocused: Boolean): State<Color> {
    val transition = updateTransition(targetState = isFocused, label = "Border Color Transition")
    return transition.animateColor(label = "Border Color Animation") { state ->
        when (state) {
            true -> MaterialTheme.colorScheme.primary // Color when focused
            false -> MaterialTheme.colorScheme.onSurfaceVariant // Default color
        }
    }
}

