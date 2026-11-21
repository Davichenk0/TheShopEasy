package com.example.shopeasy.data

import android.content.Context
import com.google.gson.Gson
import java.lang.reflect.Type

/**
 * Almacenamiento local mínimo para que el carrito y los pedidos sigan
 * funcionando mientras se configura Firestore.
 */
internal class LocalStore(context: Context) {

    private val preferences =
        context.getSharedPreferences("shopeasy_local_store", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun <T> read(key: String, type: Type): T? {
        val raw = preferences.getString(key, null) ?: return null
        return runCatching { gson.fromJson<T>(raw, type) }.getOrNull()
    }

    fun write(key: String, value: Any) {
        preferences.edit().putString(key, gson.toJson(value)).apply()
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
}
