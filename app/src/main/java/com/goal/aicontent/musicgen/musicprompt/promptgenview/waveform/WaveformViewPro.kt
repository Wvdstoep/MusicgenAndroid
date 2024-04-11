package com.goal.aicontent.musicgen.musicprompt.promptgenview.waveform

import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WaveformViewPro(waveformData: List<Float>, durationInSeconds: Int) {
    val waveformColor = MaterialTheme.colorScheme.primary
    val guidelineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val timestampColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    val density = LocalDensity.current

    Canvas(modifier = Modifier
        .height(100.dp)
        .fillMaxWidth()
        .background(color = backgroundColor)) {
        val strokeWidth = with(density) { 2.dp.toPx() }
        val spaceBetweenBars = with(density) { 4.dp.toPx() }
        val maxAmplitudeHeight = size.height * 0.8f
        val barWidth = (size.width - (waveformData.size - 1) * spaceBetweenBars) / waveformData.size
        val intervals = 5 // How many time markers you want. Adjust accordingly.
        val intervalWidth = size.width / intervals

        // Draw waveform
        waveformData.forEachIndexed { index, amplitude ->
            val x = index * (barWidth + spaceBetweenBars)
            val lineY = maxAmplitudeHeight * amplitude
            drawLine(
                color = waveformColor,
                start = Offset(x, size.height / 2 - lineY / 2),
                end = Offset(x, size.height / 2 + lineY / 2),
                strokeWidth = strokeWidth
            )
        }

        // Draw time markers and guidelines
        (0..intervals).forEach { i ->
            val x = i * intervalWidth
            val time = (durationInSeconds.toFloat() / intervals * i).toInt()
            val timestamp = "${time}s"

            // Draw guidelines
            drawLine(
                color = guidelineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth / 2
            )

            // Draw timestamps
            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    color = Color.parseColor(toHex(timestampColor))
                    textSize = with(density) { 12.sp.toPx() }
                    textAlign = Paint.Align.CENTER
                }
                drawText(timestamp, x, size.height - 5f, paint)
            }
        }
    }
}

// Helper function to convert Color to hex String
private fun toHex(color: androidx.compose.ui.graphics.Color): String {
    return "#${Integer.toHexString(color.toArgb()).toUpperCase().substring(2)}"
}