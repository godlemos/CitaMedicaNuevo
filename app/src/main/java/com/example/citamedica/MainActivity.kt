package com.example.citamedica

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.citamedica.auth.AuthManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Obtener el tipo de usuario
        userType = intent.getStringExtra("USER_TYPE") ?: "patient"

        // Actualizar el título según el tipo de usuario
        findViewById<TextView>(R.id.tvLogin).text = when(userType) {
            "patient" -> "Login Paciente"
            else -> "Login Recepcionista"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLoginButton()
        setupRegisterButton()
    }

    private fun setupLoginButton() {
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString()
            val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val result = AuthManager.iniciarSesion(email, password)
                    result.onSuccess { user ->
                        val tipoUsuario = AuthManager.obtenerTipoUsuario()
                        if (tipoUsuario == userType) {
                            val nextActivity = when(userType) {
                                "patient" -> PacienteActivity::class.java
                                else -> RecepcionistaActivity::class.java
                            }
                            startActivity(Intent(this@MainActivity, nextActivity))
                            finish()
                        } else {
                            Toast.makeText(this@MainActivity, 
                                "Tipo de usuario incorrecto", 
                                Toast.LENGTH_SHORT).show()
                            AuthManager.cerrarSesion()
                        }
                    }.onFailure { exception ->
                        Toast.makeText(this@MainActivity, 
                            "Error: ${exception.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, 
                        "Error: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRegisterButton() {
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java).apply {
                putExtra("USER_TYPE", userType)
            }
            startActivity(intent)
        }
    }
}