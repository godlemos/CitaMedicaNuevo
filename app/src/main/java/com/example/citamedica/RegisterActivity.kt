package com.example.citamedica

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.citamedica.auth.AuthManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userType = intent.getStringExtra("USER_TYPE") ?: "patient"

        setupRegisterButton()
        setupBackButton()
    }

    private fun setupRegisterButton() {
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val nombre = findViewById<TextInputEditText>(R.id.etNombreCompleto).text.toString()
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString()
            val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString()
            val confirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword).text.toString()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val result = AuthManager.registrarUsuario(email, password, nombre, userType)
                    result.onSuccess {
                        Toast.makeText(this@RegisterActivity, 
                            "Registro exitoso", 
                            Toast.LENGTH_SHORT).show()
                        finish()
                    }.onFailure { exception ->
                        Toast.makeText(this@RegisterActivity, 
                            "Error: ${exception.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, 
                        "Error: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBackButton() {
        findViewById<Button>(R.id.btnBackToLogin).setOnClickListener {
            finish()
        }
    }
}