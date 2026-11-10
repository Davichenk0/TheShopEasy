package com.example.shopeasy.ui.inicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.shopeasy.R
import com.example.shopeasy.ui.auth.LoginActivity
import com.example.shopeasy.ui.auth.RegisterActivity


class InicioActivity : AppCompatActivity() {

    private lateinit var  btnLogin: Button
    private lateinit var tvRegister: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)


        btnLogin = findViewById(R.id.btn_login)
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        tvRegister = findViewById(R.id.tv_register)
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }




    }
}