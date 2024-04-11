package com.goal.aicontent.musicgen.musicprompt.promptgenview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeMessageBubble() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Welcome to the Music Prompt Generator! Type a message to get started.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(all = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}