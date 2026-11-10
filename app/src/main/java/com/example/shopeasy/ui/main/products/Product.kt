package com.example.shopeasy.ui.main.products

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Long? = null,
    val name: String? = null,
    val price: Double? = null,
    val img_url: String? = null
)