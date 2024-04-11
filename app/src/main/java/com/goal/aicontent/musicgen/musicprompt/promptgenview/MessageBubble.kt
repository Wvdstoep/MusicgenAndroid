package com.goal.aicontent.musicgen.musicprompt.promptgenview

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.goal.aicontent.functions.MessageChat
import com.goal.aicontent.functions.MessageType

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MessageBubble(message: MessageChat, onUsePrompt: (String) -> Unit) {
    when (message.type) {
        MessageType.TEXT -> {
            SimpleMessageBubble(message.content, message.isUserMessage)
        }
        MessageType.SONG -> message.songDetails?.let { songDetails ->
            //SongMessageBubble(songDetails = songDetails)
        }
        null -> {
            Log.e("MessageBubble", "Message type is null for message: ${message.content}")
            // Optionally, render some fallback UI here
        }
    }
}