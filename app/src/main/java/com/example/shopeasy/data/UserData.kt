package com.example.shopeasy.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val id: String,
    @SerialName("names") val names: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("mail") val mail: String? = null,
    val rol: String = "customer",
    @SerialName("img_url") val imgUrl: String? = null
)
