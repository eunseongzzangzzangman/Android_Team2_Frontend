package com.example.frontend.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.frontend.R
import com.example.frontend.member.LoginActivity
import com.example.frontend.member.SignupActivity
import com.example.frontend.databinding.ActivityMainBinding
import com.example.frontend.db.DBConnect
import com.example.frontend.dto.City
import com.example.frontend.dto.FoodInfo
import com.example.frontend.member.DeleteActivity
import com.example.frontend.member.ModifyActivity
import com.example.frontend.recycler.CityAdapterAdapter2
import com.example.frontend.recycler.FoodImgMyAdapter2
import com.example.frontend.restaurant.AddRestaurantActivity
import com.example.frontend.restaurant.AllListActivity
import com.example.frontend.restaurant.SearchListActivity
import com.example.frontend.service.CityService
import com.example.frontend.service.FoodInfoService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding       ///1kjkjajskjdfkjsdkjf12121212
    lateinit var toggle: ActionBarDrawerToggle  // 메뉴11
    lateinit var cityService: CityService
    lateinit var foodinfoService: FoodInfoService

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("joj","onCreate 호출")

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // SharedPreferences 객체생성=================저장된 값을 가져오기 위해=====================================
        val sharedPreferences = getSharedPreferences("logged_user", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getString("uid", null)
        val uemail = sharedPreferences.getString("uemail", null)
        val upassword = sharedPreferences.getString("upassword", null)
        val uname = sharedPreferences.getString("uname", null)
        val unickname = sharedPreferences.getString("unickname", null)
        val uimg = sharedPreferences.getString("uimg", null)
        val role = sharedPreferences.getString("role", null)


//        Log.d("lys", "uid : $uid")
//        Log.d("lys", "uemail : $uemail")
//        Log.d("lys", "upassword : $upassword")
//        Log.d("lys", "uname : $uname")
//        Log.d("lys", "unickname : $unickname")
//        Log.d("lys", "uimg : $uimg")
//        Log.d("lys", "role : $role")







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


        binding.homeButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        binding.mainDrawerView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.joinmenu) {
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
            } else if (it.itemId == R.id.addHome) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else if (it.itemId == R.id.login) {
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
                logged.putString("role", null)
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
        //쉐어드프리퍼런스의 값에따라 화면에 표시되도록하는 부분====================================================
        // 네비게이션 헤더의 TextView 찾기
        val headerView = binding.mainDrawerView.getHeaderView(0)

        // 네비게이션 메뉴 아이템 찾기
        val navigationMenu = binding.mainDrawerView.menu


        //헤더 안에있는 뷰 접근부분
        val userImageView = headerView.findViewById<ImageView>(R.id.userImageView)
        val loggedUserNickname = headerView.findViewById<TextView>(R.id.loggedUserNickname)
        val loggedUserEmail = headerView.findViewById<TextView>(R.id.loggedUserEmail)
        val requestLogin = headerView.findViewById<TextView>(R.id.requestLogin)


        if (uid != null && uemail != null && upassword != null && uname != null && unickname != null && uimg != null && role != null) {
            val Role = role.toString()
            requestLogin.visibility = View.GONE

            //프로필 이미지 설정
            if (uimg != null) {
                // 이미지가 있는 경우 Glide 등을 사용하여 이미지를 설정
                Glide.with(this)
                    .load(uimg)
                    .into(userImageView)
            } else {
                // 이미지가 없는 경우 기본 이미지 또는 처리를 해줄 수 있음
                userImageView.setImageResource(R.drawable.user_basic)
            }


            // 값을 TextView에 설정
            loggedUserNickname.text = "$unickname 님 환영합니다!"
            loggedUserEmail.text = "Email : $uemail"


            //쉐어드 프리퍼런스에 값이 null이 아닌경우 = 로그인된 경우            에만 ?
            //1. 회원가입, 로그인 버튼 안보이게
            //2. 로그아웃 버튼 보이게

            navigationMenu.findItem(R.id.joinmenu)?.isVisible = false
            navigationMenu.findItem(R.id.login)?.isVisible = false
            navigationMenu.findItem(R.id.logout)?.isVisible = true
            navigationMenu.findItem(R.id.modify)?.isVisible = true
            navigationMenu.findItem(R.id.delete)?.isVisible = true
            navigationMenu.findItem(R.id.addrestaurant)?.isVisible = false

            if(Role.equals("ADMIN")){
                navigationMenu.findItem(R.id.addrestaurant)?.isVisible = true
            }




        } else {

            userImageView.visibility = View.GONE
            loggedUserNickname.visibility = View.GONE
            loggedUserEmail.visibility = View.GONE




            navigationMenu.findItem(R.id.joinmenu)?.isVisible = true
            navigationMenu.findItem(R.id.login)?.isVisible = true
            navigationMenu.findItem(R.id.logout)?.isVisible = false
            navigationMenu.findItem(R.id.modify)?.isVisible = false
            navigationMenu.findItem(R.id.delete)?.isVisible = false
            navigationMenu.findItem(R.id.addrestaurant)?.isVisible = false

        }
        val retrofit = DBConnect.retrofit

        cityService = retrofit.create(CityService::class.java)

        val call: Call<List<City?>?>? = cityService.getcityList()
        Log.d("joj", call.toString())
        call?.enqueue(object : Callback<List<City?>?> {
            override fun onResponse(call: Call<List<City?>?>, response: Response<List<City?>?>) {
                if (response.isSuccessful) {
                    val citys = response.body()
                    // 여기서 받아온 데이터(items)를 처리합니다.
                    val adapter = CityAdapterAdapter2(this@MainActivity, citys)

                    binding.recyclerViewone.adapter = adapter
                    Log.d("joj", "도시 새로고침")
//                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<City?>?>, t: Throwable) {
                // 호출 실패 시 처리합니다.
                
            }
        })


        foodinfoService = retrofit.create(FoodInfoService::class.java)

        val callimg: Call<List<FoodInfo?>?>? = foodinfoService.getFoodstarmaxList()
        
        callimg?.enqueue(object : Callback<List<FoodInfo?>?> {
            override fun onResponse(
                call: Call<List<FoodInfo?>?>,
                response: Response<List<FoodInfo?>?>
            ) {
                if (response.isSuccessful) {
                    val foods = response.body()
                    // 여기서 받아온 데이터(items)를 처리합니다.
                    val adapter = FoodImgMyAdapter2(this@MainActivity, foods)

                    binding.recyclerViewimg.adapter = adapter
                    Log.d("joj", "맛집 탑5 새로고침")
//                    adapter.notifyDataSetChanged()
                    Log.d("joj", "맛집 탑5 새로고침")
                    Log.d("joj", adapter.datas.toString())

                }
            }

            override fun onFailure(call: Call<List<FoodInfo?>?>, t: Throwable) {
                // 호출 실패 시 처리합니다.
            }
        })

        binding.allList.setOnClickListener {
            val intent = Intent(this@MainActivity, AllListActivity::class.java)
            startActivity(intent)

        }

    }

    override fun onStart() {
        super.onStart()
        Log.d("joj","onStart 호출")
    }

    override fun onResume() {
        super.onResume()
        Log.d("joj","onResume 호출")

    }

    override fun onPause() {
        super.onPause()
        Log.d("joj","onPause 호출")
    }

    override fun onStop() {
        super.onStop()
        Log.d("joj","onStop 호출")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("joj","onDestroy 호출")
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}



