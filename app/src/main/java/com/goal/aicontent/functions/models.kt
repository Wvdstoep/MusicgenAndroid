package com.goal.aicontent.functions

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.goal.aicontent.DownloadStatus
import com.goal.aicontent.models.MusicPromptViewModel
import com.google.gson.annotations.SerializedName

data class DownloadableContent(
    val taskId: String, // Ensure this matches with your backend's task ID field
    var title: String,
    @SerializedName("download_url")
    var downloadUrl: String?,
    var filePath: String? = null,
    var duration: Long = 0L,
    var status: MutableState<DownloadStatus> = mutableStateOf(
        DownloadStatus.NOT_DOWNLOADED)
)
data class TaskItem(
    val id: String,
    val status: String,
    @SerializedName("download_url") val downloadUrl: String?,
    val error: String?,
    val prompt: String,
    val duration: Double,
    val model_name: String
)

data class MusicPromptRequest(
    val prompt: String,
    val model: String,
    val duration: Float
)
data class DownloadResponse(
    val message: String,
    val task_id: String, // This should match the JSON field name exactly
    val download_url: String?
)

data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val max_tokens: Int = 150 // Default value set to 150, adjust as needed
)


data class Message(
    val role: String,
    val content: String
)
enum class MessageType {
    TEXT, SONG
}
data class MessageChat(
    val id: String,
    val content: String,
    val isUserMessage: Boolean,
    val type: MessageType = MessageType.TEXT,
    val songDetails: DownloadableContent? = null
)

data class ChatResponse(val choices: List<ChatChoice>) {
    data class ChatChoice(val message: Message)
}

data class DownloadableContentData(
    val title: String,
    val downloadUrl: String,
    val filePath: String?,
    val duration: Long,
    val status: String
)

data class TrimmedFileData(
    val title: String,
    val filePath: String,
    val duration: Long
)

data class TaskStatusResponse(
    val status: String,
    @SerializedName("task_id")
    val taskId: String?, // Add this if your JSON response contains a taskId
    @SerializedName("download_url")
    val downloadUrl: String?
)
