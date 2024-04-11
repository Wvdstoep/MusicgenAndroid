package com.goal.aicontent.functions

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MediaEditingManager(private val context: Context) {
    private val _isLoading = MutableStateFlow(false)
    val isLoadingCon = _isLoading.asStateFlow()

    private val tag = "MediaEditingManager"

    suspend fun concatenateSelectedFiles(
        filePaths: List<String>, // Ensure these are WAV file paths
        outputFilePath: String // Ensure this path ends with .wav
    ): Boolean {
        _isLoading.value = true
        val correctedOutputFilePath = if (!outputFilePath.endsWith(".wav")) {
            Log.d(tag, "Correcting output file extension to .wav")
            outputFilePath.replaceAfterLast('.', "wav")
        } else {
            outputFilePath
        }

        val outputFile = File(correctedOutputFilePath)
        if (outputFile.exists()) {
            Log.d(tag, "Output WAV file already exists. Deleting...")
            outputFile.delete()
        }

        val fileListPath = createFileListForConcatenation(filePaths)
        Log.d(tag, "File list for concatenation created at: $fileListPath")

        val command = "-y -f concat -safe 0 -i \"$fileListPath\" -c copy \"$correctedOutputFilePath\""
        Log.d(tag, "Executing FFmpeg command: $command")

        val executionResult = FFmpeg.execute(command)

        // Handle execution result immediately after FFmpeg command execution
        val isSuccess = executionResult == 0
        if (isSuccess) {
            // Concatenation successful
            Log.d(tag, "WAV files concatenated successfully: $correctedOutputFilePath")
            // Optionally, move file to music folder or handle success
            moveToMusicFolder(context, correctedOutputFilePath, "My Concatenated Playlist")
        } else {
            // Concatenation failed
            Log.e(tag, "Failed to concatenate WAV files. FFmpeg execution result: $executionResult")
        }

        _isLoading.value = false
        return isSuccess // Return the success status directly
    }
    private fun handleExecutionResult(executionResult: Int, outputFilePath: String) {
        CoroutineScope(Dispatchers.Main).launch {
            if (executionResult == 0) {
                _isLoading.value = false
                Log.d(tag, "WAV files concatenated successfully: $outputFilePath")
                moveToMusicFolder(context, outputFilePath, "My Concatenated Playlist")
            } else {
                _isLoading.value = false
                Log.e(tag, "Failed to concatenate WAV files. FFmpeg execution result: $executionResult")
            }
        }
    }

    fun moveToMusicFolder(context: Context, sourceFilePath: String, title: String = "Concatenated Playlist") {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$title.mp3")
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            }
        }

        try {
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create new MediaStore record.")

            resolver.openOutputStream(uri).use { outputStream ->
                File(sourceFilePath).inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream ?: throw IOException("Failed to get output stream."))
                }
            }

            // Optionally, delete the source file if no longer needed
            File(sourceFilePath).delete()

            Log.d("MoveFile", "File moved to Music folder successfully: $uri")
        } catch (e: IOException) {
            Log.e("MoveFile", "Failed to move file: ${e.message}", e)
        }
    }
    private fun createFileListForConcatenation(filePaths: List<String>): String {
        val fileList = File(context.cacheDir, "concat_list_${System.currentTimeMillis()}.txt")
        fileList.bufferedWriter().use { writer ->
            filePaths.forEach { filePath ->
                // Directly use file paths instead of converting URIs
                writer.write("file '$filePath'\n")
            }
        }
        return fileList.absolutePath
    }
    fun convertContentUriToFile(context: Context, contentUri: String): String {
        val uri = Uri.parse(contentUri)
        val tempFile = File(context.filesDir, "temp_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { fileOut ->
                inputStream.copyTo(fileOut)
            }
        }
        return tempFile.absolutePath
    }
}
