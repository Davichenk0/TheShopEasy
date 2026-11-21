package com.example.shopeasy.ui.main.products

import com.example.shopeasy.data.CartItem

fun Product.toCartItem(prefix: String = ""): CartItem = CartItem(
    productId = if (id != null) {
        if (prefix.isBlank()) id.toString() else "$prefix:$id"
    } else {
        name.orEmpty()
    },
    name = name.orEmpty(),
    price = price ?: 0.0,
    imageUrl = img_url.orEmpty(),
    quantity = 1
)
