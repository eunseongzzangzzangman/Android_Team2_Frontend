package com.example.frontend.member

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.frontend.databinding.ActivityLoginBinding

import com.example.frontend.db.DBConnect2
import com.example.frontend.dto.ApiResponse
import com.example.frontend.dto.Login
import com.example.frontend.main.MainActivity
import com.example.frontend.service.ApiService
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


            binding.loginButton.setOnClickListener {
                val uemail = binding.loginEmail.text.toString()
                val upassword = binding.loginPassword.text.toString()
                var uid = ""
                var uname = ""
                var unickname = ""
                var uimg = ""
                val role = ""

                if (uemail.isEmpty() || upassword.isEmpty()) {
                    // 어떤 입력값이 비어있으면 토스트 메시지 표시
                    Toast.makeText(this, "모든 값을 입력하세요.", Toast.LENGTH_SHORT).show()
                } else {




                    val retrofit = DBConnect2.retrofit




//                    val login = Login(uemail, upassword, uid, uname, unickname, uimg)
                    val login = Login(uemail, upassword, uid, uname, unickname, uimg, role)
                    val apiService = retrofit.create(ApiService::class.java)

                    val call = apiService.login(login)
                    call.enqueue(object : Callback<ApiResponse<Login>> {
                        override fun onResponse(call: Call<ApiResponse<Login>>, response: Response<ApiResponse<Login>>) {
                            val apiResponse = response.body()

                            if (apiResponse != null) {
                                if (apiResponse.success) {
                                    val user = apiResponse.data

                                    //데이터 응답확인
                                    Log.d("lys","응답 o $user")

                                    // 쉐어드 프리퍼런스 객체 생성(this말고 앱전체에서공유하는 applicationContext)
                                    val sharedPreferences = applicationContext.getSharedPreferences("logged_user", Context.MODE_PRIVATE)

                                    // 값을 저장하기 위한  객체 생성. 이 객체는 내부적인 특성에 의해 val로 선언해도 작동함. 그러나 값을 수정하는 객체이므로 var이 의미상 더 적절
                                    var logged = sharedPreferences.edit()

                                    //불러온 데이터를 객체에 저장
                                    logged.putString("uid",user.uid)
                                    logged.putString("uemail",user.uemail)
                                    logged.putString("upassword",user.upassword)
                                    logged.putString("uname",user.uname)
                                    logged.putString("unickname",user.unickname)
                                    logged.putString("uimg",user.uimg)
                                    logged.putString("role",user.role)
                                    // 변경 사항을 커밋하여 저장
                                    logged.apply()

                                    // 저장된 값을 로그로 확인. 해당 저장값이 없을때는, 로그에 null이 뜨도록 설정
                                    Log.d("lys", "uid: ${sharedPreferences.getString("uid", null)}")
                                    Log.d("lys", "uemail: ${sharedPreferences.getString("uemail", null)}")
                                    Log.d("lys", "upassword: ${sharedPreferences.getString("upassword", null)}")
                                    Log.d("lys", "uname: ${sharedPreferences.getString("uname", null)}")
                                    Log.d("lys", "unickname: ${sharedPreferences.getString("unickname", null)}")
                                    Log.d("lys", "uimg: ${sharedPreferences.getString("uimg", null)}")
                                    Log.d("lys", "role: ${sharedPreferences.getString("role", null)}")
                                    //현재 쉐어드에 저장된 모든 데이터 확인방법
//                                    val allEntries: Map<String, *> = sharedPreferences.all
//                                    for ((key, value) in allEntries) {
//                                        Log.d("lys", "$key: $value")
//                                    }

                                    if(sharedPreferences.getString("uid",null)!=null &&
                                       sharedPreferences.getString("uemail",null)!=null &&
                                       sharedPreferences.getString("upassword",null)!=null &&
                                       sharedPreferences.getString("uname",null)!=null &&
                                       sharedPreferences.getString("unickname",null)!=null &&
                                       sharedPreferences.getString("uimg",null)!=null
                                    ){
                                        Toast.makeText(this@LoginActivity,"로그인 완료", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }


                                } else {
                                    val errorMessage = apiResponse.error
                                    Log.d("lys","응답 x $errorMessage")
                                    if (errorMessage == "No such email") {
                                        Toast.makeText(this@LoginActivity, "해당 이메일은 없습니다.", Toast.LENGTH_SHORT).show()
                                    } else if (errorMessage == "Incorrect password") {
                                        Toast.makeText(this@LoginActivity, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@LoginActivity, "이메일또는 비밀번호가 다르거나 없습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // 응답이 null인 경우에 대한 처리
                                Log.d("lys","응답 null")
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse<Login>>, t: Throwable) {
                            // 네트워크 요청 실패에 대한 처리
                            Log.d("lys","네트워크요청실패")
                        }
                    })

                }
            }

        }
}