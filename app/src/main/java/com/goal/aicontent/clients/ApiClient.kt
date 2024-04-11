package com.goal.aicontent.clients

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(2000, TimeUnit.SECONDS)
        .readTimeout(2000, TimeUnit.SECONDS)
        .writeTimeout(2000, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.14:5000")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
}
