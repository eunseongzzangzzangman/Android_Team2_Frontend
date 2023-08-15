package com.example.frontend.retrofit




import com.example.frontend.restaurant.PageListModel
import retrofit2.http.*


interface NetworkService {


    //부산맛집정보성비스
    // itemModel, PageListModel 참고
//    http://apis.data.go.kr/6260000/FoodService/getFoodKr?serviceKey=인증키&numOfRows=10&pageNo=1
    @GET("FoodService/getFoodKr")
    fun getList(
        @Query("serviceKey") serviceKey: String?,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("resultType") resultType : String
    ): retrofit2.Call<PageListModel>

    // 부산 도보여행 정보 서비스
    // itemModel2, PageListModel2 참고
    //http://apis.data.go.kr/6260000/WalkingService/getWalkingKr?serviceKey=인증키&numOfRows=10&pageNo=1
    //예시
    //https://apis.data.go.kr/6260000/WalkingService/getWalkingKr?serviceKey=ALRX9GpugtvHxcIO%2FiPg1vXIQKi0E6Kk1ns4imt8BLTgdvSlH%2FAKv%2BA1GcGUQgzuzqM3Uv1ZGgpG5erOTDcYRQ%3D%3D&pageNo=1&numOfRows=100&resultType=json
//    @GET("WalkingService/getWalkingKr")
//    fun getList2(
//        @Query("serviceKey") serviceKey: String?,
//        @Query("pageNo") pageNo: Int,
//        @Query("numOfRows") numOfRows: Int,
//        @Query("resultType") resultType : String
//    ): retrofit2.Call<PageListModel2>

}