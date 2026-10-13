package com.example.dispensadoraguaiot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRecuperar = findViewById<TextView>(R.id.tvRecuperar)
        val tvRegistro = findViewById<TextView>(R.id.tvRegistro)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Por favor, ingresa un correo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.isEmpty() || pass.length < 4) {
                Toast.makeText(this, "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        tvRecuperar.setOnClickListener {
            Toast.makeText(this, "Recuperación: Se ha enviado un enlace a tu correo institucional", Toast.LENGTH_LONG).show()
        }

        tvRegistro.setOnClickListener {
            Toast.makeText(this, "Registro: Contacta al administrador para habilitar un nuevo operador", Toast.LENGTH_LONG).show()
        }
    }
}