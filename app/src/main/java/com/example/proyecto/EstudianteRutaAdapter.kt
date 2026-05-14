package com.example.proyecto

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemEstudianteRutaBinding

class EstudianteRutaAdapter(private val estudiantes: List<EstudianteRuta>) :
    RecyclerView.Adapter<EstudianteRutaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemEstudianteRutaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEstudianteRutaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = estudiantes[position]
        with(holder.binding) {
            tvStudentName.text = item.nombre
            tvRouteLabel.text = "Ruta: ${item.ruta}"
            tvStatus.text = "Estado: ${item.estado}"
            
            // Aplicar color al estado
            tvStatus.setTextColor(ContextCompat.getColor(root.context, item.colorEstado))

            // Botón Finalizar viaje
            btnFinishTrip.setOnClickListener {
                Toast.makeText(root.context, "Viaje finalizado para ${item.nombre}", Toast.LENGTH_SHORT).show()
            }

            // NAVEGACIÓN CORREGIDA: Al presionar "Reportar incidente" se abre ReportarIncidenteActivity
            btnReportIncident.setOnClickListener {
                val context = root.context
                val intent = Intent(context, ReportarIncidenteActivity::class.java)
                // Pasamos datos del estudiante como extras
                intent.putExtra("STUDENT_NAME", item.nombre)
                intent.putExtra("STUDENT_ROUTE", item.ruta)
                intent.putExtra("STUDENT_STATUS", item.estado)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = estudiantes.size
}
