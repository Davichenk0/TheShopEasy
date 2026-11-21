package com.example.shopeasy.data

enum class OrderStatus {
    PENDING,
    PAID,
    FAILED,
    CANCELLED
}

data class PaymentDetails(
    val refPayco: String? = null,
    val transactionId: String? = null,
    val response: String? = null,
    val approvalCode: String? = null,
    val franchise: String? = null,
    val signatureValid: Boolean? = null
)

/** Documento en la colección orders/{orderId}. */
data class Order(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val payment: PaymentDetails? = null
)

val Order.itemCount: Int
    get() = items.sumOf { it.quantity }
