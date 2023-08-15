package com.example.frontend.service

import com.example.frontend.dto.City
import retrofit2.Call
import retrofit2.http.GET


interface CityService {
    @GET("/cityfindAll")
    fun getcityList(): Call<List<City?>?>?
}