package com.example.citamedica

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import com.example.citamedica.models.Cita
import com.example.citamedica.data.EspecialidadesManager
import com.example.citamedica.data.Doctor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.example.citamedica.auth.AuthManager
import com.example.citamedica.models.EstadoCita
import android.util.Log

class NuevaCitaActivity : AppCompatActivity() {
    private var selectedEspecialidadId: String? = null
    private var selectedDoctorId: String? = null
    private var selectedDate: Date? = null
    private var citaId: String? = null
    private var nombrePaciente: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_cita)

        citaId = intent.getStringExtra("CITA_ID")
        
        lifecycleScope.launch {
            try {
                val currentUser = AuthManager.obtenerUsuarioActual()
                if (currentUser != null) {
                    val userDoc = FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(currentUser.uid)
                        .get()
                        .await()
                    
                    nombrePaciente = userDoc.getString("nombre") ?: ""
                }
            } catch (e: Exception) {
                Toast.makeText(this@NuevaCitaActivity, 
                    "Error al obtener datos del usuario", 
                    Toast.LENGTH_SHORT).show()
            }
        }

        setupEspecialidadSpinner()
        setupDatePicker()
        setupTimePicker()
        setupGuardarButton()
    }

    private fun setupEspecialidadSpinner() {
        val spinnerEspecialidad = findViewById<AutoCompleteTextView>(R.id.spinnerEspecialidad)
        val especialidades = EspecialidadesManager.especialidades
        
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            especialidades.map { it.nombre }
        )
        
        spinnerEspecialidad.setAdapter(adapter)
        spinnerEspecialidad.setOnItemClickListener { _, _, position, _ ->
            selectedEspecialidadId = especialidades[position].id
            setupDoctorSpinner(selectedEspecialidadId!!)
        }
    }

    private fun setupDoctorSpinner(especialidadId: String) {
        val spinnerDoctor = findViewById<AutoCompleteTextView>(R.id.spinnerDoctor)
        val doctores = EspecialidadesManager.getDoctoresByEspecialidad(especialidadId)
        
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            doctores.map { it.nombre }
        )
        
        spinnerDoctor.setAdapter(adapter)
        spinnerDoctor.text.clear()
        spinnerDoctor.setOnItemClickListener { _, _, position, _ ->
            selectedDoctorId = doctores[position].id
        }
    }

    private fun setupDatePicker() {
        val etFecha = findViewById<TextInputEditText>(R.id.etFecha)
        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val locale = Locale("es")
            Locale.setDefault(locale)
            
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    etFecha.setText(SimpleDateFormat("dd/MM/yyyy", locale).format(selectedDate!!))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Configurar en español
            datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY
            datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Aceptar", datePickerDialog)
            datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancelar", datePickerDialog)
            datePickerDialog.setTitle("Seleccionar fecha")
            
            datePickerDialog.show()
        }
    }

    private fun setupTimePicker() {
        val etHora = findViewById<TextInputEditText>(R.id.etHora)
        etHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val locale = Locale("es")
            Locale.setDefault(locale)
            
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hour, minute ->
                    if (selectedDate == null) {
                        Toast.makeText(this, "Por favor, seleccione primero una fecha", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }
                    
                    val dateCalendar = Calendar.getInstance().apply { time = selectedDate!! }
                    dateCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    dateCalendar.set(Calendar.MINUTE, minute)
                    selectedDate = dateCalendar.time
                    
                    etHora.setText(SimpleDateFormat("HH:mm", locale).format(selectedDate!!))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // Formato 24 horas
            )

            // Configurar en español
            timePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Aceptar", timePickerDialog)
            timePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancelar", timePickerDialog)
            timePickerDialog.setTitle("Seleccionar hora")
            
            timePickerDialog.show()
        }
    }

    private fun setupGuardarButton() {
        findViewById<android.widget.Button>(R.id.btnGuardarCita).setOnClickListener {
            if (selectedEspecialidadId == null || selectedDoctorId == null || selectedDate == null) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val currentUser = AuthManager.obtenerUsuarioActual()
                    if (currentUser == null) {
                        Log.e("NuevaCitaActivity", "Usuario no autenticado")
                        return@launch
                    }

                    val citaIdLocal = citaId

                    val nuevaCita = Cita(
                        id = citaId ?: "",
                        fecha = selectedDate!!,
                        pacienteId = currentUser.uid,
                        pacienteNombre = nombrePaciente,
                        especialidadId = selectedEspecialidadId!!,
                        doctorId = selectedDoctorId!!,
                        estado = EstadoCita.PROGRAMADA.name
                    )

                    withContext(Dispatchers.IO) {
                        val db = FirebaseFirestore.getInstance()
                        if (citaIdLocal == null) {
                            val citaRef = db.collection("citas").document()
                            nuevaCita.id = citaRef.id
                            citaRef.set(nuevaCita).await()
                        } else {
                            db.collection("citas")
                                .document(citaIdLocal)
                                .update(mapOf(
                                    "fecha" to nuevaCita.fecha,
                                    "especialidadId" to nuevaCita.especialidadId,
                                    "doctorId" to nuevaCita.doctorId,
                                    "estado" to EstadoCita.PROGRAMADA.name
                                )).await()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("NuevaCitaActivity", "Error al guardar la cita", e)
                }
            }
        }
    }
} 