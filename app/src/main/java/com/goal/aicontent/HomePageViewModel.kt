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
    private var mediaPlayer: MediaPlayer? = null

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
    // Inside HomePageViewModel
    fun searchItems(query: String): List<TaskItem> {
        // Example of a client-side filter. Adjust logic as needed.
        return _items.value.filter {
            it.prompt.contains(query, ignoreCase = true) // Assuming 'prompt' contains searchable text
        }
    }

    private fun fetchItems() {
        viewModelScope.launch {
            try {
                val fetchedItems = apiService.getItems()
                    .filter { it.downloadUrl?.isNotEmpty() == true } // Filter out items without download_url
                    .onEach { item ->
                        Log.d("DownloadUrl", "URL: ${item.downloadUrl}")
                    }
                _items.value = fetchedItems
            } catch (e: Exception) {
                Log.e("HomePageViewModel", "Error fetching items: ${e.message}")
            }
        }
    }

    fun playAudioFromUrl(context: Context, audioUrl: String) {
        // Stop and release any existing MediaPlayer instance
        stopAndReleaseMediaPlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(audioUrl))
                prepareAsync() // Use prepareAsync for streaming
                setOnPreparedListener {
                    start() // Start playback as soon as it's ready
                }
                setOnCompletionListener {
                    // Clean up and release the MediaPlayer once playback completes
                    stopAndReleaseMediaPlayer()
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
    // Function to stop and release the MediaPlayer
    private fun stopAndReleaseMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop() // Stop the player if it's playing
            }
            release() // Release resources
        }
        mediaPlayer = null // Clear the instance
    }
    fun getFavoriteIds(context: Context): Set<String> {
        val sharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("favoriteIds", emptySet()) ?: emptySet()
    }

    fun setFavoriteIds(context: Context, ids: Set<String>) {
        val sharedPreferences = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putStringSet("favoriteIds", ids)
            apply()
        }
    }

    data class InstrumentCategory(val id: Int, val name: String, val imageUrl: String)

    private val _categories = MutableStateFlow<List<InstrumentCategory>>(
        listOf(
            InstrumentCategory(1, "Piano", "https://fr.yamaha.com/fr/files/8_G_GC_series_1080_1080[1]_1080x1080_1e1e21e7ee06fc6fb68f3c2640f49d85.jpg"),
            InstrumentCategory(2, "Acoustic Guitar", "https://d1aeri3ty3izns.cloudfront.net/media/9/92323/1200/preview.jpg"),
            InstrumentCategory(3, "Violin", "https://audiomav.com/wp-content/uploads/2021/02/Best-Violin-Solos-Or-Interludes-In-Rock-Songs-Ever-Recorded-1536x1024.jpg"),
            InstrumentCategory(4, "Organ", "https://hub.yamaha.com/wp-content/uploads/2023/01/Pipe-Organ-resize.jpg"),
            InstrumentCategory(5, "Flute", "https://www.altusflutes.com/files/file_pool/1/0l040356964833715332/p_flute.jpg"),
            InstrumentCategory(6, "Bagpipe", "https://blog.nms.ac.uk/app/uploads/2019/07/bagpipes-k-2003-741-detail-tartain.jpg")
            // Add more as needed
        )
    )
    val categories: StateFlow<List<InstrumentCategory>> = _categories.asStateFlow()

    fun fetchItemsForCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                val fetchedItems = apiService.searchItemsByCategory(categoryName)
                _items.value = fetchedItems.filter { it.downloadUrl?.isNotEmpty() == true }
            } catch (e: Exception) {
                Log.e("HomePageViewModel", "Error fetching items for category $categoryName: ${e.message}")
            }
        }
    }
    fun toggleFavorite(context: Context, itemId: String) {
        val updatedFavorites = getFavoriteIds(context).toMutableSet()
        val isNowFavorite = if (updatedFavorites.contains(itemId)) {
            updatedFavorites.remove(itemId)
            false
        } else {
            updatedFavorites.add(itemId)
            true
        }
        setFavoriteIds(context, updatedFavorites)

        // Update the items list to reflect the change
        _items.value = _items.value.map { item ->
            if (item.id == itemId) item.copy(isFavorite = isNowFavorite) else item
        }
    }
    override fun onCleared() {
        super.onCleared()
        stopAndReleaseMediaPlayer()
    }
}
