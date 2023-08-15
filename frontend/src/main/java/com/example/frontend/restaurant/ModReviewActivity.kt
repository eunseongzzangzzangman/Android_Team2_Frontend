package com.example.frontend.restaurant

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.frontend.R
import com.example.frontend.databinding.ActivityModReviewBinding
import com.example.frontend.db.DBConnect
import com.example.frontend.dto.Comment
import com.example.frontend.dto.FoodInfo
import com.example.frontend.main.MainActivity
import com.example.frontend.service.FoodInfoService
import com.example.frontend.service.ReviewService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModReviewActivity : AppCompatActivity() {
    lateinit var binding : ActivityModReviewBinding
    lateinit var reviewService: ReviewService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent  = intent
        // 전달된 값 가져오기
        val id = intent.getStringExtra("id")
        val cmt = intent.getStringExtra("cmt")
        val cmtTime = intent.getStringExtra("cmtTime")
        val reviewimg = intent.getStringExtra("reviewimg")
        val nickname = intent.getStringExtra("nickname")
        val starpoint1 = intent.getStringExtra("starpoint")
        val uid = intent.getStringExtra("uid")
        val rid = intent.getStringExtra("rid")


        binding.id.text = id
        binding.uid.text = uid
        binding.rid.text = rid
        binding.modcontent.text = Editable.Factory.getInstance().newEditable(cmt)
        binding.moddate.text = cmtTime
        val urlImg = reviewimg
        binding.modnickname.text = nickname
        val starpoint = starpoint1?.toDouble()
        if (starpoint != null) {
            // 별점 이미지 설정 작업
            if (starpoint == 5.0) {
                binding.modstarimg1.setImageResource(R.drawable.five)
            } else if (starpoint < 5 && starpoint >= 4.5) {
                binding.modstarimg1.setImageResource(R.drawable.four_half)
            } else if (starpoint < 4.5 && starpoint >= 4) {
                binding.modstarimg1.setImageResource(R.drawable.four)
            } else if (starpoint < 4 && starpoint >= 3.5) {
                binding.modstarimg1.setImageResource(R.drawable.three_half)
            } else if (starpoint < 3.5 && starpoint >= 3) {
                binding.modstarimg1.setImageResource(R.drawable.three)
            } else if (starpoint < 3 && starpoint >= 2.5) {
                binding.modstarimg1.setImageResource(R.drawable.two_half)
            } else if (starpoint < 2.5 && starpoint >= 2) {
                binding.modstarimg1.setImageResource(R.drawable.two)
            } else if (starpoint < 2 && starpoint >= 1.5) {
                binding.modstarimg1.setImageResource(R.drawable.one_half)
            } else if (starpoint < 1.5 && starpoint >= 1) {
                binding.modstarimg1.setImageResource(R.drawable.one)
            } else if (starpoint < 1 && starpoint >= 0.5) {
                binding.modstarimg1.setImageResource(R.drawable.half)
            } else {
                binding.modstarimg1.setImageResource(R.drawable.zro)
            }
        }
        Glide.with(this@ModReviewActivity)
            .asBitmap()
            .load(urlImg)
            .into(object : CustomTarget<Bitmap>(200, 200) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    binding.modavatarView.setImageBitmap(resource)
//                    Log.d("lsy", "width : ${resource.width}, height: ${resource.height}")
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }
            })
        binding.reviewmod.setOnClickListener {
            val retrofit = DBConnect.retrofit
            reviewService = retrofit.create(ReviewService::class.java)
            val id = intent.getStringExtra("id")
            val cmt = binding.modcontent.text
            val cmtTime = intent.getStringExtra("cmtTime")
            val reviewimg = intent.getStringExtra("reviewimg")
            val nickname = intent.getStringExtra("nickname")
            val starpoint1 = intent.getStringExtra("starpoint")
            val uid = intent.getStringExtra("uid")
            val rid = intent.getStringExtra("rid")
            val comment = Comment(
                id,cmt.toString(),cmtTime.toString(),reviewimg.toString(),nickname.toString(),starpoint1.toString(),uid.toString(),rid.toString()
            )
            val call = reviewService.postReviewMod(comment)
            call.enqueue(object : Callback<Comment> {
                override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val message = responseBody?.toString() // 메시지 받아오기
                        Log.d("joj", "서버 응답 메시지: $message")

                    } else {
                        Log.d("joj","서버로부터 응답이 실패")
                    }
                }

                override fun onFailure(call: Call<Comment>, t: Throwable) {
                    // 통신에 실패한 경우 처리할 내용
                    // 예: 에러 메시지 표시 등
                    Log.d("joj","통신에 실패: ${t.message}")
                }
            })
            finish()

        }
        binding.reviewdel.setOnClickListener {
            val retrofit = DBConnect.retrofit

            reviewService = retrofit.create(ReviewService::class.java)
            val id = intent.getStringExtra("id")
            val call: Call<Comment> = reviewService.delreview(id.toString())
            call?.enqueue(object : Callback<Comment?> {
                override fun onResponse(call: Call<Comment?>, response: Response<Comment?>) {

                }

                override fun onFailure(call: Call<Comment?>, t: Throwable) {
                    // 호출 실패 시 처리합니다.
                }
            })
            finish()
        }

    }
}