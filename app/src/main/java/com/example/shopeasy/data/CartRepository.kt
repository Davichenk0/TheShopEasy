package com.example.shopeasy.data

import android.content.Context
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object CartRepository {

    private const val COLLECTION = "carts"

    private lateinit var local: LocalStore

    fun initialize(context: Context) {
        local = LocalStore(context.applicationContext)
    }

    suspend fun getCart(): List<CartItem> = withContext(Dispatchers.IO) {
        readCart(Session.userId())
    }

    suspend fun addItem(item: CartItem) {
        require(item.quantity > 0) { "La cantidad debe ser mayor que cero." }
        mutate { items ->
            if (items.any { it.productId == item.productId }) {
                items.map {
                    if (it.productId == item.productId) {
                        it.copy(quantity = it.quantity + item.quantity)
                    } else {
                        it
                    }
                }
            } else {
                items + item
            }
        }
    }

    suspend fun updateQuantity(productId: String, quantity: Int) {
        mutate { items ->
            if (quantity <= 0) {
                items.filterNot { it.productId == productId }
            } else {
                items.map {
                    if (it.productId == productId) it.copy(quantity = quantity) else it
                }
            }
        }
    }

    suspend fun removeItem(productId: String) {
        mutate { items -> items.filterNot { it.productId == productId } }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        val userId = Session.userId()
        if (FirebaseProvider.isConfigured) {
            FirebaseProvider.db.collection(COLLECTION).document(userId).delete().await()
        } else {
            local.remove(cartKey(userId))
        }
    }

    private suspend fun mutate(transform: (List<CartItem>) -> List<CartItem>) {
        withContext(Dispatchers.IO) {
            val userId = Session.userId()
            val items = transform(readCart(userId))

            if (FirebaseProvider.isConfigured) {
                FirebaseProvider.db.collection(COLLECTION)
                    .document(userId)
                    .set(CartDocument(userId, items, System.currentTimeMillis()))
                    .await()
            } else {
                local.write(cartKey(userId), items)
            }
        }
    }

    private suspend fun readCart(userId: String): List<CartItem> {
        return if (FirebaseProvider.isConfigured) {
            val document = FirebaseProvider.db.collection(COLLECTION)
                .document(userId)
                .get()
                .await()
            document.toObject(CartDocument::class.java)?.items ?: emptyList()
        } else {
            local.read<List<CartItem>>(
                cartKey(userId),
                object : TypeToken<List<CartItem>>() {}.type
            ) ?: emptyList()
        }
    }

    private fun cartKey(userId: String) = "cart_$userId"
}
