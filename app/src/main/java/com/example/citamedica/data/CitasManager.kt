package com.example.citamedica.data

import com.example.citamedica.models.Cita
import com.example.citamedica.models.EstadoCita
import java.util.*
import java.text.SimpleDateFormat
import java.util.UUID
import java.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object CitasManager {
    private val db = FirebaseFirestore.getInstance()
    private val citas = mutableListOf<Cita>()

    fun getCitasPaciente(nombrePaciente: String): List<Cita> {
        return citas.filter { 
            it.pacienteNombre == nombrePaciente && 
            it.estado != EstadoCita.CANCELADA.name 
        }
    }

    fun getAllCitas(): List<Cita> {
        return citas.filter { it.estado != EstadoCita.CANCELADA.name }
    }

    fun getCitasByFecha(fecha: Date): List<Cita> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return citas.filter { 
            it.estado != EstadoCita.CANCELADA.name &&
            dateFormat.format(it.fecha) == dateFormat.format(fecha) 
        }
    }

    fun addCita(cita: Cita): Boolean {
        if (isHorarioDisponible(cita.fecha, cita.doctorId)) {
            citas.add(cita.copy(id = UUID.randomUUID().toString()))
            return true
        }
        return false
    }

    fun isHorarioDisponible(fecha: Date, doctorId: String): Boolean {
        val calendar = Calendar.getInstance().apply { time = fecha }
        
        val horaInicio = calendar.clone() as Calendar
        val horaFin = calendar.clone() as Calendar
        horaFin.add(Calendar.HOUR_OF_DAY, 1)

        return citas.none { cita ->
            if (cita.doctorId != doctorId) return@none false
            
            val citaCalendar = Calendar.getInstance().apply { time = cita.fecha }
            val citaFin = citaCalendar.clone() as Calendar
            citaFin.add(Calendar.HOUR_OF_DAY, 1)

            !(horaFin.time <= citaCalendar.time || horaInicio.time >= citaFin.time)
        }
    }

    fun cancelarCita(id: String) {
        val index = citas.indexOfFirst { it.id == id }
        if (index != -1) {
            citas.removeAt(index)
        }
    }

    fun updateCita(cita: Cita) {
        val index = citas.indexOfFirst { it.id == cita.id }
        if (index != -1) {
            citas[index] = cita
        }
    }

    suspend fun getCitasByDoctorId(doctorId: String): List<Cita> {
        return try {
            val snapshot = db.collection("citas")
                .whereEqualTo("doctorId", doctorId)
                .whereNotEqualTo("estado", EstadoCita.CANCELADA.name)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Cita::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCitasByPacienteId(pacienteId: String): List<Cita> {
        return try {
            val snapshot = db.collection("citas")
                .whereEqualTo("pacienteId", pacienteId)
                .whereNotEqualTo("estado", EstadoCita.CANCELADA.name)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Cita::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 