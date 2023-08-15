package com.example.frontend.member

import android.content.Context
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.frontend.R
import com.example.frontend.databinding.ActivityModifyBinding
import com.example.frontend.db.DBConnect2
import com.example.frontend.dto.User
import com.example.frontend.main.MainActivity
import com.example.frontend.service.ApiService
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date

class ModifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModifyBinding
    private lateinit var filePath: String

    // Firebase Storage 인스턴스를 초기화
    private val storage = Firebase.storage

    // 갤러리/카메라 구분용
    private lateinit var checkImg : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //checkImg="y" <- 갤러리에서 선택한 이미지
        //checkImg="n" <- 카메라로 찍어서 선택한 이미지
        //checkImg="none" <- 현재 저장되어있는 프로필이미지
        checkImg = "none"



        // SharedPreferences 객체생성=================저장된 값을 가져오기 위해=====================================
        val sharedPreferences = getSharedPreferences("logged_user", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getString("uid", null)
        val uemail = sharedPreferences.getString("uemail", null)
        val upassword = sharedPreferences.getString("upassword", null)
        val uname = sharedPreferences.getString("uname", null)
        val unickname = sharedPreferences.getString("unickname", null)
        val uimg = sharedPreferences.getString("uimg", null)
        val role = sharedPreferences.getString("role", null)


        Log.d("lys","uid : $uid")
        Log.d("lys","uemail : $uemail")
        Log.d("lys","upassword : $upassword")
        Log.d("lys","uname : $uname")
        Log.d("lys","unickname : $unickname")
        Log.d("lys","uimg : $uimg")
        Log.d("lys","role : $role")

        //기존의 이미지가 정보 수정창에 뜨도록 하는 코드
        val userImageView = findViewById<ImageView>(R.id.userImageView)
        if (uimg != null) {
            Glide.with(this)
                .load(uimg)
                .into(userImageView)
        } else {
            userImageView.setImageResource(R.drawable.user_basic)
        }


        //기존의 데이터들이 미리 입력된상태로 나오게끔하는 코드
        val modifyEmail = findViewById<TextView>(R.id.modifyEmail)
        modifyEmail.text = "$uemail"
        val modifyPassword = findViewById<EditText>(R.id.modifyPassword)
        modifyPassword.setText(upassword)
        val modifyName = findViewById<EditText>(R.id.modifyName)
        modifyName.setText(uname)
        val modifyNickname = findViewById<EditText>(R.id.modifyNickname)
        modifyNickname.setText(unickname)









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
        binding.modifyButton.setOnClickListener{
            modify(uid, uemail, upassword, uname, unickname, uimg, role)
        }


    }


    private fun modify(uid:String?, uemail:String?, upassword:String?, uname:String?, unickname:String?, uimg:String?, role:String?){
        val memail = binding.modifyEmail.text.toString()
        val mpassword = binding.modifyPassword.text.toString()
        val mname = binding.modifyName.text.toString()
        val mnickname = binding.modifyNickname.text.toString()
        val role = role.toString()
        val existingImage = uimg.toString()
//        Log.d("lys","kiki")
//        Log.d("lys","uid : $uid")
//        Log.d("lys","uemail : $uemail")
//        Log.d("lys","upassword : $upassword")
//        Log.d("lys","uname : $uname")
//        Log.d("lys","unickname : $unickname")
//        Log.d("lys","uimg : $uimg")
//
//        Log.d("lys", "memail: $memail")
//        Log.d("lys", "memail: $mpassword")
//        Log.d("lys", "memail: $mname")
//        Log.d("lys", "memail: $mnickname")

        if (memail.isEmpty() || mpassword.isEmpty() || mname.isEmpty() || mnickname.isEmpty()) {
            // 어떤 입력값이 비어있으면 토스트 메시지 표시
            Toast.makeText(this, "모든 값을 입력하세요.", Toast.LENGTH_SHORT).show()
        }else {

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
                        val mimg = downloadUrl

                        // TODO: 필요한 대로 downloadUrl을 사용합니다.
                        //서버로 값 전송

                        val retrofit = DBConnect2.retrofit
                        val user = User(memail, mpassword, mname, mnickname, mimg, role)
                        val apiService = retrofit.create(ApiService::class.java)

                        val call = apiService.modify(user)
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

                                        Log.d("lys","갤러리사진으로 수정해서 통신까지 완료")

                                        //쉐어드프리퍼런스에 수정된값으로 업로드
                                        val sharedPreferences = getSharedPreferences("logged_user", Context.MODE_PRIVATE)
                                        var logged = sharedPreferences.edit()
                                        logged.putString("upassword",mpassword)
                                        logged.putString("uname",mname)
                                        logged.putString("unickname",mnickname)
                                        logged.putString("uimg",mimg)
                                        // 변경 사항을 커밋하여 저장
                                        logged.apply()

                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
                                        Toast.makeText(this@ModifyActivity,"수정 완료!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@ModifyActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }

                                } else {
                                    // 서버로부터 에러 응답을 받았을 때 처리
                                    Log.d("lys", "응답x")
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
                        val mimg = downloadUrl

                        // TODO: 필요한 대로 downloadUrl을 사용합니다.
                        //서버로 값 전송

                        val retrofit = DBConnect2.retrofit

                        val user = User(memail, mpassword, mname, mnickname, mimg, role)
                        val apiService = retrofit.create(ApiService::class.java)

                        val call = apiService.modify(user)
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

                                        Log.d("lys","카메라 사진으로 수정해서 통신까지 완료")
                                        //쉐어드프리퍼런스에 수정된값으로 업로드
                                        val sharedPreferences = getSharedPreferences("logged_user", Context.MODE_PRIVATE)
                                        var logged = sharedPreferences.edit()
                                        logged.putString("upassword",mpassword)
                                        logged.putString("uname",mname)
                                        logged.putString("unickname",mnickname)
                                        logged.putString("uimg",mimg)
                                        // 변경 사항을 커밋하여 저장
                                        logged.apply()

                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
                                        Toast.makeText(this@ModifyActivity,"수정 완료!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@ModifyActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }

                                } else {
                                    // 서버로부터 에러 응답을 받았을 때의 처리
                                    Log.d("lys", "응답x")
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
            }else if (checkImg.equals("none")) {



                        //서버로 값 전송

                        val retrofit = DBConnect2.retrofit

                        val user = User(memail, mpassword, mname, mnickname, existingImage, role)
                        val apiService = retrofit.create(ApiService::class.java)

                        val call = apiService.modify(user)


                        call.enqueue(object : Callback<String> {
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.isSuccessful) {
                                    Log.d("lys", "기존이미지부분")
                                    val check = response.body()
                                    // 성공적으로 응답을 받았을 때의 처리. 입력한 값을 db에 저장하고 나서 ok! 를 리턴함
                                    Log.d("lys", "응답 o, $check")

                                    //ok! 가 리턴되었다는건 정상적으로 저장이 되었다는 뜻이니까
                                    if(check.equals("ok!")){

                                        Log.d("lys", "기존 이미지 그대로 사용하고, 통신완료")
                                        //쉐어드프리퍼런스에 수정된값으로 업로드
                                        val sharedPreferences = getSharedPreferences("logged_user", Context.MODE_PRIVATE)
                                        var logged = sharedPreferences.edit()
                                        logged.putString("upassword",mpassword)
                                        logged.putString("uname",mname)
                                        logged.putString("unickname",mnickname)
                                        logged.putString("uimg",existingImage)
                                        // 변경 사항을 커밋하여 저장
                                        logged.apply()

                                        //정상적으로 동작하면 다른 화면으로 이동하게끔
                                        Toast.makeText(this@ModifyActivity,"수정 완료!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@ModifyActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }

                                } else {
                                    // 서버로부터 에러 응답을 받았을 때의 처리
                                    Log.d("lys", "응답 x")
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Log.d("lys", "응답 x Error occurred: ${t.message}")
                                // 네트워크 오류 등의 실패처리
                            }
                        })

            }

            //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ



        }


    }

    fun getBitmapFromView(view: View): Bitmap? {
        var bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        view.draw(canvas)
        return  bitmap
    }

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