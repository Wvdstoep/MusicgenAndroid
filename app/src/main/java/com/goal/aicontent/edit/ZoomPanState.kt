package com.goal.aicontent.edit


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ZoomPanState(initialZoom: Float = 1f, initialPan: Float = 0f) {
    var zoomFactor by mutableFloatStateOf(initialZoom)
    var panOffset by mutableFloatStateOf(initialPan)

    fun updateZoomFactor(newZoom: Float) {
        zoomFactor = newZoom
        Log.d("WaveformView", "ZoomFactor updated: $zoomFactor")
    }

    fun updatePanOffset(newPan: Float) {
        panOffset = newPan
        Log.d("WaveformView", "PanOffset updated: $panOffset")
    }
}

