package com.example.shopeasy.data

import android.content.Context
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

object OrderRepository {

    private const val COLLECTION = "orders"
    private const val LOCAL_KEY = "orders"

    private lateinit var local: LocalStore

    fun initialize(context: Context) {
        local = LocalStore(context.applicationContext)
    }

    suspend fun createOrder(
        items: List<CartItem>,
        customerName: String,
        customerEmail: String
    ): Order {
        require(items.isNotEmpty()) { "El carrito está vacío." }

        val now = System.currentTimeMillis()
        val order = Order(
            id = UUID.randomUUID().toString().replace("-", ""),
            userId = Session.userId(),
            customerName = customerName,
            customerEmail = customerEmail,
            items = items,
            total = items.sumOf { it.price * it.quantity },
            createdAt = now,
            updatedAt = now
        )
        save(order)
        return order
    }

    suspend fun getOrders(): List<Order> = withContext(Dispatchers.IO) {
        if (FirebaseProvider.isConfigured) {
            // Se ordena en memoria para no exigir un índice compuesto en Firestore.
            FirebaseProvider.db.collection(COLLECTION)
                .whereEqualTo("userId", Session.userId())
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Order::class.java) }
                .sortedByDescending { it.createdAt }
        } else {
            readLocalOrders()
                .filter { it.userId == Session.userId() }
                .sortedByDescending { it.createdAt }
        }
    }

    suspend fun getOrder(orderId: String): Order? = withContext(Dispatchers.IO) {
        if (FirebaseProvider.isConfigured) {
            FirebaseProvider.db.collection(COLLECTION)
                .document(orderId)
                .get()
                .await()
                .toObject(Order::class.java)
        } else {
            readLocalOrders().firstOrNull { it.id == orderId }
        }
    }

    suspend fun updateStatus(
        orderId: String,
        status: OrderStatus,
        payment: PaymentDetails? = null
    ): Order? {
        val order = getOrder(orderId) ?: return null
        val updated = order.copy(
            status = status,
            payment = payment ?: order.payment,
            updatedAt = System.currentTimeMillis()
        )
        save(updated)
        return updated
    }

    suspend fun deleteOrder(orderId: String) = withContext(Dispatchers.IO) {
        if (FirebaseProvider.isConfigured) {
            FirebaseProvider.db.collection(COLLECTION).document(orderId).delete().await()
        } else {
            local.write(LOCAL_KEY, readLocalOrders().filterNot { it.id == orderId })
        }
    }

    private suspend fun save(order: Order) = withContext(Dispatchers.IO) {
        if (FirebaseProvider.isConfigured) {
            FirebaseProvider.db.collection(COLLECTION)
                .document(order.id)
                .set(order)
                .await()
        } else {
            val orders = readLocalOrders().filterNot { it.id == order.id } + order
            local.write(LOCAL_KEY, orders)
        }
    }

    private fun readLocalOrders(): List<Order> =
        local.read<List<Order>>(LOCAL_KEY, object : TypeToken<List<Order>>() {}.type)
            ?: emptyList()
}
