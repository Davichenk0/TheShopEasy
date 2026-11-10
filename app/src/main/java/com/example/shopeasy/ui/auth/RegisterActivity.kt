package com.example.shopeasy.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.shopeasy.R
import com.example.shopeasy.SupabaseClient
import com.example.shopeasy.data.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRePassword: EditText
    private lateinit var checkTerms: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var re_account: TextView

    @Serializable
    data class UserData(
        val id: String,
        val names: String,
        val lastname: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_register)

        //Referenciar las vistas (elementos presentes en el xml)
        etName = findViewById(R.id.re_name)
        etLastName = findViewById(R.id.re_last_name)
        etEmail = findViewById(R.id.re_email)
        etPassword = findViewById(R.id.re_password)
        etRePassword = findViewById(R.id.re_repassword)
        checkTerms = findViewById(R.id.check_conditions)
        btnRegister = findViewById(R.id.btn_register)
        re_account = findViewById(R.id.re_account)


        // Scroll Adaptable (Ajustar padding cuando sale el teclado)
        val rootView = findViewById<ViewGroup>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
            insets
        }

        // Botón de registro
        btnRegister.setOnClickListener {
            val names = etName.text.toString().trim()
            val lastname = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val rePassword = etRePassword.text.toString().trim()

            //Validación local de campos
            if (names.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else if (password != rePassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Constraseña segura
            if (password.length < 8) {
                Toast.makeText(
                    this,
                    "La contraseña debe tener al menos 8 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // Validación de términos y condiciones
            if (!checkTerms.isChecked) {
                Toast.makeText(this, "Debe aceptar los términos y condiciones", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            // Registrar usuario en Supabase
            lifecycleScope.launch {
                try {
                    // 1) Registro correo y contraseña en Supabase Auth
                    val response = SupabaseClient.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    
                    val userId = response?.id ?: ""

                    if (userId.isNotEmpty()) {
                        val insertSuccess = UserRepository.insertUser(
                            id = userId,
                            names = names,
                            lastName = lastname,
                            mail = email
                        )

                        if (insertSuccess) {
                            runOnUiThread {
                                Toast.makeText(this@RegisterActivity, "Registro exitoso. Revisa tu correo si es necesario.", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                finish()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@RegisterActivity, "Error al guardar datos de perfil", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "No se pudo obtener el ID de usuario", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Error en el registro: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // Botón para volver al login
        re_account.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
