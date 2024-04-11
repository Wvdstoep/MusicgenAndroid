package com.goal.aicontent.models

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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.room.Room
import com.goal.aicontent.DownloadStatus
import com.goal.aicontent.clients.ApiClient
import com.goal.aicontent.database.AppDatabase
import com.goal.aicontent.database.DownloadableContentEntity
import com.goal.aicontent.functions.DownloadableContent
import com.goal.aicontent.functions.GenreMappings
import com.goal.aicontent.functions.MusicGenerationService
import com.goal.aicontent.functions.MusicPromptRequest
import com.goal.aicontent.functions.TaskStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@UnstableApi
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MusicPromptViewModel (application: Application) : AndroidViewModel(application){

    companion object {
        fun fromEntity(entity: DownloadableContentEntity): DownloadableContent {
            return DownloadableContent(
                taskId = entity.taskId,
                title = entity.title,
                filePath = entity.filePath,
                downloadUrl = entity.downloadUrl,
                duration = entity.duration,
                status = mutableStateOf(DownloadStatus.valueOf(entity.status))
            )
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _downloadableContents = MutableStateFlow<List<DownloadableContent>>(emptyList())
    val downloadableContents: StateFlow<List<DownloadableContent>> = _downloadableContents.asStateFlow()
    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedItemsOrder = MutableStateFlow<List<String>>(emptyList())
    val selectedItemsOrder: StateFlow<List<String>> = _selectedItemsOrder.asStateFlow()
    private val _selectedModel = MutableStateFlow("facebook/musicgen-small")
    private var _duration = MutableStateFlow(30f) // Default duration
    val genres = GenreMappings.genres
    private val musicGenerationService: MusicGenerationService =
        ApiClient.retrofit.create(MusicGenerationService::class.java)

    private val downloadIdToTaskIdMap = HashMap<Long, String>()
    init {
        checkIncompleteTasks()
    }
    private val db = Room.databaseBuilder(
        getApplication<Application>().applicationContext,
        AppDatabase::class.java, "DownloadableContentDao"
    )
        .fallbackToDestructiveMigration()
        .build()

    fun loadDownloadableContents() {
        viewModelScope.launch(Dispatchers.IO) {
            val contentEntities = db.downloadableContentDao().getAll()
            val downloadableContents = contentEntities.map { entity ->
                DownloadableContent(
                    taskId = entity.taskId, // Include taskId here
                    title = entity.title,
                    filePath = entity.filePath,
                    downloadUrl = entity.downloadUrl,
                    duration = entity.duration,
                    status = mutableStateOf(DownloadStatus.valueOf(entity.status))
                )
            }
            withContext(Dispatchers.Main) {
                _downloadableContents.value = downloadableContents
            }
        }
    }

    private fun DownloadableContent.toEntity(): DownloadableContentEntity {
        return DownloadableContentEntity(
            taskId = this.taskId,
            title = this.title,
            downloadUrl = this.downloadUrl,
            filePath = this.filePath,
            duration = this.duration,
            status = this.status.value.name
        )
    }

    fun getFilePathFromDownloadId(context: Context?, downloadId: Long): String? {
        Log.d("DownloadFile", "Retrieving file path for download ID: $downloadId")
        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager?.query(query)

        if (cursor?.moveToFirst() == true) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val filePath = cursor.getString(columnIndex)
            Log.d("DownloadFile", "File path retrieved: $filePath")
            cursor.close()
            return filePath
        } else {
            Log.e("DownloadFile", "Cursor is null or couldn't move to first. Download ID: $downloadId")
        }
        cursor?.close()
        return null
    }
    fun removeSelectedItems() {
        viewModelScope.launch(Dispatchers.IO) {
            val contentsToRemove = _downloadableContents.value.filter { it.taskId in _selectedItems.value }
            contentsToRemove.forEach { content ->
                db.downloadableContentDao().delete(content.toEntity())
                Log.d("MusicPromptVM", "Removed content from database: ${content.title}")
            }

            // Update in-memory list
            val updatedList = _downloadableContents.value - contentsToRemove.toSet()
            withContext(Dispatchers.Main) {
                _downloadableContents.value = updatedList
                _selectedItems.value = emptySet() // Clear after removal
            }
        }
    }

    private fun updateDownloadableContentStatus(taskId: String, status: DownloadStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            db.downloadableContentDao().updateStatusByTaskId(taskId, status.name)
        }
    }
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
    private fun sanitizeFileName(fileName: String, extension: String = ".wav"): String {
        val regex = Regex("[^a-zA-Z0-9_.-]")
        val sanitized = fileName.replace(regex, "_").take(50)
        return if (!sanitized.endsWith(extension)) "$sanitized$extension" else sanitized
    }

    fun downloadFile(context: Context, downloadUrl: String, originalFileName: String, finalTaskId: String) {
        val sanitizedFileName = sanitizeFileName(originalFileName)
        Log.d("MusicPromptVM", "Starting download for URL: $downloadUrl with fileName: $sanitizedFileName")

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("Downloading music")
            .setDescription("Downloading $sanitizedFileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, sanitizedFileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        downloadIdToTaskIdMap[downloadId] = finalTaskId

        monitorDownload(context, downloadId)
    }

    private fun monitorDownload(context: Context, downloadId: Long) {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val taskId = downloadIdToTaskIdMap.remove(downloadId)
                    if (taskId != null) {
                        val filePath = getFilePathFromDownloadId(context, downloadId)
                        if (filePath != null) {
                            updateDownloadStatus(taskId, DownloadStatus.DOWNLOADED, filePath)
                        } else {
                            Log.e("MusicPromptVM", "Error retrieving file path for download ID: $downloadId")
                        }
                    }
                    context?.unregisterReceiver(this)
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun updateDownloadStatus(taskId: String, newStatus: DownloadStatus, filePath: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MusicPromptVM", "Updating status in DB for taskId: $taskId to $newStatus")

            db.downloadableContentDao().updateDownloadStatusAndPath(taskId, newStatus.name, filePath ?: "")
            Log.d("MusicPromptVM", "Database update successful for taskId: $taskId")

            val updatedList = _downloadableContents.value.map { content ->
                if (content.taskId == taskId) {
                    Log.d("MusicPromptVM", "Found content in list for taskId: $taskId, updating status to $newStatus")
                    content.copy(status = mutableStateOf(newStatus), filePath = filePath)
                } else {
                    content
                }
            }

            withContext(Dispatchers.Main) {
                _downloadableContents.value = updatedList
                Log.d("MusicPromptVM", "In-memory list updated, UI should now reflect changes for taskId: $taskId")
            }
        }
    }

    fun removeItemFromDownloads(title: String, deleteFromFileSystem: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            // Find the content by title and remove it from the database
            val contentToDelete = db.downloadableContentDao().findDownloadableContentByTitle(title)
            contentToDelete?.let { content ->
                db.downloadableContentDao().delete(content)
                Log.d("MusicPromptVM", "Content removed from database: $title")

                // Optionally, delete the file associated with this content from the file system
                if (deleteFromFileSystem && content.filePath != null) {
                    File(content.filePath).delete().also { deletionSuccess ->
                        if (deletionSuccess) {
                            Log.d("MusicPromptVM", "File deleted from file system: ${content.filePath}")
                        } else {
                            Log.e("MusicPromptVM", "Failed to delete file from file system: ${content.filePath}")
                        }
                    }
                }
            } ?: Log.e("MusicPromptVM", "Content not found in database: $title")

            withContext(Dispatchers.Main) {
                // Update UI or notify user as necessary
                // For example, you could refresh the list of downloadable contents to reflect the removal
                loadDownloadableContents()
            }
        }
    }
    private fun addOrUpdateDownloadableContent(
        finalTaskId: String,
        title: String,
        downloadUrl: String?,
        filePath: String?,
        duration: Long,
        status: DownloadStatus
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MusicPromptVM", "Updating or inserting content for taskId: $finalTaskId")

            // Attempt to find an existing entry using the final taskId
            val existingContent = db.downloadableContentDao().getTaskById(finalTaskId)

            val updatedContent = existingContent?.copy(
                title = title,
                downloadUrl = downloadUrl ?: existingContent.downloadUrl,
                filePath = filePath ?: existingContent.filePath,
                duration = duration,
                status = status.name
            ) ?: DownloadableContentEntity(
                taskId = finalTaskId,
                title = title,
                downloadUrl = downloadUrl ?: "",
                filePath = filePath ?: "",
                duration = duration,
                status = status.name
            )

            // Insert or update the database entry
            db.downloadableContentDao().insert(updatedContent)
            Log.d("MusicPromptVM", "Content updated in database for taskId: $finalTaskId")

            // Update the in-memory list accordingly
            _downloadableContents.value = _downloadableContents.value.filterNot { it.taskId == finalTaskId } + fromEntity(updatedContent)

            withContext(Dispatchers.Main) {
                Log.d("MusicPromptVM", "In-memory list updated for taskId: $finalTaskId")
            }
        }
    }

    private fun checkTaskStatusPeriodically(taskId: String, title: String, duration: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            var taskStatus: TaskStatusResponse?
            try {
                do {
                    val response = musicGenerationService.checkTaskStatus(taskId)
                    taskStatus = response.body()
                    if (taskStatus?.status != "completed") {
                        delay(5000) // Poll every 5 seconds
                    }
                } while (taskStatus?.status == "processing")

                if (taskStatus?.status == "completed" && taskStatus.downloadUrl != null) {
                    addOrUpdateDownloadableContent(
                        finalTaskId = taskId,
                        title = title,
                        downloadUrl = taskStatus.downloadUrl,
                        filePath = null,
                        duration = (duration * 1000).toLong(),
                        status = DownloadStatus.NOT_DOWNLOADED
                    )
                } else {
                    // If status isn't 'completed', handle accordingly
                    updateDownloadableContentStatus(taskId, DownloadStatus.FAILED)
                }
            } catch (e: Exception) {
                Log.e("MusicPromptVM", "Error checking task status: ${e.localizedMessage}", e)
                updateDownloadableContentStatus(taskId, DownloadStatus.FAILED)
            }
        }
    }
    fun setSelectedModel(model: String) {
        val modelName = if (!model.startsWith("facebook/")) "facebook/$model" else model
        _selectedModel.value = modelName
    }

    fun setDuration(duration: Float) {
        _duration.value = duration
    }
    fun generateMusic(prompt: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            sendMusicPrompt(prompt, _selectedModel.value, _duration.value, prompt)
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }

    private suspend fun sendMusicPrompt(prompt: String, model: String, duration: Float, originalPrompt: String) {
        _isLoading.value = true
        try {
            val response = musicGenerationService.generateMusic(MusicPromptRequest(prompt, model, duration))
            if (response.isSuccessful && response.body() != null) {
                val downloadResponse  = response.body()!!
                // Immediately use the final taskId
                addOrUpdateDownloadableContent(
                    finalTaskId = downloadResponse.task_id, // Use task_id from DownloadResponse
                    title = "Music for $originalPrompt",
                    downloadUrl = null, // Initially null, will be updated upon completion
                    filePath = null,
                    duration = (duration * 1000).toLong(),
                    status = DownloadStatus.PROCESSING
                )

                // Monitor the task status
                checkTaskStatusPeriodically(downloadResponse.task_id, "Music for $originalPrompt", duration)
            } else {
                Log.e("MusicPromptVM", "Failed to initiate music generation.")
            }
        } catch (e: Exception) {
            Log.e("MusicPromptVM", "Exception in sending music prompt: ${e.localizedMessage}", e)
        } finally {
            _isLoading.value = false
        }
    }

    private fun checkIncompleteTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val incompleteTasks = db.downloadableContentDao().getTasksByStatus("processing")
                incompleteTasks.forEach { task ->
                    checkTaskStatusPeriodically(task.taskId, task.title, _duration.value)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Error checking incomplete tasks: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
