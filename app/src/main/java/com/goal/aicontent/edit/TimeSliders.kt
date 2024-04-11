package com.goal.aicontent.edit

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.goal.aicontent.models.TrimViewModel

@OptIn(UnstableApi::class) @Composable
fun TimeSliders(
    startTime: Long,
    endTime: Long,
    mediaDuration: Long,
    volume: Float, // Add this line
    onVolumeChange: (Float) -> Unit, // Add this line
    trimViewModel: TrimViewModel,
    onTimeChange: (Long, Long) -> Unit
) {

    Column {
        Text("Start Time: ${trimViewModel.formatTime(startTime)}")
        Slider(
            value = startTime.toFloat(),
            onValueChange = { newValue ->
                onTimeChange(newValue.toLong().coerceAtMost(endTime - 1000), endTime)
            },
            valueRange = 0f..mediaDuration.toFloat(),
            steps = 0
        )

        Spacer(Modifier.height(16.dp))

        Text("End Time: ${trimViewModel.formatTime(endTime)}")
        Slider(
            value = endTime.toFloat(),
            onValueChange = { newValue ->
                onTimeChange(startTime, newValue.toLong().coerceAtLeast(startTime + 1000))
            },
            valueRange = 0f..mediaDuration.toFloat(),
            steps = 0
        )
        Text("Volume: ${(volume * 100).toInt()}%")
        Slider(
            value = volume,
            onValueChange = onVolumeChange, // Use the passed lambda for updates
            valueRange = 0f..2f, // Adjust the range as needed.
            steps = 100 // Adjust for finer control over the volume
        )
    }
}