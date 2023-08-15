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
import com.example.frontend.dto.FoodInfo
import com.example.frontend.restaurant.ItemActivity


//부산맛집
class MyViewHolder2(val binding: ItemMainBinding): RecyclerView.ViewHolder(binding.root)

class MyAdapter2(val context: Context, val datas: List<FoodInfo?>?): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun getItemCount(): Int{
        return datas?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = MyViewHolder2(ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as MyViewHolder2).binding

        //도보 여행
        val food = datas?.get(position)
        binding.firstNameView.text = food?.rtitle
        val urlImg = food?.rmainimg
        binding.contactView.text = food?.rtel
        binding.starpoint.text = food?.rstaravg+"점"
        val starpoint = food?.rstaravg?.toDouble()
        if (starpoint != null) {
            if (starpoint == 5.0) {
                binding.starimg.setImageResource(R.drawable.five)
            }else if(starpoint < 5 && starpoint >= 4.5){
                binding.starimg.setImageResource(R.drawable.four_half)
            }else if(starpoint < 4.5 && starpoint >= 4){
                binding.starimg.setImageResource(R.drawable.four)
            }else if(starpoint < 4 && starpoint >= 3.5){
                binding.starimg.setImageResource(R.drawable.three_half)
            }else if(starpoint < 3.5 && starpoint >= 3){
                binding.starimg.setImageResource(R.drawable.three)
            }else if(starpoint < 3 && starpoint >= 2.5){
                binding.starimg.setImageResource(R.drawable.two_half)
            }else if(starpoint < 2.5 && starpoint >= 2){
                binding.starimg.setImageResource(R.drawable.two)
            }else if(starpoint < 2 && starpoint >= 1.5){
                binding.starimg.setImageResource(R.drawable.one_half)
            }else if(starpoint < 1.5 && starpoint >= 1){
                binding.starimg.setImageResource(R.drawable.one)
            }else if(starpoint < 1 && starpoint >= 0.5){
                binding.starimg.setImageResource(R.drawable.half)
            }else{
                binding.starimg.setImageResource(R.drawable.zro)
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
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ItemActivity::class.java)

            intent.putExtra("rid",food?.rid)
            intent.putExtra("rtitle",food?.rtitle)
            intent.putExtra("rcity",food?.rcity)
            intent.putExtra("rlat",food?.rlat)
            intent.putExtra("rlng",food?.rlng)
            intent.putExtra("rtel",food?.rtel)
            intent.putExtra("rinfo",food?.rinfo)
            intent.putExtra("rmainimg",food?.rmainimg)
            intent.putExtra("rtotalstar",food?.rtotalstar)
            intent.putExtra("rstaravg",food?.rstaravg)
            intent.putExtra("rcount",food?.rcount)
            intent.putExtra("rcount",food?.cid)
            Log.d("joj",food?.cid.toString())
            context.startActivity(intent)
        }

    }

}