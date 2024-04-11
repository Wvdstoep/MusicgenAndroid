package com.goal.aicontent.edit

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.goal.aicontent.models.TrimViewModel
import com.goal.aicontent.functions.ExoPlayerSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

@OptIn(UnstableApi::class)
@Composable
fun TrimView(
    navController: NavController,
    trimViewModel: TrimViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val mediaUri by trimViewModel.mediaUri.collectAsState()
    val mediaDuration by trimViewModel.mediaDuration.collectAsState()
    var startTime by remember { mutableStateOf(trimViewModel.initialStart.value) }
    var endTime by remember { mutableStateOf(trimViewModel.initialEnd.value) }
    var customFileName by remember { mutableStateOf("") }
    val isLoading by trimViewModel.isLoading.observeAsState()
    val isPlaying by trimViewModel.isPlaying.collectAsState()
    val waveform by trimViewModel.waveform.collectAsState()
    Log.d("WaveformView", "Waveform data size: ${waveform.size}")
    var isFullScreen by remember { mutableStateOf(false) }

    val playbackPosition by trimViewModel.playbackPosition.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val isViewInitialized = remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(1f) } // Moved here

    val zoomPanState = remember { ZoomPanState() }

    LaunchedEffect(startTime) {
        if (isViewInitialized.value) {
            mediaUri?.let {
                ExoPlayerSingleton.preview(context, it, startTime, endTime)
            }
        } else {
            isViewInitialized.value = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Make Column scrollable
    ) {
        if (isLoading == true) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            TextField(
                value = customFileName,
                onValueChange = { customFileName = it },
                label = { Text("File Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { mediaUri?.let {
                    ExoPlayerSingleton.preview(context,
                        it, startTime, endTime)
                } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preview", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            MediaTimeline(
                mediaDuration = mediaDuration,
                startTime = startTime,
                endTime = endTime,
                isPlaying = isPlaying,
                playbackPosition = playbackPosition,
                onStartTimeChanged = { newStart -> startTime = newStart.coerceAtMost(endTime - 1000) },
                onEndTimeChanged = { newEnd -> endTime = newEnd.coerceAtLeast(startTime + 1000) },
                onUpdatePlaybackPosition = { newPosition -> trimViewModel.updateUserPlaybackPosition(newPosition) },
                waveform = waveform,
                trimViewModel = trimViewModel,
                zoomPanState = zoomPanState,
            )
            Button(onClick = { isFullScreen = !isFullScreen }) {
                Text(if (isFullScreen) "Exit Full Screen" else "Full Screen")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TimeSliders(
                startTime = startTime,
                endTime = endTime,
                mediaDuration = mediaDuration,
                volume = volume, // Pass volume
                onVolumeChange = { volume = it }, // Pass lambda to update volume
                trimViewModel = trimViewModel
            ) { newStart, newEnd ->
                startTime = newStart
                endTime = newEnd
            }



            Spacer(modifier = Modifier.height(16.dp))

            // Confirm and cancel buttons
            Row {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                // Execute trimming or any long-running task here
                                trimViewModel.executeTransformationWithFFmpeg(
                                    context = context,
                                    inputUri = mediaUri ?: Uri.EMPTY,
                                    startTimeMs = startTime,
                                    endTimeMs = endTime,
                                    filename = customFileName
                                )
                            }
                            // Once the above operation is complete, navigate back or update UI here
                            // as these actions need to be performed on the Main (UI) thread.
                            withContext(Dispatchers.Main) {
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Trim")
                }

            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        mediaUri?.let { uri ->
                            trimViewModel.applyVolumeAdjustment(
                                context = context,
                                inputUri = uri,
                                startTimeMs = startTime,
                                endTimeMs = endTime,
                                volume = volume, // Use volume directly
                                filename = customFileName
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Volume")
            }


            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
        if (isFullScreen) {
            FullScreenWaveformView(
                waveform = waveform,
                onClose = { isFullScreen = false }
            )
        }
    }
}

@Composable
fun WaveformView(
    waveform: List<Float>,
    zoomPanState: ZoomPanState,
    modifier: Modifier = Modifier
) {
    // Setup for handling gestures
    val gestureModifier = modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            val newZoomFactor = (zoomPanState.zoomFactor * zoom).coerceIn(1f, 5f)
            zoomPanState.updateZoomFactor(newZoomFactor)

            val maxPanOffset = waveform.size / newZoomFactor
            val newPanOffset = (zoomPanState.panOffset + pan.x).coerceIn(-maxPanOffset, maxPanOffset)
            zoomPanState.updatePanOffset(newPanOffset)
        }
    }

    val visibleSampleCount = (waveform.size / zoomPanState.zoomFactor).toInt()
    val startSampleIndex = (-(zoomPanState.panOffset / 100) * visibleSampleCount).toInt().coerceIn(0, waveform.size - visibleSampleCount)

    Canvas(modifier = gestureModifier) {
        val sampleStep = max(1, waveform.size / visibleSampleCount)
        for (i in 0 until visibleSampleCount step sampleStep) {
            val index = startSampleIndex + i
            val amplitude = waveform[index.coerceIn(waveform.indices)]
            val lineHeight = amplitude * size.height
            val xPosition = (i.toFloat() / visibleSampleCount) * size.width

            drawLine(
                Color.White,
                Offset(xPosition, size.height / 2 - lineHeight / 2),
                Offset(xPosition, size.height / 2 + lineHeight / 2),
                strokeWidth = 1f
            )
        }
    }
}

// Adjust the logic here based on your zoom levels and desired label density
fun calculateLabelFrequency(zoomFactor: Float): Int {
    return when {
        zoomFactor < 2f -> 5 // Less frequent labels when zoomed out more
        else -> 1 // More frequent labels when zoomed in
    }
}

fun formatTimeLabel(seconds: Int): String {
    val totalSeconds = seconds.toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@OptIn(UnstableApi::class)
@Composable
fun MediaTimeline(
    mediaDuration: Long,
    startTime: Long,
    playbackPosition: Long,
    endTime: Long,
    onUpdatePlaybackPosition: (Long) -> Unit,
    isPlaying: Boolean,
    zoomPanState: ZoomPanState, // Add this parameter
    onStartTimeChanged: (Long) -> Unit,
    onEndTimeChanged: (Long) -> Unit,
    trimViewModel: TrimViewModel,
    waveform: List<Float>,
) {
    val totalWidth = remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .height(100.dp)
            .fillMaxWidth()
            .background(Color.LightGray)
            .onSizeChanged { totalWidth.value = it.width.toFloat() }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    val proportion = dragAmount / totalWidth.value
                    val timeChange = (proportion * mediaDuration).toLong()
                    val newPlaybackPosition = (playbackPosition + timeChange).coerceIn(startTime, endTime)
                    onUpdatePlaybackPosition(newPlaybackPosition)
                }
            }
    ) {
        WaveformView(
            waveform = waveform,
            zoomPanState = zoomPanState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate positions
            val startMarkerX = canvasWidth * (startTime.toFloat() / mediaDuration)
            val endMarkerX = canvasWidth * (endTime.toFloat() / mediaDuration)
            val playbackMarkerX = if (isPlaying) {
                // If playing, the marker moves with the playback position
                size.width * (playbackPosition.toFloat() / mediaDuration)
            } else {
                // If not playing, the marker sticks to the start time
                size.width * (startTime.toFloat() / mediaDuration)
            }
            drawLine(
                color = Color.Blue, // Different color to distinguish it
                start = Offset(x = playbackMarkerX, y = 0f),
                end = Offset(x = playbackMarkerX, y = size.height),
                strokeWidth = 4f
            )
            // Draw the timeline
            drawLine(
                color = Color.White,
                start = Offset(x = 0f, y = canvasHeight / 2),
                end = Offset(x = canvasWidth, y = canvasHeight / 2),
                strokeWidth = 4f
            )

            // Draw start marker
            drawLine(
                color = Color.Green,
                start = Offset(x = startMarkerX, y = 0f),
                end = Offset(x = startMarkerX, y = canvasHeight * 0.75f),
                strokeWidth = 8f
            )

            // Draw end marker
            drawLine(
                color = Color.Red,
                start = Offset(x = endMarkerX, y = 0f),
                end = Offset(x = endMarkerX, y = canvasHeight * 0.75f),
                strokeWidth = 8f
            )

            // Text paint setup for drawing times
            val textPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = 20f
                color = android.graphics.Color.BLACK
            }

            val startTimeText = trimViewModel.formatTime(startTime)
            drawContext.canvas.nativeCanvas.drawText(
                startTimeText,
                startMarkerX - textPaint.measureText(startTimeText) - 10f, // 10f is a small offset for spacing
                canvasHeight * 1f, // Positioned at 3/4 height of the marker
                textPaint
            )

            val endTimeText = trimViewModel.formatTime(endTime)
            drawContext.canvas.nativeCanvas.drawText(
                endTimeText,
                endMarkerX + 10f, // 10f is a small offset for spacing
                canvasHeight * 1f, // Positioned at 3/4 height of the marker
                textPaint
            )
        }
    }
}


@Composable
fun FullScreenWaveformView(waveform: List<Float>, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        WaveformView(
            waveform = waveform,
            zoomPanState = remember { ZoomPanState() },
            modifier = Modifier.align(Alignment.Center).fillMaxSize()
        )

        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd)) {
            Text("Close", color = Color.White)
        }
    }
}
