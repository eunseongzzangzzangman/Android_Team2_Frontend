package com.example.frontend.db

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DBConnect {
//    private const val BASE_URL = "http://10.100.103.71:8080/"
//    private const val BASE_URL = "http://10.100.103.15:8080/"
    private const val BASE_URL = "http://192.168.0.10:8080/"
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}