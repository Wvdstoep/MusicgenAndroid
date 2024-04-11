package com.goal.aicontent.models

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.goal.aicontent.clients.ApiClientAi
import com.goal.aicontent.functions.ChatService
import com.goal.aicontent.functions.ChatRequest
import com.goal.aicontent.functions.DownloadableContent
import com.goal.aicontent.functions.Message
import com.goal.aicontent.functions.MessageChat
import com.goal.aicontent.functions.MessageType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import java.io.File
import kotlin.math.abs

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val prefs = application.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _favorites = MutableStateFlow<Map<String, String>>(emptyMap())
    val favorites: StateFlow<Map<String, String>> = _favorites

    private val _responseContent = MutableStateFlow("")
    val responseContent: StateFlow<String> = _responseContent

    private val _chatHistory = MutableStateFlow<List<MessageChat>>(emptyList())
    val chatHistory: StateFlow<List<MessageChat>> = _chatHistory

    private val chatService = ApiClientAi.retrofit.create(ChatService::class.java)

    fun sendMessage(messageText: String) {
        val messages = listOf(baseMessage, Message(content = messageText, role = "user"))
        val chatRequest = ChatRequest(model = "gpt-3.5-turbo", messages = messages, max_tokens = 1024)
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = chatService.createChatCompletion(chatRequest).awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let { chatResponse ->
                        chatResponse.choices.forEach { choice ->
                            // Assuming each choice contains one message and you want to display it
                            val msg = choice.message
                            val newMessageChat = MessageChat(
                                id = System.currentTimeMillis().toString(),
                                content = msg.content,
                                isUserMessage = msg.role == "user"
                            )
                            viewModelScope.launch(Dispatchers.Main) {
                                _chatHistory.value = _chatHistory.value + newMessageChat
                                saveChatHistory()  // Save after updating chat history
                            }
                        }
                    }
                } else {
                    updateChatHistoryWithSystemMessage("API call failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                updateChatHistoryWithSystemMessage("API call exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }

        }
    }
    fun calculateAudioDuration(context: Context, audioUri: Uri): Int {
        var mediaPlayer: MediaPlayer? = null
        return try {
            mediaPlayer = MediaPlayer.create(context, audioUri)
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
            0
        } finally {
            mediaPlayer?.release() // Release the MediaPlayer resources
        }
    }
    fun generateWaveformData(context: Context, audioUri: Uri): List<Float> {
        val inputStream = context.contentResolver.openInputStream(audioUri) ?: return emptyList()
        val byteBuffer = inputStream.readBytes()
        val sampleRate = 44100 // Assuming a standard sample rate
        val bytesPerSample = 2 // Assuming 16-bit samples (2 bytes per sample)
        val samples = byteBuffer.size / bytesPerSample
        val reductionFactor = samples / sampleRate // Reduce to 1 sample per second for visualization

        val amplitudes = mutableListOf<Float>()
        for (i in byteBuffer.indices step reductionFactor * bytesPerSample) {
            val sample = (byteBuffer[i + 1].toInt() shl 8) or (byteBuffer[i].toInt() and 0xFF)
            amplitudes.add(abs(sample / 32768f))
        }
        return amplitudes
    }
    private fun updateChatHistoryWithSystemMessage(message: String) {
        val systemMessage = MessageChat(
            id = System.currentTimeMillis().toString(),
            content = message,
            isUserMessage = false
        )
        viewModelScope.launch(Dispatchers.Main) {
            _chatHistory.value = _chatHistory.value + systemMessage
        }
    }    // Define your detailed system message here
    private val baseMessage = Message(
        role = "system",
        content = """
    Generate a structured instrumental guide for crafting the intro of a song based on the following inputs:
    - Preferred instruments: [List preferred instruments here]
    - Genre: [Specify genre]
    - Mood: [Specify mood]
    - Themes: [Specify themes]
    
    The guide should be structured as follows, with key advice emphasized using double asterisks (**) for markdown formatting:
    
    - Intro Overview: [General overview of the intro's tone and thematic alignment with the chosen instrument(s)]
    
    - Slow Build-up (0:00 - 0:30): **[Key advice on starting with foundational motifs or rhythms]**
    
    - Transition to Intensity (0:30 - 0:45): **[Key advice on transitioning to a more intense section]**
    
    - Intense Climax (0:45 - 1:00): **[Key advice on maximizing the emotional and energetic impact of the intro]**
    
    Please fill in the sections with appropriate content that follows this structure, ensuring to emphasize key advice with markdown formatting as demonstrated.
""".trimIndent()
    )
    private fun saveChatHistory() {
        val chatHistoryJson = gson.toJson(_chatHistory.value)
        prefs.edit().putString("chatHistory", chatHistoryJson).apply()
    }

    private fun loadChatHistory() {
        val chatHistoryJson = prefs.getString("chatHistory", null)
        if (chatHistoryJson != null) {
            val type = object : TypeToken<List<MessageChat>>() {}.type
            val savedChatHistory: List<MessageChat> = gson.fromJson(chatHistoryJson, type)
            _chatHistory.value = savedChatHistory
        }
    }
    init {
        loadChatHistory()
        val favoritesJson = prefs.getString("favorites", "{}")
        val type = object : TypeToken<Map<String, String>>() {}.type
        _favorites.value = gson.fromJson(favoritesJson, type) ?: emptyMap()
    }

}
