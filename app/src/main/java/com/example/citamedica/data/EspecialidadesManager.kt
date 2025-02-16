package com.example.citamedica.data

data class Doctor(
    val id: String,
    val nombre: String,
    val especialidadId: String
)

data class Especialidad(
    val id: String,
    val nombre: String
)

object EspecialidadesManager {
    val especialidades = listOf(
        Especialidad("1", "Cardiología"),
        Especialidad("2", "Dermatología"),
        Especialidad("3", "Pediatría"),
        Especialidad("4", "Traumatología"),
        Especialidad("5", "Oftalmología"),
        Especialidad("6", "Ginecología"),
        Especialidad("7", "Neurología"),
        Especialidad("8", "Psiquiatría"),
        Especialidad("9", "Endocrinología"),
        Especialidad("10", "Otorrinolaringología")
    )

    val doctores = listOf(
        // Cardiología (3 doctores)
        Doctor("1", "Dr. Juan Pérez", "1"),
        Doctor("2", "Dra. María García", "1"),
        Doctor("3", "Dr. Roberto Díaz", "1"),

        // Dermatología (2 doctores)
        Doctor("4", "Dra. Ana Martínez", "2"),
        Doctor("5", "Dr. Carlos López", "2"),

        // Pediatría (3 doctores)
        Doctor("6", "Dra. Laura Sánchez", "3"),
        Doctor("7", "Dr. Luis Torres", "3"),
        Doctor("8", "Dra. Patricia Vega", "3"),

        // Traumatología (1 doctor)
        Doctor("9", "Dr. Pedro Ramírez", "4"),

        // Oftalmología (2 doctores)
        Doctor("10", "Dra. Carmen Ruiz", "5"),
        Doctor("11", "Dr. Javier Morales", "5"),

        // Ginecología (2 doctores)
        Doctor("12", "Dra. Isabel Castro", "6"),
        Doctor("13", "Dra. Sofía Luna", "6"),

        // Neurología (1 doctor)
        Doctor("14", "Dr. Miguel Flores", "7"),

        // Psiquiatría (2 doctores)
        Doctor("15", "Dr. Fernando Silva", "8"),
        Doctor("16", "Dra. Elena Vargas", "8"),

        // Endocrinología (1 doctor)
        Doctor("17", "Dra. Rosa Mendoza", "9"),

        // Otorrinolaringología (2 doctores)
        Doctor("18", "Dr. Alberto Ruiz", "10"),
        Doctor("19", "Dra. Carmen Ortiz", "10")
    )

    fun getDoctoresByEspecialidad(especialidadId: String): List<Doctor> {
        return doctores.filter { it.especialidadId == especialidadId }
    }

    fun getEspecialidadById(id: String): Especialidad? {
        return especialidades.find { it.id == id }
    }

    fun getDoctorById(id: String): Doctor? {
        return doctores.find { it.id == id }
    }
} 