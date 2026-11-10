package com.example.shopeasy.network

import com.example.shopeasy.data.Device
import com.example.shopeasy.data.Brand
import com.example.shopeasy.data.Category
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("Auth/login")
    suspend fun login(@Body loginDto: com.example.shopeasy.data.LoginDto): Response<com.example.shopeasy.data.LoginResponse>

    @POST("Auth/register")
    suspend fun register(@Body registerDto: com.example.shopeasy.data.RegisterDto): Response<String>

    // Devices

    @GET("Device")
    suspend fun getDevices(): Response<List<Device>>

    @GET("Device/{id}")
    suspend fun getDeviceById(@Path("id") id: Int): Response<String>

    @Multipart
    @POST("Device")
    suspend fun createDevice(
        @Part("Name") name: RequestBody,
        @Part("Price") price: RequestBody,
        @Part("Description") description: RequestBody,
        @Part imagen: MultipartBody.Part?,
        @Part("ImageURL") imageUrl: RequestBody,
        @Part("CategoryId") categoryId: RequestBody,
        @Part("BrandId") brandId: RequestBody
    ): Response<Unit>

    @PUT("Device/{id}")
    suspend fun updateDevice(
        @Path("id") id: Int,
        @Body deviceData: String
    ): Response<Unit>

    @DELETE("Device/{id}")
    suspend fun deleteDevice(@Path("id") id: Int): Response<Unit>

    // Brands
    @GET("Brand")
    suspend fun getBrands(): Response<List<Brand>>

    // Categories
    @GET("Category")

    suspend fun getCategories(): Response<List<Category>>
}
