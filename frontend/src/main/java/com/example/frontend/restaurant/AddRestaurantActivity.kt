package com.example.frontend.restaurant

import android.R
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.frontend.databinding.ActivityAddRestaurantBinding
import com.example.frontend.db.DBConnect
import com.example.frontend.dto.City
import com.example.frontend.dto.FoodInfo
import com.example.frontend.main.MainActivity
import com.example.frontend.service.CityService
import com.example.frontend.service.FoodInfoService
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.UUID


class AddRestaurantActivity : AppCompatActivity() {
    //SHA1: 2F:60:49:C4:A4:71:5B:60:ED:7E:42:24:76:7E:DE:D4:5C:2E:E0:87
    lateinit var binding: ActivityAddRestaurantBinding

    //파일 경로를 전역으로 설정해서 갤러리에서 사진을 선택후 해당 파일의 절대 경로를 저장하는 파일
    lateinit var filePath: String
    private val storage = Firebase.storage
    lateinit var foodinfoService : FoodInfoService
    lateinit var cityService : CityService
    lateinit var cityid : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = DBConnect.retrofit

        cityService = retrofit.create(CityService::class.java)

        val call: Call<List<City?>?>? = cityService.getcityList()
        call?.enqueue(object : Callback<List<City?>?> {
            override fun onResponse(call: Call<List<City?>?>, response: Response<List<City?>?>) {
                if (response.isSuccessful) {
                    val cityList: List<City?>? = response.body()
                    // 도시 이름 목록 추출
                    val cityNames = cityList?.mapNotNull { it?.ccity } ?: emptyList()

                    val spinner = binding.cityid

                    // 어댑터 생성 및 데이터 설정
                    val adapter = ArrayAdapter(this@AddRestaurantActivity, android.R.layout.simple_spinner_item, cityNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    // 스피너에 어댑터 설정
                    spinner.adapter = adapter
                    // 처리할 작업: cityList를 활용하여 스피너에 표시 등
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                           Log.d("joj","select box 선택")
                           Log.d("joj",(position+1).toString())
                            cityid = (position+1).toString()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // 아무것도 선택되지 않았을 때 처리
                        }
                    }
                } else {
                    // API 호출은 성공했지만 서버에서 오류 응답을 보낸 경우 처리
                    val errorBody = response.errorBody()?.string()
                    // 오류 응답 처리: errorBody를 활용하여 오류 메시지 등을 처리
                }
            }

            override fun onFailure(call: Call<List<City?>?>, t: Throwable) {
                // 네트워크 연결 실패 등 오류 처리
            }
        })

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
        Log.d("lys","uimg : $role")




        binding.setimageBtn.setOnClickListener {
            /*Intent.ACTION_PICK -> 갤러리 사진 선택으로 이동*/
            val intent = Intent(Intent.ACTION_PICK)
            //인텐트 옵션에서 액션 문자열은, 이미지를 선택후, URI를 가져오는
            //데이터 타입, MIME TYPE, 모든 이미지
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            /*인텐트의 후처리를 호출하는 함수이고 위의 정의한 변수로 이동
            * */
            requestLauncher.launch(intent)
        }
        binding.addSave.setOnClickListener {
            if (binding.addImageView.drawable !== null) {
                //store 에 먼저 데이터를 저장후 document id 값으로 업로드 파일 이름 지정
                Log.d("joj", "@@@@@@@@@@@@@")

                val uuid = UUID.randomUUID()
                uploadImage(uuid.toString())
            } else {
                Toast.makeText(this, "데이터가 모두 입력되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }


        }

    }

    /*인텐트를 이용해서 후처리를 하는 코드ActivityResultContracts.StartActivityForResult())
    *
    * 사진 선택후 돌아왔을때 후처리하는 코드*/
    val requestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    {
        /*it 해당 정보를 담는 객체
        * 안드로이드 버전의 ok ,http status 200과 동일*/
        if (it.resultCode === android.app.Activity.RESULT_OK) {
            //가져온 이미지를 처리를 글라이드를 이용
            Glide
                .with(getApplicationContext())
                //선택한 이미지를 불러오는 역할
                .load(it.data?.data)
                //출력 사진의 크기
                .apply(RequestOptions().override(250, 200))
                //사진의 크기를 조정해준다
                .centerCrop()
                //불러온 이미지를 결과뷰에 출력
                .into(binding.addImageView)

            /*커서 부분은 해당 이미지의 URI 경로로 위치를 파악하는 구문
            * 이미지의 위치가 있는 URI 주소,
            * MediaStore.Images.Media.DATA 이미지의 정보*/
            val cursor = contentResolver.query(
                it.data?.data as Uri,
                arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null
            );
            cursor?.moveToFirst().let {
                // filePath=cursor?.getString(0) as String 경로 주소
                //log로 찍어서 확인 가능
                filePath = cursor?.getString(0) as String
            }
        }
    }

    private fun uploadImage(docId: String) {
        //add............................
        val storageRef: StorageReference = storage.reference
        val imgRef: StorageReference = storageRef.child("restaurant/$docId.jpg")
        val bitmap = getBitmapFromView(binding.addImageView)
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = imgRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imgRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val imageUrl = downloadUrl.toString()
                Toast.makeText(this, "save ok..", Toast.LENGTH_SHORT).show()
                Log.d("joj", "Image URL: $imageUrl")
                val retrofit = DBConnect.retrofit
                val rtitle = binding.rtitle.text
                val rcity = binding.rcity.text
                val rlat = binding.rlat.text
                val rlng = binding.rlng.text
                val rtel = binding.rtel.text
                val rmainimg = imageUrl
                val rinfo = binding.rinfo.text
                Log.d("joj", "저장되는 이미지경로${rmainimg}")
                foodinfoService = retrofit.create(FoodInfoService::class.java)
                val foodInfo = FoodInfo(
                    null,
                    rtitle.toString(),
                    rcity.toString(),
                    rlat.toString(),
                    rlng.toString(),
                    rtel.toString(),
                    rmainimg,
                    rinfo.toString(),
                    "0",
                    "0",
                    "0",
                    cityid
                )
                Log.d("joj", foodInfo.rtitle.toString())
                Log.d("joj", foodInfo.rcity.toString())
                Log.d("joj", foodInfo.rlat.toString())
                Log.d("joj", foodInfo.rlng.toString())
                Log.d("joj", foodInfo.rtel.toString())
                Log.d("joj", foodInfo.rmainimg.toString())
                Log.d("joj", foodInfo.rinfo.toString())
                Log.d("joj", foodInfo.cid.toString())
                val call = foodinfoService.postFoodInfo(foodInfo)
                call.enqueue(object : Callback<FoodInfo> {
                    override fun onResponse(call: Call<FoodInfo>, response: Response<FoodInfo>) {
                        if (response.isSuccessful) {
                            // 서버로부터 응답이 성공적으로 돌아온 경우 처리할 내용
                            // 예: Toast 메시지 표시 등
                        } else {
                            // 서버로부터 응답이 실패한 경우 처리할 내용
                            // 예: 에러 메시지 표시 등
                        }
                    }

                    override fun onFailure(call: Call<FoodInfo>, t: Throwable) {
                        // 통신에 실패한 경우 처리할 내용
                        // 예: 에러 메시지 표시 등
                    }
                })
                val refreshIntent = Intent(this, MainActivity::class.java)
                startActivity(refreshIntent)
            }
        }
            .addOnFailureListener {
                Log.d("joj", "file save error", it)
            }

    }

    fun getBitmapFromView(view: View): Bitmap? {
        var bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}