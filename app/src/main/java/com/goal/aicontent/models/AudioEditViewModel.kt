package com.goal.aicontent.models

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.arthenica.mobileffmpeg.FFmpeg
import com.goal.aicontent.DownloadStatus
import com.goal.aicontent.functions.DownloadableContent
import com.goal.aicontent.functions.DownloadableContentData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class AudioEditViewModel(application: Application) : AndroidViewModel(application) {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _downloadableContents = MutableStateFlow<List<DownloadableContent>>(emptyList())
    val downloadableContents: StateFlow<List<DownloadableContent>> = _downloadableContents.asStateFlow()

    fun Uri.copyToTempFile(context: Context): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(this)
            val tempFile = File.createTempFile("prefix_", null, context.cacheDir).apply {
                outputStream().use { fileOut ->
                    inputStream?.copyTo(fileOut)
                }
            }
            tempFile
        } catch (e: IOException) {
            Log.e("UriExtension", "Error copying to temp file", e)
            null
        }
    }
    fun validateAndNavigate(filePath: String, context: Context, navController: NavHostController) {
        val uri = when {
            filePath.startsWith("content://") || filePath.startsWith("file://") -> Uri.parse(filePath)
            File(filePath).exists() -> Uri.fromFile(File(filePath))
            else -> null
        }

        uri?.let {
            val uriString = Uri.encode(uri.toString())
            navController.navigate("trimViewRoute/$uriString")
        } ?: run {
            Toast.makeText(context, "File path is invalid or item not found.", Toast.LENGTH_SHORT).show()
        }
    }
    fun saveFileToMusicDirectory(context: Context, originalFile: File, originalTitle: String) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$originalTitle.wav")
            put(MediaStore.Audio.Media.TITLE, originalTitle)
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            put(MediaStore.Audio.Media.IS_MUSIC, true)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
        }

        val audioUri: Uri? = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        audioUri?.let { uri ->
            context.contentResolver.openOutputStream(uri).use { outputStream ->
                FileInputStream(originalFile).use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                    Log.d("FileSave", "File saved to Music directory")
                }
            }
        } ?: run {
            Log.e("FileSave", "Failed to save file to Music directory")
        }
    }

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun convertMp3ToWav(context: Context, fileUri: Uri, originalTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tempFile = fileUri.copyToTempFile(context)
            if (tempFile != null) {
                val outputFilePath = "${tempFile.path}.wav"
                // Construct and execute the FFmpeg command for conversion
                val ffmpegCommand = "-i ${tempFile.path} -acodec pcm_s16le -ar 44100 $outputFilePath"
                val execution = FFmpeg.execute(ffmpegCommand)
                if (execution == 0) {
                    // Notify UI thread about successful conversion
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Conversion successful!", Toast.LENGTH_SHORT).show()
                    }
                    // Move the file to a more permanent location (if necessary) and update your state
                    val outputFile = File(outputFilePath)
                    val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                    val newFile = File(musicDir, outputFile.name)
                    // New step: Save the file to the system's Music directory
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Call your method to save the file to the Music directory
                        saveFileToMusicDirectory(context, outputFile, originalTitle)
                    } else {
                        // For Android versions below Q, you might want to use a different approach
                        // or ensure you have the WRITE_EXTERNAL_STORAGE permission to write to shared storage
                        Log.e("MusicPromptViewModel", "Saving to Music directory is not supported for Android versions below Q.")
                    }
                    outputFile.copyTo(target = newFile, overwrite = true)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "File saved to Music directory", Toast.LENGTH_SHORT).show()
                    }
                    // Cleanup the temporary file
                    outputFile.delete()
                } else {
                    // Notify UI thread about conversion failure
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Conversion failed.", Toast.LENGTH_SHORT).show()
                    }
                }
                // Cleanup the temporary input file
                tempFile.delete()
            } else {
                // Notify UI thread about failure to create a temporary file
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to create temp file for conversion.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun DownloadableContent.toDownloadableContentData(): DownloadableContentData? =
        downloadUrl?.let {
            DownloadableContentData(title,
                it, filePath, duration, status.value.name)
        } // Ensure status is converted to String


    private fun persistDownloadableContents() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("downloadableContents", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val contentsData = _downloadableContents.value.map { it.toDownloadableContentData() }
        val json = gson.toJson(contentsData)
        editor.putString("contents", json)
        editor.apply()
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun processFile(uri: Uri) {
        // Generate a title for the file based on the current timestamp
        val title = "Converted_${System.currentTimeMillis()}"
        // Convert URI to path, then proceed with conversion, passing the generated title
        convertMp3ToWav(getApplication<Application>().applicationContext, uri, title)
    }
}
