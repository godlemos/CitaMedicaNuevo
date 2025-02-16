package com.example.citamedica

import android.app.Application
import java.util.Locale
import com.google.firebase.FirebaseApp
import android.util.Log

class CitaMedicaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("CitaMedicaApp", "Error initializing Firebase", e)
        }
        // Establecer el idioma español como predeterminado
        val config = resources.configuration
        val locale = Locale("es")
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
} 