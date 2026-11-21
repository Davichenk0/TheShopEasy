package com.example.shopeasy.data

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Device(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("imageURL") val imageURL: String?,
    @SerializedName("description") val description: String? = null
)

data class Brand(
    @SerializedName("brandId") val brandId: UUID?,
    @SerializedName("name") val name: String?
)

data class Category(
    @SerializedName("categoryId") val categoryId: UUID?,
    @SerializedName("name") val name: String?
)

data class LoginDto(
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?
)

data class RegisterDto(
    @SerializedName("email") val email: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("password") val password: String?
)

data class LoginResponse(
    @SerializedName("token") val token: String?
)

