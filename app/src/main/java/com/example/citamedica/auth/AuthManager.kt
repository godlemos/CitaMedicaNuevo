package com.example.citamedica.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.citamedica.models.Usuario

object AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun registrarUsuario(
        email: String, 
        password: String, 
        nombre: String,
        tipoUsuario: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Guardar información adicional en Firestore
                val usuario = Usuario(
                    id = user.uid,
                    nombre = nombre,
                    email = email,
                    tipoUsuario = tipoUsuario
                )
                db.collection("usuarios").document(user.uid).set(usuario).await()
            }
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerTipoUsuario(): String? {
        val user = auth.currentUser ?: return null
        val doc = db.collection("usuarios").document(user.uid).get().await()
        return doc.getString("tipoUsuario")
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun obtenerUsuarioActual(): FirebaseUser? {
        return auth.currentUser
    }
} 