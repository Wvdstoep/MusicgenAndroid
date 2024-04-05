package com.goal.aicontent.musicgen

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MusicPromptViewModel (application: Application) : AndroidViewModel(application){
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _audioUris = MutableStateFlow<List<Uri>>(emptyList())
    private val _selectedModel = MutableStateFlow("facebook/musicgen-small")

    private var _duration = MutableStateFlow(30f) // Default duration
    data class DownloadableContent(
        val title: String,
        val downloadUrl: String,
        var filePath: String? = null,
        var duration: Long = 0L,
        var status: MutableState<DownloadStatus> = mutableStateOf(DownloadStatus.NOT_DOWNLOADED)
    )

    enum class DownloadStatus {
        NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED
    }
    companion object {
        const val DOWNLOAD_STATUS_PREF = "DownloadStatus"
        fun statusKey(title: String) = "${title}_status"
        fun pathKey(title: String) = "${title}_path"
    }
    fun getFilePathFromDownloadId(context: Context?, downloadId: Long): String? {
        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager?.query(query)
        if (cursor?.moveToFirst() == true) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            return cursor.getString(columnIndex)
        }
        cursor?.close()
        return null
    }

    private val _downloadableContents = MutableStateFlow<List<DownloadableContent>>(emptyList())
    val downloadableContents: StateFlow<List<DownloadableContent>> = _downloadableContents.asStateFlow()
    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    val selectedItems: StateFlow<Set<String>> = _selectedItems.asStateFlow()
    private val _selectedItemsOrder = MutableStateFlow<List<String>>(emptyList())
    val selectedItemsOrder: StateFlow<List<String>> = _selectedItemsOrder.asStateFlow()

    fun toggleItemSelection(itemId: String) {
        _selectedItems.value = _selectedItems.value.toMutableSet().apply {
            if (contains(itemId)) {
                remove(itemId)
                _selectedItemsOrder.value = _selectedItemsOrder.value.filterNot { it == itemId }
            } else {
                add(itemId)
                _selectedItemsOrder.value = _selectedItemsOrder.value + itemId
            }
        }
    }

    fun clearSelection() {
        _selectedItems.value = emptySet()
    }
    // Adjust the function to receive duration
    fun addDownloadableContent(title: String, downloadUrl: String, duration: Long) {
        val newList = _downloadableContents.value.toMutableList().apply {
            add(DownloadableContent(title, downloadUrl, duration = duration))
        }
        _downloadableContents.value = newList
    }
    fun downloadFile(context: Context, downloadUrl: String, fileName: String, content: DownloadableContent) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("Downloading music")
            .setDescription("Downloading $fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        content.status.value = DownloadStatus.DOWNLOADING

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val filePath = getFilePathFromDownloadId(context, downloadId)
                    content.filePath = filePath
                    content.status.value = DownloadStatus.DOWNLOADED
                    saveDownloadStatusAndPath(context, content.title, filePath, content.duration)
                    context?.unregisterReceiver(this)
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED)
    }
    fun saveDownloadStatusAndPath(context: Context?, title: String, filePath: String?, duration: Long) {
        context?.let {
            val sharedPreferences = it.getSharedPreferences(DOWNLOAD_STATUS_PREF, Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putString(statusKey(title), "DOWNLOADED")
                putString(pathKey(title), filePath)
                putLong("${title}_duration", duration) // Save duration here
                apply()
            }
        }
    }
    fun loadPersistedDownloads(context: Context) {
        val sharedPreferences = context.getSharedPreferences(DOWNLOAD_STATUS_PREF, Context.MODE_PRIVATE)
        val allDownloads = sharedPreferences.all
        for ((key, value) in allDownloads) {
            if (key.endsWith("_path")) {
                val title = key.removeSuffix("_path")
                val statusString = sharedPreferences.getString(statusKey(title), null) ?: continue
                val path = value as? String ?: continue
                val duration = sharedPreferences.getLong("${title}_duration", 0L) // Retrieve duration here

                val downloadStatus = when (statusString) {
                    "DOWNLOADED" -> DownloadStatus.DOWNLOADED
                    else -> DownloadStatus.NOT_DOWNLOADED
                }
                val downloadableContent = DownloadableContent(
                    title = title,
                    downloadUrl = "",
                    filePath = path,
                    duration = duration,
                    status = mutableStateOf(downloadStatus)
                )
                addDownloadableContentFromSharedPreferences(downloadableContent)
            }
        }
    }

    private fun addDownloadableContentFromSharedPreferences(content: DownloadableContent) {
        val newList = _downloadableContents.value.toMutableList().apply {
            add(content)
        }
        _downloadableContents.value = newList
    }


    fun setSelectedModel(model: String) {
        val modelName = if (!model.startsWith("facebook/")) "facebook/$model" else model
        _selectedModel.value = modelName
    }

    fun setDuration(duration: Float) {
        _duration.value = duration
    }
    fun generateMusic(prompt: String, context: Context) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            sendMusicPrompt(prompt, context, _selectedModel.value, _duration.value, prompt)
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }
    private suspend fun sendMusicPrompt(prompt: String, context: Context, model: String, duration: Float, originalPrompt: String) {
        _isLoading.value = true
        val service = ApiClient.retrofit.create(MusicGenerationService::class.java)
        try {
            val response = withContext(Dispatchers.IO) {
                service.generateMusic(MusicPromptRequest(prompt, model, duration))
            }
            if (response.isSuccessful) {
                response.body()?.let { downloadResponse ->
                    val downloadUrl = downloadResponse.download_url // Assume you have parsed this correctly
                    val title = "Music for $originalPrompt"
                    val durationMillis = (duration * 1000).toLong() // Convert seconds to milliseconds and then to Long
                    addDownloadableContent(title, downloadUrl, durationMillis)
                }
            }
        } catch (e: Exception) {
            Log.e("MusicGen", "API call failed", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun playAudioFromUri(context: Context, audioUri: Uri) {
        val exoPlayer = ExoPlayerSingleton.getExoPlayer(context)

        val mediaUri = audioUri // Assuming the audioUri is already a valid Uri

        // Start playback
        ExoPlayerSingleton.preview(context, mediaUri, 0, Long.MAX_VALUE)

        // Make sure to release the player when it's no longer needed
        (context as? LifecycleOwner)?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    ExoPlayerSingleton.releaseExoPlayer()
                }
            }
        })
    }}
