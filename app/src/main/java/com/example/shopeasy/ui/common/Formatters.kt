package com.example.shopeasy.ui.common

import com.example.shopeasy.data.OrderStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val currencyFormatter: NumberFormat =
    NumberFormat.getCurrencyInstance(Locale("es", "CO"))

fun Double.asCurrency(): String = currencyFormatter.format(this)

fun Long.asOrderDate(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(this))

fun OrderStatus.label(): String = when (this) {
    OrderStatus.PENDING -> "Pendiente"
    OrderStatus.PAID -> "Pagado"
    OrderStatus.FAILED -> "Fallido"
    OrderStatus.CANCELLED -> "Cancelado"
}
