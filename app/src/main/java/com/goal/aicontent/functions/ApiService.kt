package com.goal.aicontent.functions

import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit API Service
interface ApiService {
    @GET("api/items")
    suspend fun getItems(): List<TaskItem>

    @GET("api/search")
    suspend fun searchItemsByCategory(@Query("category") category: String): List<TaskItem>
}