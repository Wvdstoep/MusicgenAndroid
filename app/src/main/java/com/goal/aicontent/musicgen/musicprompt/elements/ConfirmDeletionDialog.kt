package com.goal.aicontent.musicgen.musicprompt.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun ConfirmDeletionDialog(
    title: String,
    showDeleteFromFileSystemCheckbox: Boolean,
    deleteFromFileSystem: Boolean,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onCheckboxChange: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = {
            Column {
                Text("Are you sure you want to remove the file from the list?")
                if (showDeleteFromFileSystemCheckbox) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = deleteFromFileSystem,
                            onCheckedChange = onCheckboxChange
                        )
                        Text(text = "Also delete file from device")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(deleteFromFileSystem) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}