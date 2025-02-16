package com.example.citamedica

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.citamedica.adapters.CitaAdapter
import com.example.citamedica.models.Cita
import android.widget.Toast
import android.content.Intent
import android.app.AlertDialog
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.widget.TextView
import com.example.citamedica.utils.toDate
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.example.citamedica.auth.AuthManager
import com.example.citamedica.models.EstadoCita
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class RecepcionistaActivity : AppCompatActivity() {
    private lateinit var citaAdapter: CitaAdapter
    private val citas = mutableListOf<Cita>()
    private lateinit var calendarView: CalendarView
    private val db = FirebaseFirestore.getInstance()
    private var citasListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recepcionista)

        setupRecyclerView()
        setupCalendar()
        
        findViewById<FloatingActionButton>(R.id.fabNuevaCita).setOnClickListener {
            startActivity(Intent(this, NuevaCitaActivity::class.java))
        }

        setupCitasListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        citasListener?.remove()
    }

    private fun setupRecyclerView() {
        val rvCitas = findViewById<RecyclerView>(R.id.rvCitasAdmin)
        citaAdapter = CitaAdapter(
            citas = citas,
            isAdmin = true,
            onCitaClick = { cita ->
                Toast.makeText(this, "Cita de ${cita.pacienteNombre}", Toast.LENGTH_SHORT).show()
            },
            onCancelarClick = { cita ->
                cancelarCita(cita)
            },
            onEditarClick = { cita ->
                editarCita(cita)
            }
        )

        rvCitas.apply {
            layoutManager = LinearLayoutManager(this@RecepcionistaActivity)
            adapter = citaAdapter
        }
    }

    private fun setupCalendar() {
        calendarView = findViewById(R.id.calendarView)
        
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    // Filtrar citas por el día seleccionado
                    val citasDelDia = citas.filter { cita -> 
                        // Implementar filtrado por fecha
                        true // Temporalmente retorna todas las citas
                    }
                    citaAdapter.actualizarCitas(citasDelDia)
                }
            }
        }

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.textView.text = day.date.dayOfMonth.toString()
                
                if (day.owner == DayOwner.THIS_MONTH) {
                    container.textView.setTextColor(Color.BLACK)
                    container.textView.background = null
                } else {
                    container.textView.setTextColor(Color.GRAY)
                    container.textView.background = null
                }
            }
        }

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale("es")).firstDayOfWeek
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)
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
            .setMessage("¿Estás seguro de que deseas eliminar esta cita?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("citas")
                    .document(cita.id)
                    .delete()  // Eliminar el documento completamente
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cita eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, 
                            "Error al eliminar cita: ${e.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun setupCitasListener() {
        citasListener = db.collection("citas")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar citas: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val citasActualizadas = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Cita::class.java)
                } ?: emptyList()

                citaAdapter.actualizarCitas(citasActualizadas)
                citas.clear()
                citas.addAll(citasActualizadas)
            }
    }
} 