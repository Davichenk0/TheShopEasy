package com.example.shopeasy.data

import com.example.shopeasy.SupabaseClient
import io.github.jan.supabase.auth.auth

internal object Session {

    private const val LOCAL_USER_ID = "invitado"

    /**
     * En Firestore se usa el UID anónimo de Firebase (necesario para las reglas).
     * En modo local se usa el usuario de Supabase o un identificador de invitado.
     */
    suspend fun userId(): String {
        if (FirebaseProvider.isConfigured) {
            return FirebaseProvider.userId()
        }
        return SupabaseClient.client.auth.currentUserOrNull()?.id ?: LOCAL_USER_ID
    }
}
