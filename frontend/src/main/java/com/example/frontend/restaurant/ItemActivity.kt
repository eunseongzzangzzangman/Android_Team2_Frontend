package com.example.frontend.restaurant

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity

import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.frontend.R
import com.example.frontend.databinding.ActivityItemBinding
import com.example.frontend.db.DBConnect
import com.example.frontend.db.DBConnect2
import com.example.frontend.dto.Comment
import com.example.frontend.dto.CommentWithRating
import com.example.frontend.dto.FoodInfo
import com.example.frontend.main.MainActivity
import com.example.frontend.member.LoginActivity
import com.example.frontend.member.SignupActivity
import com.example.frontend.recycler.MyAdapter2
import com.example.frontend.recycler.ReviewMyAdapter2
import com.example.frontend.service.ApiService
import com.example.frontend.service.FoodInfoService
import com.example.frontend.service.ReviewService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ItemActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    lateinit var toggle: ActionBarDrawerToggle  // 메뉴
    lateinit var binding: ActivityItemBinding
    private lateinit var formattedTime: String
    private val comments = mutableListOf<CommentWithRating>()
    lateinit var foodinfoService: FoodInfoService
    lateinit var reviewService: ReviewService
    lateinit var filePath: String
    private val storage = Firebase.storage
    lateinit var  uid : String
    lateinit var  unickname : String
    lateinit var  id : String
    lateinit var  rid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityItemBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.homeButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        
        //============================user정보=========================
        // SharedPreferences 객체생성=================저장된 값을 가져오기 위해=====================================
        val sharedPreferences = getSharedPreferences("logged_user", Context.MODE_PRIVATE)
        uid = sharedPreferences.getString("uid", null).toString()
        val uemail = sharedPreferences.getString("uemail", null)
        val upassword = sharedPreferences.getString("upassword", null)
        val uname = sharedPreferences.getString("uname", null)
        unickname = sharedPreferences.getString("unickname", null).toString()
        val uimg = sharedPreferences.getString("uimg", null)
        val role = sharedPreferences.getString("role", null)
            
        val Role = role.toString()

        binding.mod.isVisible = false
        binding.del.isVisible = false



        if(Role.equals("ADMIN")){
            binding.mod.isVisible = true
            binding.del.isVisible = true

        }
        //user가 있을경우 없을경우
        if(unickname !=null){
            binding.commentUserIdTextView.text = unickname.toString()
        }else{
            binding.commentUserIdTextView.text="비회원"
        }
        //==================댓글 리사이클러뷰==================================
        recycle()




        //갤러리 요청 런처mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
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
                    binding.reviewImageView.setImageBitmap(bitmap)
                } ?: let{
                    Log.d("kkang", "bitmap null")
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm


        //====================토글 메뉴==========================
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
            if(it.itemId == R.id.joinmenu){
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
            }else if(it.itemId == R.id.login){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            true
        }
        //====================토글 메뉴==========================
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // 값을 받기 위해 Intent 가져오기
        val intent = intent

        // 전달된 값 가져오기
        val rid = intent.getStringExtra("rid")
        val rtitle = intent.getStringExtra("rtitle")
        val rcity = intent.getStringExtra("rcity")
        val rtel = intent.getStringExtra("rtel")
        val rinfo = intent.getStringExtra("rinfo")
        binding.bigtitle.text = rtitle
        binding.rid.text = rid
        binding.rtitle.text = rtitle
        binding.rcity.text = rcity
        binding.rtel.text = rtel
        binding.rinfo.text = rinfo
        val img = intent.getStringExtra("rmainimg")
        //=========================별점 가지고 오기================
        val retrofit = DBConnect.retrofit

        foodinfoService = retrofit.create(FoodInfoService::class.java)

        val call: Call<FoodInfo?>? = foodinfoService.getFoodone(rid.toString())
        Log.d("joj",call.toString())
        call?.enqueue(object : Callback<FoodInfo?> {
            override fun onResponse(call: Call<FoodInfo?>, response: Response<FoodInfo?>) {
                if (response.isSuccessful) {
                    val foodInfo = response.body()
                    updateStarRatingImage(foodInfo?.rstaravg)
                    // foodInfo를 사용하여 필요한 작업 수행
                    if (foodInfo != null) {
                        // 예시: 응답 데이터의 이름을 로그로 출력

                        Log.d("joj", "Food Name: ${foodInfo.rtitle}")
                    } else {
                        Log.d("joj", "No data received")
                    }
                } else {
                    // 응답이 성공하지 않은 경우
                    Log.d("joj", "Response not successful")
                }
            }

            override fun onFailure(call: Call<FoodInfo?>, t: Throwable) {
                // 호출 실패 시 처리합니다.
            }
        })

// 자기글 하나 들고 오기

        reviewService = retrofit.create(ReviewService::class.java)

        val call1: Call<Comment?>? = reviewService.getReviewOne(uid,rid.toString())
        call1?.enqueue(object : Callback<Comment?> {
            override fun onResponse(call1: Call<Comment?>, response: Response<Comment?>) {
                if (response.isSuccessful) {
                    val revieOne = response.body()
                    // foodInfo를 사용하여 필요한 작업 수행
                    if (revieOne != null) {
                        // 예시: 응답 데이터의 이름을 로그로 출력
                        id = revieOne.id.toString()
                        binding.modcontent.text = revieOne.cmt
                        binding.moddate.text = revieOne.timestamp
                        val urlImg = revieOne?.reviewimg
                        binding.modnickname.text = revieOne.nickname
                        val starpoint = revieOne.starpoint.toDouble()
                        Log.d("joj","별점")
                        Log.d("joj",starpoint.toString())
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
                        Glide.with(this@ItemActivity)
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
                        Log.d("joj", "Food Name: ${revieOne.cmt}")
                    } else {
                        Log.d("joj", "No data received")
                    }
                } else {
                    // 응답이 성공하지 않은 경우
                    Log.d("joj", "Response not successful")
                }
            }

            override fun onFailure(call1: Call<Comment?>, t: Throwable) {
                // 호출 실패 시 처리합니다.
            }
        })





        Log.d("joj",img.toString())
        Glide.with(this)
            .asBitmap()
            .load(img)
            .into(object : CustomTarget<Bitmap>(200, 200) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    binding.imgpath.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }
            })
