package com.example.proyecto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemParadaTutorBinding

class ParadasTutorAdapter(
    private val paradas: MutableList<Map<String, Any>>
) : RecyclerView.Adapter<ParadasTutorAdapter.VH>() {
    inner class VH(val b: ItemParadaTutorBinding) : RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemParadaTutorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val data       = paradas[position]
        val nombre     = data["nombre"]     as? String  ?: ""
        val direccion  = data["direccion"]  as? String  ?: ""
        val completada = data["completada"] as? Boolean ?: false
        holder.b.tvParadaNombreEstudiante.text = nombre
        holder.b.tvParadaDireccionTutor.text   = direccion
        holder.b.tvParadaEstadoTexto.text      = if (completada) "✓ Llegó" else "En camino"
        holder.b.tvParadaEstadoTexto.setTextColor(
            holder.itemView.context.getColor(
                if (completada) android.R.color.holo_green_dark
                else android.R.color.holo_orange_dark
            )
        )
        holder.b.ivParadaEstado.setImageResource(
            if (completada) android.R.drawable.checkbox_on_background
            else android.R.drawable.presence_busy
        )
    }
    override fun getItemCount() = paradas.size
}