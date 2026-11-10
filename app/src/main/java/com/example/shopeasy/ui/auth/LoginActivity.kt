package com.example.shopeasy.ui.auth

import android.content.Intent
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.credentials.CredentialManager as AuthCredentialManager
import androidx.credentials.GetCredentialRequest
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.shopeasy.R
import com.example.shopeasy.SupabaseClient
import com.example.shopeasy.data.CredentialManager
import com.example.shopeasy.data.DeviceRepository
import com.example.shopeasy.data.UserRepository
import com.example.shopeasy.network.TokenManager
import com.example.shopeasy.ui.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var btnRegister: TextView
    private lateinit var btnStartSesion: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var repository: DeviceRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var layoutGoogle: LinearLayout
    private lateinit var tvFingerPrint: ImageView
    private lateinit var tvRePassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        repository = DeviceRepository(this)
        tokenManager = TokenManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
            insets
        }

        // Referenciar Views
        etEmail = findViewById(R.id.email_login)
        etPassword = findViewById(R.id.password_login)
        btnRegister = findViewById(R.id.btn_register)
        btnStartSesion = findViewById(R.id.btn_start_sesion)
        layoutGoogle = findViewById(R.id.btn_google)
        tvFingerPrint = findViewById(R.id.tv_fingerprint)
        tvRePassword = findViewById(R.id.btn_reregister)

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        tvRePassword.setOnClickListener {
            Toast.makeText(this, "Soon...", Toast.LENGTH_SHORT).show()
        }
        btnStartSesion.setOnClickListener {
            performLogin()
        }
        layoutGoogle.setOnClickListener {
            loginWithGoogle()
        }
        tvFingerPrint.setOnClickListener {
            showFingerprintDialog()
        }
    }

    private fun showFingerprintDialog() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                proceedWithBiometric()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(this, "No biometric features available on this device", Toast.LENGTH_SHORT).show()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(this, "Biometric features are currently unavailable", Toast.LENGTH_SHORT).show()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Toast.makeText(this, "No biometrics enrolled. Please check your security settings", Toast.LENGTH_SHORT).show()
            else ->
                Toast.makeText(this, "Biometric authentication is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun proceedWithBiometric() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@LoginActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val mail = CredentialManager.getMail(this@LoginActivity)
                    val password = CredentialManager.getPassword(this@LoginActivity)
                    
                    Log.d("Biometric_Debug", "Auth succeeded. Attempting Supabase login for: $mail")

                    if (!mail.isNullOrEmpty() && !password.isNullOrEmpty()) {
                        lifecycleScope.launch {
                            try {
                                SupabaseClient.client.auth.signInWith(Email) {
                                    this.email = mail
                                    this.password = password
                                }
                                Log.d("Biometric_Debug", "Supabase login successful via Biometry")
                                navigateToMainActivity()
                            } catch (e: Exception) {
                                Log.e("Biometric_Debug", "Supabase Login error: ${e.message}")
                                Toast.makeText(this@LoginActivity, "Supabase Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.w("Biometric_Debug", "Credentials not found in SharedPreferences")
                        Toast.makeText(this@LoginActivity, "No credentials saved. Please login manually first.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("Login_Debug", "Starting login process for: $email")
                
                // 1. Login en Supabase (Prioridad Alta)
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                Log.d("Login_Debug", "Supabase login successful")
                
                // Guardamos credenciales para Biometría ya que Supabase confirmó que son correctas
                CredentialManager.saveCredentials(this@LoginActivity, email, password)

                // 2. Intento de Login en API .NET para obtener Token de servicios
                try {
                    val loginDto = com.example.shopeasy.data.LoginDto(email, password)
                    val response = repository.login(loginDto)
                    
                    if (response.isSuccessful) {
                        Log.d("Login_Debug", ".NET API login successful")
                        response.body()?.token?.let { token ->
                            tokenManager.saveToken(token)
                        }
                    } else {
                        Log.w("Login_Debug", ".NET API login failed (Code: ${response.code()}). Continuing with Supabase session.")
                    }
                } catch (e: Exception) {
                    Log.e("Login_Debug", "Optional .NET API login failed: ${e.message}. The user is still authenticated via Supabase.")
                }

                // Navegamos porque el login de Supabase fue exitoso
                navigateToMainActivity()

            } catch (e: Exception) {
                Log.e("Login_Debug", "Login Exception: ${e.message}", e)
                val message = when {
                    e.message?.contains("Invalid login credentials") == true || 
                    e.message?.contains("400") == true -> "Invalid email or password (Supabase)"
                    else -> "Error in login: ${e.localizedMessage}"
                }
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loginWithGoogle() {
        lifecycleScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("987657683989-cbdklkkepjq7hocc0nqfqpobmkiuhsnc.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val authCredentialManager = AuthCredentialManager.create(this@LoginActivity)
                val result = authCredentialManager.getCredential(context = this@LoginActivity, request = request)

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)
                // send token from google to supabase
                SupabaseClient.client.auth.signInWith(IDToken) {
                    idToken = googleIdTokenCredential.idToken
                }

                val user = SupabaseClient.client.auth.currentUserOrNull()
                if (user != null) {
                    val exist = UserRepository.userExits(userId = user.id)

                    if (!exist) {
                        val nameComplete = user.userMetadata
                            ?.get("full_name")?.toString()
                            ?.replace(oldValue = "\"", newValue = "")?:""
                        val names = if (nameComplete.contains(" ")) nameComplete.split(" ")[0] else nameComplete
                        val last_name = if (nameComplete.contains(" ")) nameComplete.split(" ").getOrElse(1) { "" } else ""
                        UserRepository.insertUser(user.id, names = names, lastName = last_name, mail = user.email)
                    }
                }
                navigateToMainActivity()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error to login with Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
