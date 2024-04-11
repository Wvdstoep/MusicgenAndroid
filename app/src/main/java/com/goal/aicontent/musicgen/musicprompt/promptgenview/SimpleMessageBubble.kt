package com.goal.aicontent.musicgen.musicprompt.promptgenview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.IconButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleMessageBubble(message: String, isUserMessage: Boolean) {
    var showActionsMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Dummy action to simulate copying text to clipboard
    val copyTextToClipboard = {
        // Implement actual copy logic here
        println("Text copied to clipboard")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUserMessage) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isUserMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .combinedClickable(
                    onClick = { /* Handle simple click */ },
                    onLongClick = { showActionsMenu = true } // Trigger actions menu on long press
                )
        ) {
            Box(modifier = Modifier.padding(all = 8.dp)) {
                SelectionContainer {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Display an actions menu or an icon button when long pressed
                if (showActionsMenu) {
                    // Replace this IconButton with a DropdownMenu for multiple actions
                    IconButton(onClick = { copyTextToClipboard() }) {
                        Icon(Icons.Filled.MoreVert, "More actions")
                    }
                }
            }
        }
    }
}
