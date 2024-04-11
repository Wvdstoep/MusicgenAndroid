package com.goal.aicontent.musicgen.musicprompt.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun DurationSelectionButton(selectedDuration: Int, onSelect: (Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    // Button that shows current selection and opens the dialog on click
    OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Duration: $selectedDuration seconds", style = MaterialTheme.typography.bodyLarge)
    }

    // Dialog for selecting duration
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Duration") },
            text = {
                // Replace with your list of durations
                val durations = listOf(5, 10, 15, 20, 25, 30)
                Column {
                    durations.forEach { duration ->
                        TextButton(onClick = {
                            onSelect(duration)
                            showDialog = false
                        }) {
                            Text("$duration seconds")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}