package com.example.citamedica

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.citamedica.adapters.CitaAdapter
import com.example.citamedica.models.Cita
import com.example.citamedica.data.EspecialidadesManager
import android.app.AlertDialog
import com.example.citamedica.auth.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.ListenerRegistration
import com.example.citamedica.models.EstadoCita
import android.util.Log

class PacienteActivity : AppCompatActivity() {
    private lateinit var citaAdapter: CitaAdapter
    private val db = FirebaseFirestore.getInstance()
    private var citasListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paciente)

        setupRecyclerView()
        
        findViewById<FloatingActionButton>(R.id.fabNuevaCita).setOnClickListener {
            startActivity(Intent(this, NuevaCitaActivity::class.java))
        }

        setupCitasListener()
    }

    private fun setupRecyclerView() {
        val rvCitas = findViewById<RecyclerView>(R.id.rvCitasPaciente)
        citaAdapter = CitaAdapter(
            citas = emptyList(),
            onCitaClick = { cita ->
                lifecycleScope.launch {
                    try {
                        val doctor = EspecialidadesManager.getDoctorById(cita.doctorId)
                        val especialidad = EspecialidadesManager.getEspecialidadById(cita.especialidadId)
                        Log.d("PacienteActivity", "Cita con ${doctor?.nombre} - ${especialidad?.nombre}")
                    } catch (e: Exception) {
                        Log.e("PacienteActivity", "Error al cargar detalles", e)
                    }
                }
            },
            onCancelarClick = { cita ->
                cancelarCita(cita)
            },
            onEditarClick = { cita ->
                editarCita(cita)
            }
        )

        rvCitas.apply {
            layoutManager = LinearLayoutManager(this@PacienteActivity)
            adapter = citaAdapter
        }
    }

    private fun setupCitasListener() {
        val currentUser = AuthManager.obtenerUsuarioActual()
        if (currentUser == null) {
            Log.e("PacienteActivity", "Usuario no autenticado")
            return
        }

        citasListener?.remove()

        citasListener = db.collection("citas")
            .whereEqualTo("pacienteId", currentUser.uid)
            .whereNotEqualTo("estado", EstadoCita.CANCELADA.name)
            .orderBy("estado")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("PacienteActivity", "Error al escuchar cambios", e)
                    return@addSnapshotListener
                }

                val citas = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Cita::class.java)?.apply {
                        id = doc.id
                    }
                } ?: emptyList()

                citaAdapter.actualizarCitas(citas)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        citasListener?.remove()
    }

    private fun editarCita(cita: Cita) {
        val intent = Intent(this, NuevaCitaActivity::class.java).apply {
            putExtra("CITA_ID", cita.id)
        }
        startActivity(intent)
    }

    private fun cancelarCita(cita: Cita) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Estás seguro de que deseas cancelar esta cita?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            db.collection("citas")
                                .document(cita.id)
                                .update(mapOf(
                                    "estado" to EstadoCita.CANCELADA.name
                                ))
                                .await()
                        }
                        Log.d("PacienteActivity", "Cita cancelada exitosamente")
                    } catch (e: Exception) {
                        Log.e("PacienteActivity", "Error al cancelar cita", e)
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
} 