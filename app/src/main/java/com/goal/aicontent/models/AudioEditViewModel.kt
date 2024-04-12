package com.goal.aicontent.models

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

import com.arthenica.mobileffmpeg.FFmpeg
import com.goal.aicontent.functions.AudioFile
import com.goal.aicontent.functions.ExoPlayerSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.math.abs

@UnstableApi
class AudioEditViewModel(application: Application) : AndroidViewModel(application), Player.Listener {

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerSingleton.getExoPlayer(getApplication()).apply {
            addListener(this@AudioEditViewModel)
        }
    }

    private val _timelineAudios = MutableLiveData<List<AudioFile>>()
    val timelineAudios: LiveData<List<AudioFile>> = _timelineAudios
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying
    private val _playbackMarkerPosition = MutableStateFlow(0f)
    val playbackMarkerPosition: StateFlow<Float> = _playbackMarkerPosition.asStateFlow()

    private val _totalDurationSeconds = MutableLiveData<Float>()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        _timelineAudios.observeForever {
            updateTotalDuration()
        }
        subscribeToPlayerEvents() // Call here to ensure it's used and listening starts appropriately
    }
    private fun updateTotalDuration() {
        val totalMillis = _timelineAudios.value?.sumOf { it.duration } ?: 0L
        _totalDurationSeconds.postValue(totalMillis / 1000f) // Convert milliseconds to seconds
    }

    private val _previousTracksDuration = MutableStateFlow(0L)  // Cumulative duration of all previous tracks in milliseconds

    private fun startTrackingPlayback() {
        viewModelScope.launch {
            while (exoPlayer.isPlaying) {
                val currentPosition = exoPlayer.currentPosition
                val newPosition = calculatePositionInTimeline(currentPosition)
                Log.d("Playback", "Current Position: $currentPosition, Calculated Timeline Position: $newPosition")
                if (_playbackMarkerPosition.value != newPosition) {
                    _playbackMarkerPosition.value = newPosition
                }
                delay(100) // Check more frequently for smoother updates
            }
        }
    }
    private fun calculatePositionInTimeline(currentPosition: Long): Float {
        // Total duration of the timeline in seconds
        val totalTimelineDurationSeconds = 600f

        // Current position plus the duration of all previously played tracks
        val adjustedPositionInMillis = _previousTracksDuration.value + currentPosition

        // Convert to seconds and normalize based on the total timeline duration
        return (adjustedPositionInMillis / 1000f) / totalTimelineDurationSeconds
    }
    fun clearTimeline() {
        viewModelScope.launch {
            _timelineAudios.postValue(emptyList())
            // Optionally reset other related states if necessary
        }
    }

    fun removeLastAudio() {
        viewModelScope.launch {
            val currentList = _timelineAudios.value ?: return@launch
            if (currentList.isNotEmpty()) {
                _timelineAudios.postValue(currentList.dropLast(1))
                // Update cumulative duration or other states if needed
            }
        }
    }
    private fun subscribeToPlayerEvents() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue(isPlaying)
                if (isPlaying) {
                    startTrackingPlayback()
                }
            }
        })
    }

    private suspend fun concatenateAudioFiles(context: Context, uris: List<Uri>, outputFilePath: String): Boolean {
        _isLoading.value = true
        val result = withContext(Dispatchers.IO) {
            val tempDir = File(context.cacheDir, "converted_audio")
            if (!tempDir.exists()) tempDir.mkdir()

            val convertedFiles = uris.mapIndexed { index, uri ->
                val outputFile = File(tempDir, "converted_$index.mp3")
                val commandConvert = arrayOf(
                    "-i", File(uriToTempFile(context, uri).path).absolutePath,
                    "-acodec", "libmp3lame",
                    "-ar", "44100",
                    "-ac", "2",
                    "-b:a", "192k",
                    outputFile.absolutePath
                )
                if (FFmpeg.execute(commandConvert) == 0) outputFile else null
            }.filterNotNull()

            val listContent = convertedFiles.joinToString(separator = "\n") { file ->
                "file '${file.absolutePath}'"
            }
            val listFile = File.createTempFile("ffmpeg_list", ".txt", context.cacheDir).apply {
                writeText(listContent)
            }

            val commandConcat = arrayOf(
                "-f", "concat",
                "-safe", "0",
                "-i", listFile.absolutePath,
                "-c:a", "libmp3lame",
                "-q:a", "2",
                "-y",
                outputFilePath
            )
            val executionResult = FFmpeg.execute(commandConcat)
            listFile.delete()  // Clean up list file
            convertedFiles.forEach { it.delete() }  // Clean up converted files

            executionResult == 0
        }
        _isLoading.value = false
        return result
    }

    fun saveConcatenatedAudio(context: Context) {
        val uris = _timelineAudios.value?.map { it.uri } ?: return
        if (uris.isEmpty()) {
            Log.d("AudioEditViewModel", "No audio files to save.")
            return
        }

        val privateFilePath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/SavedAudio_${System.currentTimeMillis()}.mp3"

        viewModelScope.launch(Dispatchers.IO) {
            val success = concatenateAudioFiles(context, uris, privateFilePath)
            if (success) {
                val publicUri = copyToPublicMusicDirectory(context, privateFilePath)
                if (publicUri != null) {
                    File(privateFilePath).delete()  // Delete the private file after successful copy
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Audio saved successfully in Music directory.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to save audio to public directory.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Log.e("AudioEditViewModel", "Failed to save audio.")
                    Toast.makeText(context, "Failed to save audio.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun copyToPublicMusicDirectory(context: Context, filePath: String): Uri? {
        val file = File(filePath)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            put(MediaStore.MediaColumns.SIZE, file.length())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }
        }
        return uri
    }
    fun playTimelineAudios() {
        val uris = _timelineAudios.value?.map { it.uri } ?: return
        val context = getApplication<Application>().applicationContext
        val outputFilePath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/Concatenated_${System.currentTimeMillis()}.mp3"

        viewModelScope.launch {
            val success = concatenateAudioFiles(context, uris, outputFilePath)
            if (success) {
                withContext(Dispatchers.Main) {
                    val mediaItem = MediaItem.fromUri(Uri.parse(outputFilePath))
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                    Log.d("AudioEditViewModel", "Playback started for concatenated file.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    Log.e("AudioEditViewModel", "Failed to concatenate files.")
                }
            }
        }
    }
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
    private fun extractRawAudioData(inputAudioFilePath: String, outputRawFilePath: String) {
        val command = "-y -i '$inputAudioFilePath' -f s16le -ac 1 -ar 44100 '$outputRawFilePath'"
        FFmpeg.execute(command)
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

    fun addToTimeline(uris: List<Uri>, context: Context) {
        _isLoading.value = true  // Set loading state to true at the start
        viewModelScope.launch(Dispatchers.IO) {
            val existingAudios = _timelineAudios.value.orEmpty()
            var lastEndTime = existingAudios.maxOfOrNull { it.startTimeInSeconds + it.duration } ?: 0L

            val newFiles = uris.map { uri ->
                val fileDuration = getMediaDuration(context, uri)
                AudioFile(
                    name = uri.lastPathSegment ?: "Unknown",
                    uri = uri,
                    duration = fileDuration,
                    startTimeInSeconds = lastEndTime,
                    waveform = listOf()  // Initially empty, populated later
                ).also {
                    lastEndTime += fileDuration
                }
            }

            withContext(Dispatchers.Main) {
                _timelineAudios.value = existingAudios + newFiles
                _isLoading.value = false  // Unset loading state after updating LiveData
            }

            // Handle waveform generation
            newFiles.forEach { audioFile ->
                processAudioFile(context, audioFile.uri) { waveform ->
                    updateAudioFileWithWaveform(audioFile.uri, waveform)
                }
            }
        }
    }
    private fun getMediaDuration(context: Context, mediaUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        var duration: Long = 0L  // Initialize default value
        try {
            context.contentResolver.openFileDescriptor(mediaUri, "r")?.use { parcelFileDescriptor ->
                retriever.setDataSource(parcelFileDescriptor.fileDescriptor)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                duration = durationStr?.toLong() ?: 0L
            }
        } catch (e: Exception) {
            Log.e("AudioEditViewModel", "Error retrieving media duration", e)
        } finally {
            retriever.release()  // Ensure retriever is always released
        }
        return duration  // Return outside the try-catch block
    }
    private fun updateAudioFileWithWaveform(uri: Uri, waveform: List<Float>) {
        viewModelScope.launch(Dispatchers.Main) {
            val updatedFiles = _timelineAudios.value?.map { if (it.uri == uri) it.copy(waveform = waveform) else it }
            _timelineAudios.value = updatedFiles
        }
    }
    fun pausePlayback() {
        exoPlayer.pause()
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

}
