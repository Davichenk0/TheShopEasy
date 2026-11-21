package com.example.shopeasy

import android.app.Application
import com.example.shopeasy.data.CartRepository
import com.example.shopeasy.data.FirebaseProvider
import com.example.shopeasy.data.OrderRepository

class ShopEasyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseProvider.initialize(this)
        CartRepository.initialize(this)
        OrderRepository.initialize(this)
    }
}
