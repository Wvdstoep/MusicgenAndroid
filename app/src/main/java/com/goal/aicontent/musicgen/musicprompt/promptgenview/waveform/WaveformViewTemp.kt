package com.goal.aicontent.musicgen.musicprompt.promptgenview.waveform

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun WaveformViewTemp(waveformData: List<Float>) {
    val waveformColor = MaterialTheme.colorScheme.primary // Customize as needed
    Canvas(modifier = Modifier.height(50.dp).fillMaxWidth()) {
        val strokeWidth = 4f
        val spaceBetweenBars = 8f
        val maxAmplitudeHeight = size.height
        val barWidth = size.width / waveformData.size

        waveformData.forEachIndexed { index, amplitude ->
            val x = index * (barWidth + spaceBetweenBars)
            val lineY = maxAmplitudeHeight * amplitude
            drawLine(
                start = Offset(x, size.height / 2 - lineY / 2),
                end = Offset(x, size.height / 2 + lineY / 2),
                color = waveformColor,
                strokeWidth = strokeWidth
            )
        }
    }
}