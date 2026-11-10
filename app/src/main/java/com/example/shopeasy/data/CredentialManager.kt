package com.example.shopeasy.data

import android.content.Context
import androidx.core.content.edit

object CredentialManager {

    private const val PREF_NAME = "auth_credentials"
    private const val KEY_MAIL = "mail"
    private const val KEY_PASSWORD = "password"
    private const val KEY_FINGERPRINT = "fingerprint_active"

    fun saveCredentials(context: Context, mail: String, password: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_MAIL, mail)
                putString(KEY_PASSWORD, password)
                putBoolean(KEY_FINGERPRINT, true)
            }
    }

    fun clearCredentials(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                clear()
            }
    }

    fun getMail(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MAIL, null)

    fun getPassword(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PASSWORD, null)

    fun fingerprintActive(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FINGERPRINT, false)

}
