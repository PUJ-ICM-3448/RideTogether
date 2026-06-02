package com.example.proyecto

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemEstudianteAdministradorBinding

class EstudiantesFirestoreAdapter(
    private val lista: MutableList<Pair<String, Map<String, Any>>>,
    private val onDetalles: (String, Map<String, Any>) -> Unit
) : RecyclerView.Adapter<EstudiantesFirestoreAdapter.VH>() {
    inner class VH(val b: ItemEstudianteAdministradorBinding) :
        RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemEstudianteAdministradorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val id   = lista[position].first
        val data = lista[position].second
        holder.b.tvName.text  = data["nombre"]          as? String ?: "Sin nombre"
        holder.b.tvRoute.text = "Ruta: ${data["ruta"]   as? String ?: "Sin asignar"}"
        holder.b.tvStop.text  = "Parada: ${data["direccionParada"] as? String ?: ""}"
        holder.b.btnDetails.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, DetalleEstudianteActivity::class.java).apply {
                putExtra("estudianteId",  id)
                putExtra("nombre",        data["nombre"]          as? String ?: "")
                putExtra("documento",     data["documento"]       as? String ?: "")
                putExtra("ruta",          data["ruta"]            as? String ?: "")
                putExtra("tutorNombre",   data["tutorNombre"]     as? String ?: "")
                putExtra("tutorUid",      data["tutorUid"]        as? String ?: "")
                putExtra("tutorPhone",    data["tutorPhone"]      as? String ?: "")
                putExtra("tutorEmail",    data["tutorEmail"]      as? String ?: "")
            }
            ctx.startActivity(intent)
        }
    }

    override fun getItemCount() = lista.size
}