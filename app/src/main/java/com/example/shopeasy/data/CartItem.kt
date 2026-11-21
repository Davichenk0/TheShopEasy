package com.example.shopeasy.data

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1
)

/** Documento raíz del carrito en Firestore: carts/{userId}. */
data class CartDocument(
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val updatedAt: Long = 0L
)

val CartItem.subtotal: Double
    get() = price * quantity
