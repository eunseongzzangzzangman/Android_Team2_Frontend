package com.example.frontend.dto

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("cid")
    val cid:String,
    @SerializedName("ccity")
    val ccity:String,
    )