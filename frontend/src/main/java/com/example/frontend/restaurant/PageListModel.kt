package com.example.frontend.restaurant

import com.google.gson.annotations.SerializedName

//부산맛집탐방
data class PageListModel (
    //var data: List<ItemModel>?
    var getFoodKr: GetFoodKr
)

data class GetFoodKr (
    var item : List<ItemModel4>
)

data class ItemModel4 (
    @SerializedName("UC_SEQ")
    var UC_SEQ: String,
    @SerializedName("TITLE")
    var TITLE: String,
    @SerializedName("MAIN_IMG_NORMAL")
    var MAIN_IMG_NORMAL: String,
    //연락처
    @SerializedName("CNTCT_TEL")
    var CNTCT_TEL: String,
    @SerializedName("LAT")
    var LAT: String,
    @SerializedName("LNG")
    var LNG: String,

)