package com.goal.aicontent.musicgen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DurationSelectionRow(durationOptions: List<Int>, onDurationSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        durationOptions.forEach { duration ->
            OutlinedButton(
                onClick = { onDurationSelected(duration) },
                modifier = Modifier.weight(1f)
            ) {
                Text("$duration sec", maxLines = 1)
            }
        }
    }
}
