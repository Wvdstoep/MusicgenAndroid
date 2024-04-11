package com.goal.aicontent.models

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import com.arthenica.mobileffmpeg.FFmpeg
import com.goal.aicontent.functions.DownloadableContent
import com.goal.aicontent.functions.ExoPlayerSingleton
import com.goal.aicontent.functions.MediaEditingManager
import com.goal.aicontent.functions.TrimmedFileData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.abs

@UnstableApi
class TrimViewModel(application: Application) : AndroidViewModel(application), Player.Listener {
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerSingleton.getExoPlayer(getApplication()).apply {
            addListener(this@TrimViewModel)
        }
    }
    private val _mediaUri = MutableStateFlow<Uri?>(null)
    val mediaUri: StateFlow<Uri?> = _mediaUri
    private val _mediaDuration = MutableStateFlow(0L)
    val mediaDuration: StateFlow<Long> = _mediaDuration
    private val _initialStart = MutableStateFlow(0L)
    val initialStart: StateFlow<Long> = _initialStart
    private val _initialEnd = MutableStateFlow(0L)
    val initialEnd: StateFlow<Long> = _initialEnd
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _waveform = MutableStateFlow<List<Float>>(listOf())
    val waveform: StateFlow<List<Float>> = _waveform

    private fun processAudioFile(
        context: Context,
        audioUri: Uri,
        onWaveformDataReady: (List<Float>) -> Unit
    ) {
        val tempFile = uriToTempFile(context, audioUri)
        val tempPcmFile = File(context.cacheDir, "temp_audio_file.pcm")
        extractRawAudioData(tempFile.path, tempPcmFile.path)
        val waveform = generateWaveformData(tempPcmFile.path)
        onWaveformDataReady(waveform)
    }

    fun applyVolumeAdjustment(context: Context, inputUri: Uri, startTimeMs: Long, endTimeMs: Long, volume: Float, filename: String) {
        Log.d("VolumeAdjustment", "Starting volume adjustment")
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            val inputFile = getTempFileForTrimming(context, inputUri)
            val outputFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "$filename.mp3")

            if (outputFile.exists()) outputFile.delete()

            val startTimeSec = startTimeMs / 1000.0
            val endTimeSec = endTimeMs / 1000.0

            // Construct the FFmpeg command for volume adjustment
            val adjustVolumeCommand = listOf(
                "-y",
                "-i", inputFile.absolutePath,
                "-filter_complex",
                "[0:a]atrim=0:$startTimeSec,asetpts=PTS-STARTPTS[before];" +
                        "[0:a]atrim=$startTimeSec:$endTimeSec,volume=$volume,asetpts=PTS-STARTPTS[during];" +
                        "[0:a]atrim=$endTimeSec,asetpts=PTS-STARTPTS[after];" +
                        "[before][during][after]concat=n=3:v=0:a=1[out]",
                "-map", "[out]",
                outputFile.absolutePath
            ).toTypedArray()

            Log.d("VolumeAdjustment", "Executing FFmpeg command: ${adjustVolumeCommand.joinToString(" ")}")

            // Execute the FFmpeg command
            val result = FFmpeg.execute(adjustVolumeCommand)
            if (result == 0) {
                Log.d("VolumeAdjustment", "Volume adjustment and concatenation successful: ${outputFile.absolutePath}")
                saveFileToMediaStore(context, outputFile, filename)
            } else {
                Log.e("VolumeAdjustment", "Volume adjustment failed with FFmpeg exit code: $result")
            }

            _isLoading.postValue(false)
        }
    }

    private fun saveFileToMediaStore(context: Context, inputFile: File, displayName: String) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let { contentUri ->
            contentResolver.openOutputStream(contentUri).use { outputStream ->
                FileInputStream(inputFile).use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                    Log.d("VolumeAdjustment", "File successfully copied to MediaStore: $displayName")
                }
            }
        } ?: run {
            Log.e("VolumeAdjustment", "Failed to create new MediaStore record for: $displayName")
        }
    }

    private fun uriToTempFile(context: Context, uri: Uri): File {
        val tempFile = File.createTempFile("audio", ".mp3", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }


    private fun extractRawAudioData(inputAudioFilePath: String, outputRawFilePath: String) {
        val command = "-y -i '$inputAudioFilePath' -f s16le -ac 1 -ar 44100 '$outputRawFilePath'"
        FFmpeg.execute(command)
    }

    private fun generateWaveformData(rawAudioFilePath: String): List<Float> {
        val file = File(rawAudioFilePath)
        if (!file.exists()) return emptyList()
        val byteBuffer = file.readBytes()
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

    fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
    }

    init {
        startTrackingPlaybackPosition()
    }
    init {
        exoPlayer.addListener(this)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }
    // Updating the playback position periodically
    private fun startTrackingPlaybackPosition() {
        viewModelScope.launch {
            while (true) {
                _playbackPosition.value = exoPlayer.currentPosition
                delay(1000) // For example, updating every second
            }
        }
    }
    fun updateUserPlaybackPosition(newPosition: Long) {
        // Update the playback position directly
        _playbackPosition.value = newPosition
        // Optionally, seek the player to this new position if needed
        exoPlayer.seekTo(newPosition)
    }
    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedItemsOrder = MutableStateFlow<List<String>>(emptyList())
    val selectedItemsOrder: StateFlow<List<String>> = _selectedItemsOrder.asStateFlow()
    private val _downloadableContents = MutableStateFlow<List<DownloadableContent>>(emptyList())
    val downloadableContents: StateFlow<List<DownloadableContent>> = _downloadableContents.asStateFlow()
    fun editSelectedItem(context: Context, navController: NavHostController) {
        val selectedItemTitle = selectedItemsOrder.value.firstOrNull() // Assuming only one item can be edited at a time
        selectedItemTitle?.let { title ->
            downloadableContents.value.find { it.title == title }?.filePath?.let { filePath ->
                val uri = when {
                    filePath.startsWith("content://") || filePath.startsWith("file://") -> Uri.parse(filePath)
                    else -> Uri.fromFile(File(filePath))
                }
                val uriString = Uri.encode(uri.toString())
                navController.navigate("trimViewRoute/$uriString")
            } ?: run {
                Toast.makeText(context, "File path is invalid or item not found.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun shareSong(fileUri: Uri, songTitle: String) {
        val context = getApplication<Application>().applicationContext

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.applicationContext.packageName}.provider",
                File(fileUri.path ?: return)
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Song")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val _currentlyPlaying = MutableStateFlow<String?>(null)
    val currentlyPlaying: StateFlow<String?> = _currentlyPlaying.asStateFlow()
    fun playAudioFromUri(context: Context, audioUri: Uri, title: String) {
        // If the same audio is already playing, pause it.
        if (_currentlyPlaying.value == title) {
            ExoPlayerSingleton.getExoPlayer(context).pause()
            _isPlaying.value = false
            _currentlyPlaying.value = null
        } else {
            // Else, play the new audio.
            val file = File(audioUri.path ?: "")
            if (!file.exists()) {
                Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show()
                return
            }
            val mediaUri = Uri.fromFile(file)
            ExoPlayerSingleton.preview(context, mediaUri, 0, Long.MAX_VALUE)
            _isPlaying.value = true
            _currentlyPlaying.value = title

            ExoPlayerSingleton.playerStateCallback = object : ExoPlayerSingleton.PlayerStateCallback {
                override fun onPlaybackStateChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (!isPlaying) {
                        // If playback stopped, clear the currently playing state.
                        _currentlyPlaying.value = null
                    }
                }
            }
        }
    }

    fun concatenateSelectedAudioFiles(context: Context) {
        val mediaEditingManager = MediaEditingManager(context)
        val selectedFiles = _downloadableContents.value
            .filter { it.title in _selectedItems.value }
            .mapNotNull { it.filePath }
            .toList()

        if (selectedFiles.isEmpty()) {
            Toast.makeText(context, "No files selected for concatenation.", Toast.LENGTH_SHORT).show()
            return
        }

        val outputFilePath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/Concatenated_${System.currentTimeMillis()}.mp3"

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val success = mediaEditingManager.concatenateSelectedFiles(selectedFiles, outputFilePath)
            withContext(Dispatchers.Main) {
                _isLoading.value = false
                if (success) {
                    Toast.makeText(context, "Files merged successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to merge files.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        ExoPlayerSingleton.playerStateCallback = null
    }

    fun prepareMediaData(mediaUri: Uri) {
        _mediaUri.value = mediaUri

        viewModelScope.launch {
            _isLoading.value = true

            // Calculate and set media duration
            val duration = getMediaDuration(getApplication(), mediaUri)
            _mediaDuration.value = duration

            // Set initial start and end times to full duration by default
            _initialStart.value = 0L
            _initialEnd.value = duration

            // Load waveform data
            processAudioFile(getApplication(), mediaUri) { waveformData ->
                _waveform.value = waveformData
                _isLoading.value = false
            }
        }
    }
    private fun executeFFmpegCommandAndGetOutput(command: String, context: Context): Pair<Int, String> {
        val outputFile = File(context.cacheDir, "ffmpeg_output_${System.currentTimeMillis()}.txt")
        val resultCode = FFmpeg.execute(command)
        val output = if (outputFile.exists()) outputFile.readText() else "No output file generated."
        Log.d("FFmpegOutput", "FFmpeg command output: $output")
        outputFile.delete() // Clean up the output file
        return resultCode to output
    }
    suspend fun executeTransformationWithFFmpeg(
        context: Context,
        inputUri: Uri,
        startTimeMs: Long,
        endTimeMs: Long,
        filename: String
    ) {
        withContext(Dispatchers.Main) {
            _isLoading.value = true
        }
        val inputFile = getTempFileForTrimming(context, inputUri)
        val processedFile = ensureCompatibleFormat(inputFile)
        // Change the output file extension to .wav
        val outputFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "$filename.wav")

        outputFile.parentFile?.mkdirs()

        // Update the FFmpeg command to output a WAV file
        // Here we use pcm_s16le codec which is typical for WAV files, and we also specify the sample rate and number of channels.
        val command = buildFFmpegTrimCommand(
            inputFile = processedFile.path,
            outputFile = outputFile.path,
            startTimeMs = startTimeMs,
            endTimeMs = endTimeMs,
            outputFormat = "wav", // Specify WAV output format
            codec = "pcm_s16le" // Specify PCM codec typical for WAV files
        )

        Log.d("FFmpegCommand", "Executing FFmpeg command: $command")
        val (result, output) = executeFFmpegCommandAndGetOutput(command, context)

        withContext(Dispatchers.Main) {
            if (result == 0) {
                val movedFileUri = moveFileToPublicMusicFolder(context, outputFile, filename)
                if (movedFileUri != null) {
                    val trimmedFileData = TrimmedFileData(
                        title = filename,
                        filePath = movedFileUri.toString(), // Store URI as a string
                        duration = endTimeMs - startTimeMs
                    )
                    // Save the data
                    saveTrimmedFileData(trimmedFileData)
                    Log.d("Transformation", "File successfully moved to Music folder: $movedFileUri")
                    Toast.makeText(context, "Transformation completed successfully", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("Transformation", "Failed to move file to Music folder")
                    Toast.makeText(context, "Failed to move file", Toast.LENGTH_LONG).show()
                }
                inputFile.delete()
                processedFile.takeIf { it != inputFile }?.delete()
            } else {
                Log.e("Transformation", "Transformation failed: $output")
                Toast.makeText(context, "Transformation failed", Toast.LENGTH_LONG).show()
            }
            _isLoading.value = false
        }
    }

    // Update the buildFFmpegTrimCommand to accept outputFormat and codec parameters
    private fun buildFFmpegTrimCommand(
        inputFile: String,
        outputFile: String,
        startTimeMs: Long,
        endTimeMs: Long,
        outputFormat: String,
        codec: String
    ): String {
        val startTime = startTimeMs / 1000
        val endTime = endTimeMs / 1000
        // Include codec and output format in the FFmpeg command
        return "-y -i '$inputFile' -ss $startTime -to $endTime -acodec $codec -ar 44100 -ac 2 '$outputFile'"
    }


    fun saveTrimmedFileData(trimmedFileData: TrimmedFileData) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("trimmedFiles", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val existingDataJson = sharedPreferences.getString("trimmedDataList", "[]")
        val existingData: MutableList<TrimmedFileData> = gson.fromJson(existingDataJson, object : TypeToken<MutableList<TrimmedFileData>>() {}.type)

        existingData.add(trimmedFileData)
        editor.putString("trimmedDataList", gson.toJson(existingData))
        editor.apply()
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        context.contentResolver.openInputStream(uri).use { inputStream ->
            val tempFile = File.createTempFile("source", "audio", context.cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
            }
            return tempFile
        }
    }
    private fun buildFFmpegTrimCommand(inputFile: String, outputFile: String, startTimeMs: Long, endTimeMs: Long): String {
        val startTime = startTimeMs / 1000
        val endTime = endTimeMs / 1000
        return "-y -i '$inputFile' -ss $startTime -to $endTime -acodec copy '$outputFile'"
    }
    fun getMediaDuration(context: Context, mediaUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        try {
            context.contentResolver.openFileDescriptor(mediaUri, "r")?.use { parcelFileDescriptor ->
                retriever.setDataSource(parcelFileDescriptor.fileDescriptor)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                return durationStr?.toLong() ?: 0L
            }
        } catch (e: Exception) {
            Log.e("TrimViewModel", "Error retrieving media duration", e)
        } finally {
            retriever.release()
        }
        return 0L // Default to 0 if there's an error
    }

    private suspend fun ensureCompatibleFormat(file: File): File = withContext(Dispatchers.IO) {
        val checkCodecCommand = "-v error -show_entries stream=codec_name -of default=noprint_wrappers=1:nokey=1 ${file.absolutePath}"
        FFmpeg.execute(checkCodecCommand)
        val newFilePath = "${file.parent}/${file.nameWithoutExtension}_converted.mp3"
        val reencodeCommand = "-y -i ${file.absolutePath} -acodec libmp3lame -b:a 320k $newFilePath"
        val reencodeResult = FFmpeg.execute(reencodeCommand)
        if (reencodeResult == 0) {
            File(newFilePath)
        } else {
            file
        }
    }

    private fun getTempFileForTrimming(context: Context, uri: Uri): File {
        val tempFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.mp3")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun moveFileToPublicMusicFolder(context: Context, file: File, title: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, title)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }
        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                contentResolver.update(it, values, null, null)
            }
            file.delete()
            return uri
        }
        return null
    }
}