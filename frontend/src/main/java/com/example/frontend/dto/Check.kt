package com.example.frontend.dto

import com.google.gson.annotations.SerializedName

data class Check(
    @SerializedName("uemail")
    val uemail:String
    )