package com.example.frontend.dto

import com.google.gson.annotations.SerializedName
data class FoodInfo(
    @SerializedName("rid")
    val rid:String?,
    @SerializedName("rtitle")
    val rtitle:String?,
    @SerializedName("rcity")
    val rcity:String?,
    @SerializedName("rlat")
    val rlat: String?,
    @SerializedName("rlng")
    val rlng: String?,
    @SerializedName("rtel")
    val rtel: String?,
    @SerializedName("rmainimg")
    val rmainimg: String?,
    @SerializedName("rinfo")
    val rinfo: String?,
    @SerializedName("rtotalstar")
    val rtotalstar: String?,
    @SerializedName("rstaravg")
    val rstaravg: String?,
    @SerializedName("rcount")
    val rcount: String?,
    @SerializedName("cid")
    val cid: String?,
)
