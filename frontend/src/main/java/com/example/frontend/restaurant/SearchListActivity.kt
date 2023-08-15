package com.example.frontend.restaurant

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontend.R
import com.example.frontend.databinding.ActivitySearchListBinding
import com.example.frontend.db.DBConnect
import com.example.frontend.dto.FoodInfo
import com.example.frontend.main.MainActivity
import com.example.frontend.member.DeleteActivity
import com.example.frontend.member.LoginActivity
import com.example.frontend.member.ModifyActivity
import com.example.frontend.member.SignupActivity
import com.example.frontend.recycler.MyAdapter2
import com.example.frontend.recycler.SearchMyAdapter2
import com.example.frontend.service.FoodInfoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchListActivity : AppCompatActivity() {
    lateinit var binding : ActivitySearchListBinding
    lateinit var foodinfoService : FoodInfoService
    lateinit var toggle: ActionBarDrawerToggle  // 메뉴11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 객체생성=================저장된 값을 가져오기 위해=====================================
        val sharedPreferences = getSharedPreferences("logged_user", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getString("uid", null)
        val uemail = sharedPreferences.getString("uemail", null)
        val upassword = sharedPreferences.getString("upassword", null)
        val uname = sharedPreferences.getString("uname", null)
        val unickname = sharedPreferences.getString("unickname", null)
        val uimg = sharedPreferences.getString("uimg", null)


        Log.d("lys", "uid : $uid")
        Log.d("lys", "uemail : $uemail")
        Log.d("lys", "upassword : $upassword")
        Log.d("lys", "uname : $uname")
        Log.d("lys", "unickname : $unickname")
        Log.d("lys", "uimg : $uimg")


        //====================토글 메뉴============================
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawer,
            R.string.drawer_opened,
            R.string.drawer_closed
        )
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        binding.mainDrawerView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.joinmenu) {
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
            } else if (it.itemId == R.id.login) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else if (it.itemId == R.id.logout) {

                //로그인할때 저장했던 객체 다시가져오기
                var logged = sharedPreferences.edit()

                //저장되어있는 값 null로 초기화
                logged.putString("uid", null)
                logged.putString("uemail", null)
                logged.putString("upassword", null)
                logged.putString("uname", null)
                logged.putString("unickname", null)
                logged.putString("uimg", null)

                // 변경 사항을 커밋하여 저장
                logged.apply()

                Toast.makeText(this, "로그아웃 완료", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

            } else if (it.itemId == R.id.modify) {
                val intent = Intent(this, ModifyActivity::class.java)
                startActivity(intent)
            } else if (it.itemId == R.id.delete) {
                val intent = Intent(this, DeleteActivity::class.java)
                startActivity(intent)
            } else if (it.itemId == R.id.addrestaurant) {
                val intent = Intent(this, AddRestaurantActivity::class.java)
                startActivity(intent)
            }
            true
        }


        val intent = intent
        // 전달된 값 가져오기
        val cid = intent.getStringExtra("cid")
        val ccity = intent.getStringExtra("ccity")
        Log.d("joj",cid.toString())
        Log.d("joj",ccity.toString())

        val retrofit = DBConnect.retrofit

        foodinfoService = retrofit.create(FoodInfoService::class.java)

        val call: Call<List<FoodInfo?>?>? = foodinfoService.getSearchList(cid.toString())
        Log.d("joj",call.toString())
        call?.enqueue(object : Callback<List<FoodInfo?>?> {
            override fun onResponse(call: Call<List<FoodInfo?>?>, response: Response<List<FoodInfo?>?>) {
                if (response.isSuccessful) {
                    val foods = response.body()
                    // 여기서 받아온 데이터(items)를 처리합니다.
                    val adapter = SearchMyAdapter2(this@SearchListActivity, foods)

                    binding.recyclerView.adapter = adapter

                    binding.recyclerView.addItemDecoration(
                        DividerItemDecoration(
                            this@SearchListActivity,
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