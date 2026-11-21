package com.example.shopeasy.data

import android.content.Context
import com.example.shopeasy.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Inicializa Firebase sin el plugin de Google Services para que el proyecto
 * compile aunque todavía no exista google-services.json.
 */
object FirebaseProvider {

    private const val APP_NAME = "shopeasy"

    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    val isConfigured: Boolean
        get() = firestore != null

    val db: FirebaseFirestore
        get() = firestore ?: error("Firestore no está configurado. Revisa local.properties.")

    fun initialize(context: Context) {
        if (firestore != null) return
        if (BuildConfig.FIREBASE_PROJECT_ID.isBlank() ||
            BuildConfig.FIREBASE_APP_ID.isBlank() ||
            BuildConfig.FIREBASE_API_KEY.isBlank()
        ) {
            return
        }

        val options = FirebaseOptions.Builder()
            .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
            .setApplicationId(BuildConfig.FIREBASE_APP_ID)
            .setApiKey(BuildConfig.FIREBASE_API_KEY)
            .build()

        val app = FirebaseApp.initializeApp(context.applicationContext, options, APP_NAME) ?: return
        firestore = FirebaseFirestore.getInstance(app)
        auth = FirebaseAuth.getInstance(app)
    }

    /**
     * Firestore necesita una identidad para aplicar las reglas de seguridad.
     * Usamos una sesión anónima de Firebase, independiente del login de Supabase.
     */
    suspend fun userId(): String {
        val firebaseAuth = auth ?: error("Firebase Authentication no está configurado.")
        firebaseAuth.currentUser?.let { return it.uid }

        val result = firebaseAuth.signInAnonymously().await()
        return result.user?.uid
            ?: error("No fue posible crear la sesión anónima de Firebase.")
    }
}
