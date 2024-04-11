package com.goal.aicontent.functions

import retrofit2.http.GET

// Retrofit API Service
interface ApiService {
    @GET("api/items")
    suspend fun getItems(): List<TaskItem>
}