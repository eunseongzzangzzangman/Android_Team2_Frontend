package com.example.frontend.member

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.frontend.R
import com.example.frontend.service.ApiService
import com.example.frontend.dto.User
import com.example.frontend.databinding.ActivitySignupBinding
import com.example.frontend.db.DBConnect
import com.example.frontend.db.DBConnect2
import com.example.frontend.dto.Check
import com.example.frontend.main.MainActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var filePath: String
//    private lateinit var inputStream: InputStream

    // Firebase Storage 인스턴스를 초기화
    private val storage = Firebase.storage
    private lateinit var checkImg : String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //checkImg="y" <- 갤러리에서 선택한 이미지
        //checkImg="n" <- 카메라로 찍어서 선택한 이미지
        //checkImg="none" <- drawable에 저장된 기본이미지
        checkImg = "none"



        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
        //갤러리, 사진앱으로 프로필설정하는 부분
        //갤러리 요청 런처
        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            try {

                //calRatio는 원본의 사진을 얼마나 줄일지 비율 값을 나타냄
                val calRatio = calculateInSampleSize(
                    it.data!!.data!!,
                    resources.getDimensionPixelSize(R.dimen.imgSize),
                    resources.getDimensionPixelSize(R.dimen.imgSize)
                )

                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio

                //이미지 로딩
                //사진을 바이트 단위로 읽었음. inputStream : 이미지의 바이트 단위의 결과값
              var inputStream = contentResolver.openInputStream(it.data!!.data!!)!!



                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                bitmap?.let {
                    binding.userImageView.setImageBitmap(bitmap)
                } ?: let{
                    Log.d("kkang", "bitmap null")
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }


        binding.galleryButton.setOnClickListener {
            checkImg = "y"
            //갤러리 앱
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        //카메라 요청 런처
        val requestCameraFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            //카메라 앱
            val calRatio = calculateInSampleSize(
                Uri.fromFile(File(filePath)),
                resources.getDimensionPixelSize(R.dimen.imgSize),
                resources.getDimensionPixelSize(R.dimen.imgSize)
            )
            val option = BitmapFactory.Options()
            option.inSampleSize = calRatio
            val bitmap = BitmapFactory.decodeFile(filePath, option)
            bitmap?.let {
                binding.userImageView.setImageBitmap(bitmap)
            }
        }


        binding.cameraButton.setOnClickListener {
            checkImg = "n"
            //카메라 앱
            //파일 준비
            val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File? =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )
            filePath = file.absolutePath
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "com.example.frontend.fileprovider", file
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            requestCameraFileLauncher.launch(intent)

        }
        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ







        //스프링에 데이터보내는 부분
        binding.signupButton.setOnClickListener {
            signUp()
        }
    }




    private fun signUp() {
        val uemail = binding.signupEmail.text.toString()
        val upassword = binding.signupPassword.text.toString()
        val uname = binding.signupName.text.toString()
        val unickname = binding.signupNickname.text.toString()
        val role = "USER"

        if (uemail.isEmpty() || upassword.isEmpty() || uname.isEmpty() || unickname.isEmpty()) {
            // 어떤 입력값이 비어있으면 토스트 메시지 표시
            Toast.makeText(this, "모든 값을 입력하세요.", Toast.LENGTH_SHORT).show()
        }else {



            val retrofit = DBConnect2.retrofit
            val checkk = Check(uemail)
            val apiService = retrofit.create(ApiService::class.java)
            val call = apiService.check(checkk)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {

                    if (response.isSuccessful) {

                        val check = response.body()
                        if(check.equals("ok!")){

                            if (checkImg.equals("y")) {
                                val storageRef: StorageReference = storage.reference
                                val imgRef: StorageReference = storageRef.child("profile_images/$uemail.jpg")
                                val bitmap = getBitmapFromView(binding.userImageView)
                                val baos = ByteArrayOutputStream()
                                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val data = baos.toByteArray()

                                var uploadTask = imgRef.putBytes(data)
                                uploadTask.addOnSuccessListener {_ ->
                                    Log.d("lys", "이미지 업로드 성공")
                                    // 이미지 업로드 후 다운로드 URL 가져오기
                                    imgRef.downloadUrl.addOnSuccessListener { uri ->
                                        val downloadUrl = uri.toString()
                                        Log.d("lys", "Download URL: $downloadUrl")
                                        val uimg = downloadUrl

                                        // TODO: 필요한 대로 downloadUrl을 사용합니다.
                                        //서버로 값 전송

                                        val retrofit = DBConnect2.retrofit

                                        val user = User(uemail, upassword, uname, unickname, uimg, role)
                                        val apiService = retrofit.create(ApiService::class.java)

                                        val call = apiService.signup(user)
                                        call.enqueue(object : Callback<String> {
                                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                                if (response.isSuccessful) {
                                                    // 성공적으로 응답을 받았을 때의 처리
                                                    Log.d("lys", "갤러리부분")
                                                    val check = response.body()
                                                    // 성공적으로 응답을 받았을 때의 처리. 입력한 값을 db에 저장하고 나서 ok! 를 리턴함
                                                    Log.d("lys", "응답 o, $check")

                                                    //ok! 가 리턴되었다는건 정상적으로 저장이 되었다는 뜻이니까
                                                    if(check.equals("ok!")){
                                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
                                                        Toast.makeText(this@SignupActivity,"회원가입 완료!", Toast.LENGTH_SHORT).show()
                                                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }

                                                } else {
                                                    // 서버로부터 에러 응답을 받았을 때 처리
                                                    Log.d("lys", "갤러리응답x")
                                                }
                                            }

                                            override fun onFailure(call: Call<String>, t: Throwable) {
                                                Log.d("lys", "Error occurred: ${t.message}")
                                                // 네트워크 오류 등의 실패 처리
                                            }
                                        })


                                    }.addOnFailureListener { exception ->
                                        Log.e("lys", "다운로드 URL 가져오기 실패: ${exception.message}")
                                    }
                                }.addOnFailureListener {
                                    Log.e("lys", "이미지 업로드 실패: ${it.message}")
                                    // TODO: 이미지 업로드 실패 시에 할 작업 추가
                                }
                            } else if (checkImg.equals("n")) {
                                val storageRef: StorageReference = storage.reference
                                val imgRef: StorageReference = storageRef.child("profile_images/$uemail.jpg")
                                val stream = FileInputStream(File(filePath))
                                val uploadTask = imgRef.putStream(stream)

                                uploadTask.addOnSuccessListener {_ ->
                                    Log.d("lys", "이미지 업로드 성공")
                                    // 이미지 업로드 후 다운로드 URL 가져오기
                                    imgRef.downloadUrl.addOnSuccessListener { uri ->
                                        val downloadUrl = uri.toString()
                                        Log.d("lys", "Download URL: $downloadUrl")
                                        val uimg = downloadUrl

                                        // TODO: 필요한 대로 downloadUrl을 사용합니다.
                                        //서버로 값 전송

                                        val retrofit = DBConnect2.retrofit

                                        val user = User(uemail, upassword, uname, unickname, uimg, role)
                                        val apiService = retrofit.create(ApiService::class.java)

                                        val call = apiService.signup(user)
                                        call.enqueue(object : Callback<String> {
                                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                                if (response.isSuccessful) {
                                                    // 성공적으로 응답을 받았을 때의 처리
                                                    Log.d("lys", "카메라부분")
                                                    val check = response.body()
                                                    // 성공적으로 응답을 받았을 때의 처리. 입력한 값을 db에 저장하고 나서 ok! 를 리턴함
                                                    Log.d("lys", "응답 o, $check")

                                                    //ok! 가 리턴되었다는건 정상적으로 저장이 되었다는 뜻이니까
                                                    if(check.equals("ok!")){
                                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
                                                        Toast.makeText(this@SignupActivity,"회원가입 완료!", Toast.LENGTH_SHORT).show()
                                                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }

                                                } else {
                                                    // 서버로부터 에러 응답을 받았을 때의 처리
                                                    Log.d("lys", "카메라응답x")
                                                }
                                            }

                                            override fun onFailure(call: Call<String>, t: Throwable) {
                                                Log.d("lys", "Error occurred: ${t.message}")
                                                // 네트워크 오류 등의 실패 처리
                                            }
                                        })


                                    }.addOnFailureListener { exception ->
                                        Log.e("lys", "다운로드 URL 가져오기 실패: ${exception.message}")
                                    }
                                }.addOnFailureListener {
                                    Log.e("lys", "이미지 업로드 실패: ${it.message}")
                                    // TODO: 이미지 업로드 실패 시에 할 작업 추가
                                }
                            } else if (checkImg.equals("none")) {

                                val storageRef: StorageReference = storage.reference
                                val imgRef: StorageReference = storageRef.child("profile_images/$uemail.jpg")

                                val drawableId = R.drawable.user_basic // drawable 폴더에 있는 이미지의 리소스 ID
                                val drawable = resources.getDrawable(drawableId, null)
                                val bitmap = (drawable as BitmapDrawable).bitmap

                                // 이미지를 스트림으로 변환
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                                val byteArray = stream.toByteArray()

                                // 이미지 업로드
                                val uploadTask = imgRef.putBytes(byteArray)
                                uploadTask.addOnSuccessListener {_ ->
                                    Log.d("lys", "이미지 업로드 성공")
                                    // 이미지 업로드 후 다운로드 URL 가져오기
                                    imgRef.downloadUrl.addOnSuccessListener { uri ->
                                        val downloadUrl = uri.toString()
                                        Log.d("lys", "Download URL: $downloadUrl")
                                        val uimg = downloadUrl

                                        // TODO: 필요한 대로 downloadUrl을 사용합니다.
                                        //서버로 값 전송

                                        val retrofit = DBConnect2.retrofit

                                        val user = User(uemail, upassword, uname, unickname, uimg, role)
                                        val apiService = retrofit.create(ApiService::class.java)

                                        val call = apiService.signup(user)


                                        call.enqueue(object : Callback<String> {
                                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                                if (response.isSuccessful) {
                                                    Log.d("lys", "기본이미지부분")
                                                    val check = response.body()
                                                    // 성공적으로 응답을 받았을 때의 처리. 입력한 값을 db에 저장하고 나서 ok! 를 리턴함
                                                    Log.d("lys", "응답 o, $check")

                                                    //ok! 가 리턴되었다는건 정상적으로 저장이 되었다는 뜻이니까
                                                    if(check.equals("ok!")){
                                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
                                                        Toast.makeText(this@SignupActivity,"회원가입 완료!", Toast.LENGTH_SHORT).show()
                                                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }

                                                } else {
                                                    // 서버로부터 에러 응답을 받았을 때의 처리
                                                    Log.d("lys", "기본이미지응답x")
                                                }
                                            }

                                            override fun onFailure(call: Call<String>, t: Throwable) {
                                                Log.d("lys", "응답 x Error occurred: ${t.message}")
                                                // 네트워크 오류 등의 실패처리
                                            }
                                        })


                                    }.addOnFailureListener { exception ->
                                        Log.e("lys", "다운로드 URL 가져오기 실패: ${exception.message}")
                                    }
                                }.addOnFailureListener {
                                    Log.e("lys", "이미지 업로드 실패: ${it.message}")
                                    // TODO: 이미지 업로드 실패 시에 할 작업 추가
                                }
                            }


                        }
                        if(check.equals("no")){
                            Toast.makeText(this@SignupActivity, "이미 등록된 이메일입니다.", Toast.LENGTH_SHORT).show()
                        }


                    } else {
                        Log.d("lys", "체크응답x")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("lys", "Error occurred: ${t.message}")
                }
            })






