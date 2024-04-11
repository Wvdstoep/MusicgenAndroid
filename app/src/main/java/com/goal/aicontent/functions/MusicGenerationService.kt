package com.goal.aicontent.functions

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MusicGenerationService {
    @Multipart
    @POST("generate_continuation")
    suspend fun generateContinuation(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody?,
        @Part("duration") duration: RequestBody?
    ): Response<DownloadResponse>

    @GET("task_status/{task_id}")
    suspend fun checkTaskStatus(@Path("task_id") taskId: String): Response<TaskStatusResponse>

    @Multipart
    @POST("generate_followup")
    suspend fun generateFollowup(
        @Part file: MultipartBody.Part,
        @Part("model") modelName: RequestBody, // Assuming model is required
        @Part("duration") duration: RequestBody, // Assuming duration is required and sent as a string
        @Part("description") description: RequestBody? = null // Optional, keep or remove based on need
    ): Response<DownloadResponse>

    @POST("generate")
    suspend fun generateMusic(@Body request: MusicPromptRequest): Response<DownloadResponse>
}