//============================사진 불러오기 버튼=========================
        binding.setimageBtn.setOnClickListener {


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
                binding.reviewImageView.setImageBitmap(bitmap)
            }


        }
        //==========================리뷰=====================
        binding.sendButton.setOnClickListener {

            //갤러리에서 가져온 이미지를 파이어베이스에 저장하기 전에 선언해야할부분mmmmmmmmmmmmmmmmmmmmmmmmmm
            val storageRef: StorageReference = storage.reference
            //파이어베이스 스토리지의 profile_images 라는 패키지 안에  reviewimg.jpg 라는 이미지로 바꿔야된다.
            val imgRef: StorageReference = storageRef.child("profile_images/reviewimg.jpg")

            val bitmap = getBitmapFromView(binding.reviewImageView)
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

            val cmt = binding.commentInputEditText.text.toString()

            //별점 값
            val rating = binding.ratingBar.rating.toInt()



            if (rating == 0) {
                Toast.makeText(this@ItemActivity, "별점 필수!!", Toast.LENGTH_SHORT).show()
                //별점을 매기지 않은 경우 처리
                // (예: 에러 메시지 표시 또는 별점 필수 입력)
            } else {
                //댓글과 별점을 결합하여 저장
                val currentTime = System.currentTimeMillis()
                val dataFormat = SimpleDateFormat("(yyyy-MM-dd, HH:mm)", Locale.getDefault())
                formattedTime = dataFormat.format(Date(currentTime))
                comments.add(CommentWithRating(cmt, rating, currentTime, uid))

                if (data.isEmpty()) {
                    //이미지가 없는 경우
//                    val noImageMessage = "이미지 없음"
//                    comments.add(CommentWithRating(cmt, rating, currentTime, uid, noImageMessage))
//                    binding.reviewImageView.setImageResource(R.drawable.noimg)
                    //이미지가 있는 경우
                    //파이어베이스에 사진을 올리고, 그 사진의 url을 따온다음? 다른데이터와 함께 스프링부트에 전송mmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
                    val storageRef: StorageReference = storage.reference
                    val imgRef: StorageReference = storageRef.child("review/noimg.jpg")

                    val drawableId = R.drawable.noimg // drawable 폴더에 있는 이미지의 리소스 ID
                    val drawable = resources.getDrawable(drawableId, null)
                    val bitmap = (drawable as BitmapDrawable).bitmap

                    // 이미지를 스트림으로 변환
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val byteArray = stream.toByteArray()

                    // 이미지 업로드
                    val uploadTask = imgRef.putBytes(byteArray)
                    uploadTask.addOnSuccessListener { _ ->
                        Log.d("lsy", "이미지 업로드 성공")
                        // 이미지 업로드 후 다운로드 URL 가져오기
                        imgRef.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            Log.d("lsy", "Download URL: $downloadUrl")
                            val reviewimg = downloadUrl
                            val rrid = intent.getStringExtra("rid").toString()
                            // TODO: 필요한 대로 downloadUrl을 사용합니다.
                            //Retrofit 인스턴스 생성, 서버로 값 전송
                            val retrofit = DBConnect2.retrofit
                            val comment = Comment(
                                null,
                                cmt,
                                formattedTime,
                                reviewimg,
                                unickname,
                                rating.toString(),
                                uid,
                                rrid
                            ) //Comment 클래스는 댓글 데이터 모델을 나타냄
                            reviewService = retrofit.create(ReviewService::class.java)

                            //댓글, 시간, 등록한 이미지의 url이 comment에 담겨있는데,
                            //ApiService 안에 선언한 함수 @POST("comments") postComment 이것을 통해서 스프링부트에다가 POST하는것
                            val call = reviewService.postComment(comment)

                            //이 부분은 POST한 이후 응답을 처리하는 부분
                            call.enqueue(object : Callback<String> {
                                override fun onResponse(
                                    call: Call<String>,
                                    response: Response<String>
                                ) {
                                    //요청이 성공적으로 처리되었을 때 실행되는 코드
                                    //댓글 등록 후, 등록한 시간을 commentTimeTextView에 업데이트
                                    // binding.commentTimeTextView.text = formattedTimeㅁ

                                    Log.d("joj", "진입 여부")
                                    //댓글 입력 필드 및 별점 초기화
                                    binding.commentInputEditText.text.clear()
                                    binding.ratingBar.rating = 0.0f
                                    Log.d("joj", "디비 저장 후")
                                    recycle()
                                    Log.d("joj", "디비 저장 후1111")
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    //요청이 실패했을 때 실행되는 코드
                                    Log.d("joj", "데이터 입력 실패")
                                }
                            })
                        }
                    }
                } else {
                    //이미지가 있는 경우
                    //파이어베이스에 사진을 올리고, 그 사진의 url을 따온다음? 다른데이터와 함께 스프링부트에 전송mmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
                    var uploadTask = imgRef.putBytes(data)
                    uploadTask.addOnSuccessListener { _ ->
                        Log.d("lsy", "이미지 업로드 성공")
                        // 이미지 업로드 후 다운로드 URL 가져오기
                        imgRef.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            Log.d("lsy", "Download URL: $downloadUrl")
                            val reviewimg = downloadUrl
                            val rrid = intent.getStringExtra("rid").toString()
                            // TODO: 필요한 대로 downloadUrl을 사용합니다.
                            //Retrofit 인스턴스 생성, 서버로 값 전송
                            val retrofit = DBConnect2.retrofit
                            val comment = Comment(null,cmt, formattedTime, reviewimg,unickname,rating.toString(), uid, rrid) //Comment 클래스는 댓글 데이터 모델을 나타냄
                            reviewService = retrofit.create(ReviewService::class.java)

                            //댓글, 시간, 등록한 이미지의 url이 comment에 담겨있는데,
                            //ApiService 안에 선언한 함수 @POST("comments") postComment 이것을 통해서 스프링부트에다가 POST하는것
                            val call = reviewService.postComment(comment)

                            //이 부분은 POST한 이후 응답을 처리하는 부분
                            call.enqueue(object : Callback<String> {
                                override fun onResponse(call: Call<String>,response: Response<String>) {
                                    //요청이 성공적으로 처리되었을 때 실행되는 코드
                                    //댓글 등록 후, 등록한 시간을 commentTimeTextView에 업데이트
                                   // binding.commentTimeTextView.text = formattedTimeㅁ

                                    Log.d("joj","진입 여부")
                                    //댓글 입력 필드 및 별점 초기화
                                    binding.commentInputEditText.text.clear()
                                    binding.ratingBar.rating = 0.0f
                                    Log.d("joj","디비 저장 후")
                                    recycle()
                                    Log.d("joj","디비 저장 후1111")
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    //요청이 실패했을 때 실행되는 코드
                                    Log.d("joj","데이터 입력 실패")
                                }
                            })

                        }
                    }
                }

                //댓글 작성 후 달린 댓글 및 평균 별점 표시
                updateCommentTextViewAndRating()

                //댓글 입력 필드 및 별점 초기화
                binding.commentInputEditText.text.clear()
                binding.ratingBar.rating = 0.0f


                //댓글 등록 후 키패드 닫기
                hideKeyboard()

                //mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm



            }
        }

        //=============================음식점 수정 버튼==========================================\
        binding.mod.setOnClickListener {
            val modintent = Intent(this@ItemActivity, RestModActivity::class.java)
            Log.d("jojj1", intent.getStringExtra("rid").toString())
            Log.d("jojj1", intent.getStringExtra("rtitle").toString())
            Log.d("jojj1", intent.getStringExtra("rcity").toString())
            Log.d("jojj1", intent.getStringExtra("rlat").toString())
            Log.d("jojj1", intent.getStringExtra("rlng").toString())
            Log.d("jojj1", intent.getStringExtra("rtel").toString())
            Log.d("jojj1", intent.getStringExtra("rinfo").toString())
            Log.d("jojj1", intent.getStringExtra("rmainimg").toString())
            Log.d("jojj1", intent.getStringExtra("rtotalstar").toString())
            Log.d("jojj1", intent.getStringExtra("rstaravg").toString())
            Log.d("jojj1", intent.getStringExtra("rcount").toString())
            Log.d("jojj1", intent.getStringExtra("cid").toString())
            modintent.putExtra("rid",intent.getStringExtra("rid"))
            modintent.putExtra("rtitle",intent.getStringExtra("rtitle"))
            modintent.putExtra("rcity",intent.getStringExtra("rcity"))
            modintent.putExtra("rlat",intent.getStringExtra("rlat"))
            modintent.putExtra("rlng",intent.getStringExtra("rlng"))
            modintent.putExtra("rtel",intent.getStringExtra("rtel"))
            modintent.putExtra("rinfo",intent.getStringExtra("rinfo"))
            modintent.putExtra("rmainimg",intent.getStringExtra("rmainimg"))
            modintent.putExtra("rtotalstar",intent.getStringExtra("rtotalstar"))
            modintent.putExtra("rstaravg",intent.getStringExtra("rstaravg"))
            modintent.putExtra("rcount",intent.getStringExtra("rcount"))
            modintent.putExtra("cid",intent.getStringExtra("cid"))
            startActivity(modintent)
        }

        //===================================음식점 삭제 버튼===============================
        binding.del.setOnClickListener {
                val imageUrl = intent.getStringExtra("rmainimg").toString()
                Log.d("joj", "삭제-> 이미지 경로")
                Log.d("joj", imageUrl)
                val storage = FirebaseStorage.getInstance()
                val storageRef: StorageReference = storage.getReferenceFromUrl(imageUrl)
                storageRef.delete()
                    .addOnSuccessListener {
                        Log.d("joj", "삭제-> 이미지 삭제 성공")
                    }
                    .addOnFailureListener {
                        // 이미지 삭제 실패한 경우 처리할 내용
                        // 예: 에러 메시지 표시 등
                        Log.d("joj", "이미지 삭제 실패", it)
                    }
                Log.d("joj","삭제 확인 버튼 클릭")
                val retrofit = DBConnect.retrofit
                val rid = intent.getStringExtra("rid").toString()
                Log.d("joj",rid)
                foodinfoService = retrofit.create(FoodInfoService::class.java)
                val foodInfo = FoodInfo(
                    rid,null,null,null,null,null,null,null,null,null,null,null
                )
                val call = foodinfoService.postFoodInfodelete(foodInfo)
                call.enqueue(object : Callback<FoodInfo> {
                    override fun onResponse(call: Call<FoodInfo>, response: Response<FoodInfo>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            val message = responseBody?.toString() // 메시지 받아오기
                            Log.d("joj", "서버 응답 메시지: $message")

                        } else {
                            Log.d("joj","서버로부터 응답이 실패")
                        }
                        val refreshIntent = Intent(this@ItemActivity, MainActivity::class.java)
                        startActivity(refreshIntent)
                    }

                    override fun onFailure(call: Call<FoodInfo>, t: Throwable) {
                        // 통신에 실패한 경우 처리할 내용
                        // 예: 에러 메시지 표시 등
                        Log.d("joj","통신에 실패: ${t.message}")
                    }
                })
            val refreshIntent = Intent(this@ItemActivity, MainActivity::class.java)
            startActivity(refreshIntent)
            }
        //===================================리뷰 수정 버튼===============================
        binding.reviewmod.setOnClickListener {
            reviewService = retrofit.create(ReviewService::class.java)

            val call1: Call<Comment?>? = reviewService.getReviewOne(uid,rid.toString())
            call1?.enqueue(object : Callback<Comment?> {
                override fun onResponse(call1: Call<Comment?>, response: Response<Comment?>) {
                    if (response.isSuccessful) {
                        val revieOne = response.body()
                        // foodInfo를 사용하여 필요한 작업 수행
                        if (revieOne != null) {
                            // 예시: 응답 데이터의 이름을 로그로 출력
                            val reviewintnet = Intent(this@ItemActivity, ModReviewActivity::class.java)
                            reviewintnet.putExtra("id",id)
                            reviewintnet.putExtra("cmt",revieOne.cmt)
                            reviewintnet.putExtra("cmtTime",revieOne.timestamp)
                            reviewintnet.putExtra("reviewimg",revieOne?.reviewimg)
                            reviewintnet.putExtra("nickname",revieOne.nickname)
                            reviewintnet.putExtra("starpoint",revieOne.starpoint)
                            reviewintnet.putExtra("uid",uid)
                            reviewintnet.putExtra("rid",intent.getStringExtra("rid"))
                            startActivity(reviewintnet)

                        } else {
                            Log.d("joj", "No data received")
                        }
                    } else {
                        // 응답이 성공하지 않은 경우
                        Log.d("joj", "Response not successful")
                    }
                }

                override fun onFailure(call1: Call<Comment?>, t: Throwable) {
                    // 호출 실패 시 처리합니다.
                }
            })



        }



    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        val intent = intent
        val LAT = intent.getStringExtra("rlat")?.toDouble() // String을 Double로 변환
        val LNG = intent.getStringExtra("rlng")?.toDouble()

        if (LAT != null && LNG != null) {
            Log.d("joj", LAT.toString())
            Log.d("joj", LNG.toString())
            mMap = googleMap

            // Add a marker at the specified location and move the camera
            val location = LatLng(LAT, LNG)
            mMap.addMarker(MarkerOptions().position(location).title("Marker"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        } else {
            Log.d("joj", "LAT or LNG is null.")
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.commentInputEditText.windowToken, 0)
    }


    private fun updateCommentTextViewAndRating() {

        var combinedComments = ""
        for(comment in comments) {
            if(comment.message == "이미지 없음")  {
                combinedComments += "별점 : ${comment.rating} \n 이미지 없음 ${comment.comment} ${formattedTime}\n"
            } else {
                combinedComments += "별점 : ${comment.rating} \n ${comment.comment} ${formattedTime}\n"
            }
        }

//        combinedComments = comments.joinToString("\n") {
//            "${it.comment} (별점: ${it.rating}, 시간: $formattedTime)"
//        }
//        binding.commentTextView.text = "\n$combinedComments"

//        val averageRating = calculateAverageRating()
//        binding.ratingAverageTextView.text = "평균 별점: %.2f점".format(averageRating)
    }

    private fun calculateAverageRating() : Double {
        if (comments.isEmpty()) {
            return 0.0
        }

        val totalRating = comments.sumBy { it.rating }
        return totalRating.toDouble() / comments.size
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

    fun getBitmapFromView(view: View): Bitmap? {
        var bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        view.draw(canvas)
        return  bitmap
    }
    fun recycle(){
        //==================댓글 리사이클러뷰==================================
        val retrofit = DBConnect.retrofit

        reviewService = retrofit.create(ReviewService::class.java)
        val reviewrid = intent.getStringExtra("rid").toString()
        val call1: Call<List<Comment?>?>? = reviewService.getReviewList(uid,reviewrid)
        Log.d("joj","리사이클러뷰 ")
        call1?.enqueue(object : Callback<List<Comment?>?> {
            override fun onResponse(call: Call<List<Comment?>?>, response: Response<List<Comment?>?>) {
                if (response.isSuccessful) {
                    val reviews = response.body()
                    // 여기서 받아온 데이터(items)를 처리합니다.
                    val adapter = ReviewMyAdapter2(this@ItemActivity, reviews)

                    binding.recyclerView.adapter = adapter

                    binding.recyclerView.addItemDecoration(
                        DividerItemDecoration(
                            this@ItemActivity,
                            LinearLayoutManager.VERTICAL
                        )
                    )
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<Comment?>?>, t: Throwable) {
                // 호출 실패 시 처리합니다.
            }
        })
    }
    // 별점 이미지를 업데이트하는 메서드
    private fun updateStarRatingImage(rstaravg: String?) {
        val starpoint = rstaravg?.toDouble()
        if (starpoint != null) {
            // 별점 이미지 설정 작업
            if (starpoint == 5.0) {
                binding.starimg.setImageResource(R.drawable.five)
            } else if (starpoint < 5 && starpoint >= 4.5) {
                binding.starimg.setImageResource(R.drawable.four_half)
            } else if (starpoint < 4.5 && starpoint >= 4) {
                binding.starimg.setImageResource(R.drawable.four)
            } else if (starpoint < 4 && starpoint >= 3.5) {
                binding.starimg.setImageResource(R.drawable.three_half)
            } else if (starpoint < 3.5 && starpoint >= 3) {
                binding.starimg.setImageResource(R.drawable.three)
            } else if (starpoint < 3 && starpoint >= 2.5) {
                binding.starimg.setImageResource(R.drawable.two_half)
            } else if (starpoint < 2.5 && starpoint >= 2) {
                binding.starimg.setImageResource(R.drawable.two)
            } else if (starpoint < 2 && starpoint >= 1.5) {
                binding.starimg.setImageResource(R.drawable.one_half)
            } else if (starpoint < 1.5 && starpoint >= 1) {
                binding.starimg.setImageResource(R.drawable.one)
            } else if (starpoint < 1 && starpoint >= 0.5) {
                binding.starimg.setImageResource(R.drawable.half)
            } else {
                binding.starimg.setImageResource(R.drawable.zro)
            }
        }
    }

}