//            if (checkImg.equals("y")) {
//                val storageRef: StorageReference = storage.reference
//                val imgRef: StorageReference = storageRef.child("profile_images/$uemail.jpg")
//                val bitmap = getBitmapFromView(binding.userImageView)
//                val baos = ByteArrayOutputStream()
//                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//                val data = baos.toByteArray()
//
//                var uploadTask = imgRef.putBytes(data)
//                uploadTask.addOnSuccessListener {_ ->
//                    Log.d("lys", "이미지 업로드 성공")
//                    // 이미지 업로드 후 다운로드 URL 가져오기
//                    imgRef.downloadUrl.addOnSuccessListener { uri ->
//                        val downloadUrl = uri.toString()
//                        Log.d("lys", "Download URL: $downloadUrl")
//                        val uimg = downloadUrl
//
//                        // TODO: 필요한 대로 downloadUrl을 사용합니다.
//                        //서버로 값 전송
//
//                        val retrofit = DBConnect2.retrofit
//
//                        val user = User(uemail, upassword, uname, unickname, uimg, role)
//                        val apiService = retrofit.create(ApiService::class.java)
//
//                        val call = apiService.signup(user)
//                        call.enqueue(object : Callback<String> {
//                            override fun onResponse(call: Call<String>, response: Response<String>) {
//                                if (response.isSuccessful) {
//                                    // 성공적으로 응답을 받았을 때의 처리
//                                    Log.d("lys", "갤러리부분")
//                                    val check = response.body()
//                                    // 성공적으로 응답을 받았을 때의 처리. 입력한 값을 db에 저장하고 나서 ok! 를 리턴함
//                                    Log.d("lys", "응답 o, $check")
//
//                                    //ok! 가 리턴되었다는건 정상적으로 저장이 되었다는 뜻이니까
//                                    if(check.equals("ok!")){
//                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
//                                        Toast.makeText(this@SignupActivity,"회원가입 완료!", Toast.LENGTH_SHORT).show()
//                                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
//                                        startActivity(intent)
//                                    }
//
//                                } else {
//                                    // 서버로부터 에러 응답을 받았을 때 처리
//                                    Log.d("lys", "응답x")
//                                }
//                            }
//
//                            override fun onFailure(call: Call<String>, t: Throwable) {
//                                Log.d("lys", "Error occurred: ${t.message}")
//                                // 네트워크 오류 등의 실패 처리
//                            }
//                        })
//
//
//                    }.addOnFailureListener { exception ->
//                        Log.e("lys", "다운로드 URL 가져오기 실패: ${exception.message}")
//                    }
//                }.addOnFailureListener {
//                    Log.e("lys", "이미지 업로드 실패: ${it.message}")
//                    // TODO: 이미지 업로드 실패 시에 할 작업 추가
//                }
//            } else if (checkImg.equals("n")) {
//                val storageRef: StorageReference = storage.reference
//                val imgRef: StorageReference = storageRef.child("profile_images/$uemail.jpg")
//                val stream = FileInputStream(File(filePath))
//                val uploadTask = imgRef.putStream(stream)
//
//                uploadTask.addOnSuccessListener {_ ->
//                    Log.d("lys", "이미지 업로드 성공")
//                    // 이미지 업로드 후 다운로드 URL 가져오기
//                    imgRef.downloadUrl.addOnSuccessListener { uri ->
//                        val downloadUrl = uri.toString()
//                        Log.d("lys", "Download URL: $downloadUrl")
//                        val uimg = downloadUrl
//
//                        // TODO: 필요한 대로 downloadUrl을 사용합니다.
//                        //서버로 값 전송
//
//                        val retrofit = DBConnect2.retrofit
//
//                        val user = User(uemail, upassword, uname, unickname, uimg, role)
//                        val apiService = retrofit.create(ApiService::class.java)
//
//                        val call = apiService.signup(user)
//                        call.enqueue(object : Callback<String> {
//                            override fun onResponse(call: Call<String>, response: Response<String>) {
//                                if (response.isSuccessful) {
//                                    // 성공적으로 응답을 받았을 때의 처리
//                                    Log.d("lys", "카메라부분")
//                                    val check = response.body()
//                                    // 성공적으로 응답을 받았을 때의 처리. 입력한 값을 db에 저장하고 나서 ok! 를 리턴함
//                                    Log.d("lys", "응답 o, $check")
//
//                                    //ok! 가 리턴되었다는건 정상적으로 저장이 되었다는 뜻이니까
//                                    if(check.equals("ok!")){
//                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
//                                        Toast.makeText(this@SignupActivity,"회원가입 완료!", Toast.LENGTH_SHORT).show()
//                                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
//                                        startActivity(intent)
//                                    }
//
//                                } else {
//                                    // 서버로부터 에러 응답을 받았을 때의 처리
//                                    Log.d("lys", "응답x")
//                                }
//                            }
//
//                            override fun onFailure(call: Call<String>, t: Throwable) {
//                                Log.d("lys", "Error occurred: ${t.message}")
//                                // 네트워크 오류 등의 실패 처리
//                            }
//                        })
//
//
//                    }.addOnFailureListener { exception ->
//                        Log.e("lys", "다운로드 URL 가져오기 실패: ${exception.message}")
//                    }
//                }.addOnFailureListener {
//                    Log.e("lys", "이미지 업로드 실패: ${it.message}")
//                    // TODO: 이미지 업로드 실패 시에 할 작업 추가
//                }
//            } else if (checkImg.equals("none")) {
//
//                val storageRef: StorageReference = storage.reference
//                val imgRef: StorageReference = storageRef.child("profile_images/$uemail.jpg")
//
//                val drawableId = R.drawable.user_basic // drawable 폴더에 있는 이미지의 리소스 ID
//                val drawable = resources.getDrawable(drawableId, null)
//                val bitmap = (drawable as BitmapDrawable).bitmap
//
//                // 이미지를 스트림으로 변환
//                val stream = ByteArrayOutputStream()
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//                val byteArray = stream.toByteArray()
//
//                // 이미지 업로드
//                val uploadTask = imgRef.putBytes(byteArray)
//                uploadTask.addOnSuccessListener {_ ->
//                    Log.d("lys", "이미지 업로드 성공")
//                    // 이미지 업로드 후 다운로드 URL 가져오기
//                    imgRef.downloadUrl.addOnSuccessListener { uri ->
//                        val downloadUrl = uri.toString()
//                        Log.d("lys", "Download URL: $downloadUrl")
//                        val uimg = downloadUrl
//
//                        // TODO: 필요한 대로 downloadUrl을 사용합니다.
//                        //서버로 값 전송
//
//                        val retrofit = DBConnect2.retrofit
//
//                        val user = User(uemail, upassword, uname, unickname, uimg, role)
//                        val apiService = retrofit.create(ApiService::class.java)
//
//                        val call = apiService.signup(user)
//
//
//                        call.enqueue(object : Callback<String> {
//                            override fun onResponse(call: Call<String>, response: Response<String>) {
//                                if (response.isSuccessful) {
//                                    Log.d("lys", "기본이미지부분")
//                                    val check = response.body()
//                                    // 성공적으로 응답을 받았을 때의 처리. 입력한 값을 db에 저장하고 나서 ok! 를 리턴함
//                                    Log.d("lys", "응답 o, $check")
//
//                                    //ok! 가 리턴되었다는건 정상적으로 저장이 되었다는 뜻이니까
//                                    if(check.equals("ok!")){
//                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
//                                        Toast.makeText(this@SignupActivity,"회원가입 완료!", Toast.LENGTH_SHORT).show()
//                                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
//                                        startActivity(intent)
//                                    }
//
//                                } else {
//                                    // 서버로부터 에러 응답을 받았을 때의 처리
//                                    Log.d("lys", "응답 x")
//                                }
//                            }
//
//                            override fun onFailure(call: Call<String>, t: Throwable) {
//                                Log.d("lys", "응답 x Error occurred: ${t.message}")
//                                // 네트워크 오류 등의 실패처리
//                            }
//                        })
//
//
//                    }.addOnFailureListener { exception ->
//                        Log.e("lys", "다운로드 URL 가져오기 실패: ${exception.message}")
//                    }
//                }.addOnFailureListener {
//                    Log.e("lys", "이미지 업로드 실패: ${it.message}")
//                    // TODO: 이미지 업로드 실패 시에 할 작업 추가
//                }
//            }

            //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ



        }

    }


