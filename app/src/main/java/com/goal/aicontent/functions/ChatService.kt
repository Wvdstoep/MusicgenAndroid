package com.goal.aicontent.functions

import com.goal.aicontent.functions.ChatRequest
import com.goal.aicontent.functions.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface ChatService {
    @POST("chat/completions")
    fun createChatCompletion(@Body request: ChatRequest): Call<ChatResponse>
}

