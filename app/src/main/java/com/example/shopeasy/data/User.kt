package com.example.shopeasy.data

data class User(
    val id: Int,
    var name: String,
    var email: String,
    var role: String,
    var imageRes: Int = 0
)