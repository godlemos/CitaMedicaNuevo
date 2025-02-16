package com.example.citamedica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class UserTypeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_type)

        findViewById<MaterialCardView>(R.id.cardPaciente).setOnClickListener {
            navigateToLogin("patient")
        }

        findViewById<MaterialCardView>(R.id.cardRecepcionista).setOnClickListener {
            navigateToLogin("receptionist")
        }
    }

    private fun navigateToLogin(userType: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_TYPE", userType)
        }
        startActivity(intent)
    }
} 