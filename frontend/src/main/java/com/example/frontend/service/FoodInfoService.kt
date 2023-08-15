package com.example.frontend.service

import com.example.frontend.dto.FoodInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface FoodInfoService {
    @GET("/foodfindAll")
    fun getFoodInfoList(): Call<List<FoodInfo?>?>?
    @GET("/getFoodstarmaxList")
    fun getFoodstarmaxList(): Call<List<FoodInfo?>?>?
    @GET("/getFoodone")
    fun getFoodone(@Query("rid") rid: String): Call<FoodInfo?>?
    @GET("/getSearchList")
    fun getSearchList(@Query("cid") cid: String): Call<List<FoodInfo?>?>?

    @POST("/postFoodInfo")
    fun postFoodInfo(@Body foodInfo: FoodInfo): Call<FoodInfo>
    @POST("/postFoodInfodelete")
    fun postFoodInfodelete(@Body foodInfo: FoodInfo): Call<FoodInfo>
}