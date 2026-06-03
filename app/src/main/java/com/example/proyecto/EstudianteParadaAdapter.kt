package com.example.proyecto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemEstudianteParadaBinding

class EstudiantesParadaAdapter(
    private val lista: List<Map<String, Any>>,
    private val onAgregar: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<EstudiantesParadaAdapter.VH>() {

    inner class VH(val b: ItemEstudianteParadaBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemEstudianteParadaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val data = lista[position]
        holder.b.tvEstudianteNombre.text    = data["nombre"]          as? String ?: ""
        holder.b.tvEstudianteDireccion.text = data["direccionParada"] as? String ?: "Sin dirección"
        holder.b.tvEstudianteRuta.text      = "Ruta: ${data["ruta"]  as? String ?: ""}"
        holder.b.btnSeleccionarEstudiante.setOnClickListener { onAgregar(data) }
    }

    override fun getItemCount() = lista.size
}