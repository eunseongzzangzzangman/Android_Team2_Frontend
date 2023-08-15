package com.example.frontend.restaurant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontend.R
import com.example.frontend.databinding.ActivityAllListBinding
import com.example.frontend.db.DBConnect
import com.example.frontend.dto.FoodInfo
import com.example.frontend.recycler.MyAdapter2
import com.example.frontend.service.FoodInfoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllListActivity : AppCompatActivity() {
    lateinit var binding : ActivityAllListBinding
    lateinit var foodinfoService : FoodInfoService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_list)

        binding = ActivityAllListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val retrofit = DBConnect.retrofit

        foodinfoService = retrofit.create(FoodInfoService::class.java)

        val call: Call<List<FoodInfo?>?>? = foodinfoService.getFoodInfoList()
        Log.d("joj",call.toString())
        call?.enqueue(object : Callback<List<FoodInfo?>?> {
            override fun onResponse(call: Call<List<FoodInfo?>?>, response: Response<List<FoodInfo?>?>) {
                if (response.isSuccessful) {
                    val foods = response.body()
                    // 여기서 받아온 데이터(items)를 처리합니다.
                    val adapter = MyAdapter2(this@AllListActivity, foods)

                    binding.recyclerView.adapter = adapter

                    binding.recyclerView.addItemDecoration(
                        DividerItemDecoration(
                            this@AllListActivity,
                            LinearLayoutManager.VERTICAL
                        )
                    )
                }
            }

            override fun onFailure(call: Call<List<FoodInfo?>?>, t: Throwable) {
                // 호출 실패 시 처리합니다.
            }
        })
    }
}