package com.example.frontend.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T> (

    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: T,
    @SerializedName("error")
    val error: String

    )
