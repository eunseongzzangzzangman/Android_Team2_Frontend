package com.example.frontend.recycler

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.frontend.R
import com.example.frontend.databinding.ItemMainBinding
import com.example.frontend.databinding.ReviewItemMainBinding
import com.example.frontend.dto.Comment
import com.example.frontend.dto.FoodInfo
import com.example.frontend.restaurant.ItemActivity


//부산맛집
class ReviewMyViewHolder2(val binding: ReviewItemMainBinding): RecyclerView.ViewHolder(binding.root)

class ReviewMyAdapter2(val context: Context, val datas: List<Comment?>?): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun getItemCount(): Int{
        return datas?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = ReviewMyViewHolder2(ReviewItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as ReviewMyViewHolder2).binding
        //리뷰
        val review = datas?.get(position)
        binding.nickname.text = review?.nickname
        val urlImg = review?.reviewimg
        binding.date.text = review?.timestamp
        binding.content.text = review?.cmt
        val starpoint = review?.starpoint?.toDouble()
        if (starpoint != null) {
            if (starpoint == 5.0) {
                binding.starimg1.setImageResource(R.drawable.five)
            }else if(starpoint < 5 && starpoint >= 4.5){
                binding.starimg1.setImageResource(R.drawable.four_half)
            }else if(starpoint < 4.5 && starpoint >= 4){
                binding.starimg1.setImageResource(R.drawable.four)
            }else if(starpoint < 4 && starpoint >= 3.5){
                binding.starimg1.setImageResource(R.drawable.three_half)
            }else if(starpoint < 3.5 && starpoint >= 3){
                binding.starimg1.setImageResource(R.drawable.three)
            }else if(starpoint < 3 && starpoint >= 2.5){
                binding.starimg1.setImageResource(R.drawable.two_half)
            }else if(starpoint < 2.5 && starpoint >= 2){
                binding.starimg1.setImageResource(R.drawable.two)
            }else if(starpoint < 2 && starpoint >= 1.5){
                binding.starimg1.setImageResource(R.drawable.one_half)
            }else if(starpoint < 1.5 && starpoint >= 1){
                binding.starimg1.setImageResource(R.drawable.one)
            }else if(starpoint < 1 && starpoint >= 0.5){
                binding.starimg1.setImageResource(R.drawable.half)
            }else{
                binding.starimg1.setImageResource(R.drawable.zro)
            }
        }
        Glide.with(context)
            .asBitmap()
            .load(urlImg)
            .into(object : CustomTarget<Bitmap>(200, 200) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    binding.avatarView.setImageBitmap(resource)
//                    Log.d("lsy", "width : ${resource.width}, height: ${resource.height}")
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }
            })

    }

}