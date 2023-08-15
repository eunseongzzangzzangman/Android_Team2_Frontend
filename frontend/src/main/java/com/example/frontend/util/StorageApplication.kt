package com.example.frontend.util

import androidx.multidex.MultiDexApplication
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class StorageApplication: MultiDexApplication() {
    /*dex 코틀린으로 컴파일 할때 추가된 파일 구조*/
    companion object {//클래스 변수를 사용하겠다, 자바로 치면 sratic 비슷함
    //lateinit 형식으로 선언이 되어있어서 선만만 되었고
    //실제 사용할려면 초기화하는 로직이 반드시 필요함
    //oncreate()에서 초기화시킴
    lateinit var storage: FirebaseStorage
    }

    override fun onCreate() {
        super.onCreate()
        storage = Firebase.storage
    }
}