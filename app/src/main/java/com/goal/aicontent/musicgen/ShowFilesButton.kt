package com.goal.aicontent.musicgen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ShowFilesButton(showFiles: Boolean, onToggle: (Boolean) -> Unit) {
    Button(
        onClick = { onToggle(!showFiles) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Show Saved Files")
    }
}