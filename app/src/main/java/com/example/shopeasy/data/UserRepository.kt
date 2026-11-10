package com.example.shopeasy.data

import com.example.shopeasy.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class IdResponse(val id: String)

object UserRepository {

    suspend fun userExits(userId: String): Boolean {
        return try {
            val result = SupabaseClient.client
                .postgrest["users"]
                .select(Columns.raw("id")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<Map<String, String>>()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentUser(): UserData? {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return null
        return try {
            val result = SupabaseClient.client
                .postgrest["users"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<UserData>()
            result.firstOrNull()

        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error fetching current user", e)
            null
        }
    }
    suspend fun insertUser(id: String, names: String, lastName: String, mail: String?): Boolean {
        return try {
            val newUser = UserData(
                id = id,
                names = names,
                lastName = lastName,
                mail = mail
            )

            SupabaseClient.client.postgrest["users"].insert(newUser)
            true
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error inserting user", e)
            false
        }
    }

    suspend fun getCurrentRol(): String{
        return try {
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return "customer"
            val result = SupabaseClient.client
                .postgrest["users"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<UserData>()
            result.firstOrNull()?.rol ?: "customer"
        } catch (e: Exception) {
            "customer"
        }
    }

    suspend fun updateProfile(names: String, lastName: String, mail: String? = null, imageUrl: String? = null) {
        val userId = SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return

        val data = buildJsonObject {
            put("names", names)
            put("last_name", lastName)
            put("mail", mail)
            // picture new
            if (imageUrl != null) put("img_url", imageUrl)
        }
        SupabaseClient.client.postgrest["users"]
            .update(data){
                filter {eq(column = "id", value = userId)}
            }

    }
    suspend fun addImageProfile(
        contexto: android.content.Context,
        uri: android.net.Uri
    ): String {
        val userId = SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return ""

        val bytes = if (uri.scheme == "content") {
            contexto.contentResolver.openInputStream(uri)?.readBytes()
        } else {
            java.io.File(uri.path!!).readBytes()
        }?:return ""

        val ruteFile = "perfil_$userId.jpg"

        SupabaseClient.client.storage["avatars"]
            .update(
                path = ruteFile,
                data = bytes,
                options = {upsert = true}
            )
        return SupabaseClient.client.storage["avatars"]
            .publicUrl(path = ruteFile)
    }
}
