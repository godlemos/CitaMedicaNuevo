package com.example.citamedica.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Cita(
    var id: String = "",
    val pacienteId: String = "",
    val pacienteNombre: String = "",
    val doctorId: String = "",
    val especialidadId: String = "",
    val fecha: Date = Date(),
    val estado: String = EstadoCita.PENDIENTE.name
) {
    constructor() : this(
        id = "",
        pacienteId = "",
        pacienteNombre = "",
        doctorId = "",
        especialidadId = "",
        fecha = Date(),
        estado = EstadoCita.PENDIENTE.name
    )
}

enum class EstadoCita {
    PROGRAMADA,
    CANCELADA,
    COMPLETADA,
    PENDIENTE
} 