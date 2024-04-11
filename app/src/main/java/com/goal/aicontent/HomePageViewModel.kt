package com.goal.aicontent

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.goal.aicontent.functions.ApiService
import com.goal.aicontent.functions.ExoPlayerSingleton
import com.goal.aicontent.functions.TaskItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomePageViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<TaskItem>>(emptyList())
    val items: StateFlow<List<TaskItem>> = _items.asStateFlow()

    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.14:5001") // Replace with your API's base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    init {
        fetchItems()
    }

    private fun fetchItems() {
        viewModelScope.launch {
            try {
                val fetchedItems = apiService.getItems()
                fetchedItems.forEach { item ->
                    Log.d("DownloadUrl", "URL: ${item.downloadUrl}")
                }
                _items.value = fetchedItems
            } catch (e: Exception) {
                Log.e("HomePageViewModel", "Error fetching items: ${e.message}")
            }
        }
    }
    fun getUniqueModels(tasks: List<TaskItem>): List<String> {
        return tasks.map { it.model_name }.distinct()
    }

    fun playAudioFromUrl(context: Context, audioUrl: String) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync() // Use prepareAsync for streaming
                setOnPreparedListener {
                    start() // Start playback as soon as it's ready
                }
                setOnCompletionListener {
                    // Clean up and release the MediaPlayer once playback completes
                    release()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error setting up audio playback", Toast.LENGTH_SHORT).show()
        }
    }

    fun categorizeTasks(tasks: List<TaskItem>): Map<String, List<TaskItem>> {
        // Categorizing by model name as an example
        val byModel = tasks.groupBy { it.model_name }

        // Further categorization by duration within each model category
        val categorized = byModel.mapValues { (_, items) ->
            items.groupBy {
                when {
                    it.duration <= 10 -> "Short Tracks (<=10s)"
                    it.duration <= 30 -> "Medium Tracks (<=30s)"
                    else -> "Long Tracks (>30s)"
                }
            }
        }

        // Flatten the map for easier use in Composables
        val flattened = categorized.flatMap { entry ->
            entry.value.map { "${entry.key} - ${it.key}" to it.value }
        }.toMap()

        return flattened
    }

}
