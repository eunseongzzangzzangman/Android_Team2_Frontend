package com.example.frontend.dto

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("uemail")
    val uemail:String,
    @SerializedName("upassword")
    val upassword:String,
    @SerializedName("uname")
    val uname:String,
    @SerializedName("unickname")
    val unickname: String,
    @SerializedName("uimg")
    val uimg: String,
    @SerializedName("role")
    val role: String
    )