fun getBitmapFromView(view: View): Bitmap? {
    var bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    var canvas = Canvas(bitmap)
    view.draw(canvas)
    return  bitmap
}
    //사진사이즈 조절하는 함수
    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        //비트맵 객체 그대로 사용하면, 사진 원본을 그대로 사용해서 메모리 부족 현상 생김.
        // 그래서, 옵션이라는 속성을 사용.
        val options = BitmapFactory.Options()
        // 실제 비트맵 객체를 생성하는 것 아니고, 옵션 만 설정하겠다라는 의미.
        options.inJustDecodeBounds = true
        try {
            // 실제 원본 사진의 물리 경로에 접근해서, 바이트로 읽음.
            // 사진을 읽은 바이트 단위.
            var inputStream = contentResolver.openInputStream(fileUri)

            //inJustDecodeBounds 값을 true 로 설정한 상태에서 decodeXXX() 를 호출.
            //로딩 하고자 하는 이미지의 각종 정보가 options 에 설정 된다.
            BitmapFactory.decodeStream(inputStream, null, options)
            // 읽었던 원본의 사진의 메모리 사용은 반납.
            inputStream!!.close()
            // 객체를 null 초기화,//
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //비율 계산........................
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        //inSampleSize 비율 계산
        //height ,width 원본의 가로 세로 크기.
        // reqHeight, reqWidth 원하는 크기 사이즈,
        // 이것보다 크면 원본의 사이즈를 반으로 줄이는 작업을 계속 진행.
        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }




}