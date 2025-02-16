package com.example.citamedica.models

data class Usuario(
    val id: String = "",          // ID único de Firebase Auth
    val nombre: String = "",      // Nombre completo del usuario
    val email: String = "",       // Email del usuario
    val tipoUsuario: String = ""  // "patient" o "receptionist"
) 