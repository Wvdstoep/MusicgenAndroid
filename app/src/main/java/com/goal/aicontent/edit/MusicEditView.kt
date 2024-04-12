package com.goal.aicontent.edit

import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.goal.aicontent.functions.AudioFile
import com.goal.aicontent.models.AudioEditViewModel
import com.goal.aicontent.models.TrimViewModel
import kotlin.math.max
import kotlin.math.min

@OptIn(UnstableApi::class)
@Composable
fun MusicEditView() {
    val audioEditViewModel: AudioEditViewModel = viewModel()
    val timelineAudios by audioEditViewModel.timelineAudios.observeAsState(emptyList())
    val isPlaying by audioEditViewModel.isPlaying.observeAsState(false)
    val isLoading by audioEditViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            // Timeline display area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                TimelineRow(timelineAudios, audioEditViewModel)
            }

            // Control buttons with icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { audioEditViewModel.playTimelineAudios() }, enabled = !isLoading) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }
                IconButton(onClick = { audioEditViewModel.clearTimeline() }, enabled = !isLoading) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Timeline")
                }
                IconButton(onClick = { audioEditViewModel.removeLastAudio() }, enabled = !isLoading) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = "Remove Last Audio")
                }
                IconButton(onClick = { audioEditViewModel.saveConcatenatedAudio(context) }, enabled = !isLoading) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save Audio")
                }
            }

            // File picker and loading indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    FilePickerTimelineButton(audioEditViewModel)
                }
            }
        }
    }
}



@OptIn(UnstableApi::class)
@Composable
fun FilePickerTimelineButton(audioEditViewModel: AudioEditViewModel) {
    val context = LocalContext.current
    val timelinePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        audioEditViewModel.addToTimeline(uris, context)
    }

    Button(onClick = { timelinePickerLauncher.launch("audio/*") }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Audio")
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Add to Timeline")
    }
}
@Composable
fun TimelineRow(timelineAudios: List<AudioFile>, audioEditViewModel: AudioEditViewModel) {
    val totalDurationMinutes = 10  // Fixed timeline duration in minutes
    val totalDurationSeconds = totalDurationMinutes * 60f  // Convert to seconds, 600 seconds
    val maxTimelineWidthDp = 6000.dp  // Maximum width of the timeline in dp
    val playbackMarkerPosition by audioEditViewModel.playbackMarkerPosition.collectAsState()

    val density = LocalDensity.current
    val maxTimelineWidthPx = with(density) { maxTimelineWidthDp.toPx() }
    val widthPerSecond = maxTimelineWidthPx / totalDurationSeconds

    Box(modifier = Modifier.height(350.dp).horizontalScroll(rememberScrollState()).width(maxTimelineWidthDp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val paint = Paint().apply {
                color = android.graphics.Color.BLUE
                strokeWidth = with(density) { 2.dp.toPx() }
            }

            // Draw Static Timeline at the bottom
            drawStaticTimeline(totalDurationSeconds.toInt(), size.width, paint, size.height, density)

            // Draw all waveforms positioned and scaled within the 10-minute width
            timelineAudios.forEach { audioFile ->
                val fileDurationSeconds = audioFile.duration / 1000f  // Duration in seconds
                val fileWidthPx = widthPerSecond * fileDurationSeconds  // Width of the waveform on the canvas
                val startOffsetPx = widthPerSecond * (audioFile.startTimeInSeconds / 1000f)  // Start offset on the canvas
                drawWaveform(audioFile, startOffsetPx, fileWidthPx, size.height, paint)
            }

            // Calculate marker's x position
            val markerXPos = playbackMarkerPosition * maxTimelineWidthPx  // Convert position proportion to pixels
            drawLine(
                color = Color.Red,
                start = Offset(x = markerXPos, y = 0f),
                end = Offset(x = markerXPos, y = size.height),
                strokeWidth = 4.dp.toPx()
            )
        }
    }
}


fun DrawScope.drawStaticTimeline(totalSeconds: Int, totalWidth: Float, paint: Paint, canvasHeight: Float, density: Density) {
    val segmentWidthPx = totalWidth / totalSeconds
    val bottomY = canvasHeight - with(density) { 10.dp.toPx() }

    // Draw main timeline line at the bottom
    drawLine(Color.LightGray, Offset(0f, bottomY), Offset(totalWidth, bottomY), paint.strokeWidth)

    // Draw minute and second markers
    for (i in 0 until totalSeconds) {
        val xPosition = i * segmentWidthPx
        val isMinuteMarker = i % 60 == 0
        val isTenSecondMarker = i % 10 == 0 && !isMinuteMarker
        val tickHeight = if (isMinuteMarker) canvasHeight / 4 else canvasHeight / 10

        paint.color = if (isMinuteMarker) android.graphics.Color.BLACK else android.graphics.Color.GRAY

        // Draw larger marker for every 10 seconds
        if (isTenSecondMarker) {
            drawLine(
                Color(paint.color),
                Offset(xPosition, bottomY),
                Offset(xPosition, bottomY - tickHeight * 1.5f),  // Adjust multiplier for larger marker
                paint.strokeWidth
            )
        } else {
            drawLine(
                Color(paint.color),
                Offset(xPosition, bottomY),
                Offset(xPosition, bottomY - tickHeight),
                paint.strokeWidth
            )
        }

        // Add time stamp under each minute marker
        if (isMinuteMarker) {
            drawContext.canvas.nativeCanvas.drawText(
                "${i / 60} min",
                xPosition,
                bottomY + 20,  // Position text below the minute marker line
                paint.apply {
                    textSize = with(density) { 12.sp.toPx() }
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}


fun DrawScope.drawWaveform(audioFile: AudioFile, startOffsetPx: Float, fileWidthPx: Float, canvasHeight: Float, paint: Paint) {
    val actualDurationSeconds = audioFile.duration.toFloat()
    val waveformWidthPx = min(fileWidthPx, (actualDurationSeconds / audioFile.duration) * fileWidthPx)
    val widthPerSample = waveformWidthPx / max(audioFile.waveform.size, 1)
    val maxAmplitude = audioFile.waveform.maxOrNull() ?: 1f
    val centerHeight = canvasHeight * 0.6f  // Adjust this to position the waveform vertically

    audioFile.waveform.forEachIndexed { index, amplitude ->
        val lineHeight = (amplitude / maxAmplitude) * canvasHeight * 0.5f  // Adjust multiplier to control height
        val yStart = centerHeight - lineHeight / 2
        val yEnd = centerHeight + lineHeight / 2
        val xPos = startOffsetPx + index * widthPerSample
        drawLine(Color.Blue, Offset(xPos, yStart), Offset(xPos, yEnd), paint.strokeWidth)
    }
}
