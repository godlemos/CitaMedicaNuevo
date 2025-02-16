package com.example.citamedica.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.citamedica.R
import com.example.citamedica.models.Cita
import com.example.citamedica.data.EspecialidadesManager
import java.text.SimpleDateFormat
import java.util.Locale

class CitaAdapter(
    private var citas: List<Cita>,
    private val isAdmin: Boolean = false,
    private val onCitaClick: (Cita) -> Unit,
    private val onCancelarClick: (Cita) -> Unit,
    private val onEditarClick: (Cita) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPaciente: TextView = view.findViewById(R.id.tvPaciente)
        val tvDoctor: TextView = view.findViewById(R.id.tvDoctor)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val btnCancelar: Button = view.findViewById(R.id.btnCancelar)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = citas[position]
        val doctor = EspecialidadesManager.getDoctorById(cita.doctorId)
        val especialidad = EspecialidadesManager.getEspecialidadById(cita.especialidadId)
        
        holder.tvPaciente.text = cita.pacienteNombre
        holder.tvDoctor.text = "${doctor?.nombre} - ${especialidad?.nombre}"
        holder.tvFecha.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(cita.fecha)

        holder.itemView.setOnClickListener { onCitaClick(cita) }
        
        if (isAdmin) {
            holder.btnEditar.visibility = View.VISIBLE
            holder.btnEditar.setOnClickListener { onEditarClick(cita) }
        } else {
            holder.btnEditar.visibility = View.GONE
        }
        
        holder.btnCancelar.setOnClickListener { onCancelarClick(cita) }
    }

    override fun getItemCount() = citas.size

    fun actualizarCitas(nuevasCitas: List<Cita>) {
        this.citas = nuevasCitas
        notifyDataSetChanged()
    }
} 