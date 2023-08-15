package com.example.frontend.service

import com.example.frontend.dto.ApiResponse
import com.example.frontend.dto.Comment
import com.example.frontend.dto.FoodInfo
import com.example.frontend.dto.Login
import com.example.frontend.dto.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReviewService {
    @POST("comments")
    fun postComment(@Body comment: Comment): Call<String>

    @GET("/getReviewList")
    fun getReviewList(@Query("uid") uid: String,@Query("rid") rid: String): Call<List<Comment?>?>?
    @GET("/getReviewOne")
    fun getReviewOne(@Query("uid") uid: String,@Query("rid") rid: String): Call<Comment?>?
    @POST("postReviewMod")
    fun postReviewMod(@Body comment: Comment): Call<Comment>
    @POST("delreview")
    fun delreview(@Query("id") id: String): Call<Comment>
}
