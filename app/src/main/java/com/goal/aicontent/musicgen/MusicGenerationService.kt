package com.goal.aicontent.musicgen

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MusicGenerationService {
    @POST("generate")
    suspend fun generateMusic(@Body request: MusicPromptRequest): Response<DownloadResponse>
}


data class MusicPromptRequest(
    val prompt: String,
    val model: String,
    val duration: Float
)
data class DownloadResponse(
    val message: String,
    val download_url: String
)