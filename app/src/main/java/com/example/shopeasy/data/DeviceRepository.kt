package com.example.shopeasy.data

import android.content.Context
import com.example.shopeasy.network.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class DeviceRepository(private val context: Context) {
    private val apiService = RetrofitClient.getInstance(context)

    suspend fun login(loginDto: LoginDto) = apiService.login(loginDto)

    suspend fun register(registerDto: RegisterDto) = apiService.register(registerDto)

    suspend fun getDevices(): Response<List<Device>> = apiService.getDevices()

    suspend fun getDeviceById(id: Int): Response<String> = apiService.getDeviceById(id)

    suspend fun createDevice(
        name: RequestBody,
        price: RequestBody,
        description: RequestBody,
        imagen: MultipartBody.Part?,
        imageUrl: RequestBody,
        categoryId: RequestBody,
        brandId: RequestBody
    ): Response<Unit> = apiService.createDevice(name, price, description, imagen, imageUrl, categoryId, brandId)

    suspend fun updateDevice(id: Int, deviceData: String): Response<Unit> = apiService.updateDevice(id, deviceData)

    suspend fun deleteDevice(id: Int): Response<Unit> = apiService.deleteDevice(id)

    suspend fun getBrands(): Response<List<Brand>> = apiService.getBrands()

    suspend fun getCategories(): Response<List<Category>> = apiService.getCategories()
}
