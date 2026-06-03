package com.example.proyecto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemParadaBinding

class ParadasAdapter(
    val paradas: MutableList<Pair<String, Map<String, Any>>>,
    private val onCompletar: (String) -> Unit
) : RecyclerView.Adapter<ParadasAdapter.VH>() {
    inner class VH(val b: ItemParadaBinding) : RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemParadaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val (id, data) = paradas[position]
        val orden      = (data["orden"] as? Long ?: position.toLong()) + 1
        val direccion  = data["direccion"] as? String ?: ""
        val completada = data["completada"] as? Boolean ?: false

        holder.b.tvParadaNumero.text   = "Parada #$orden"
        holder.b.tvParadaDireccion.text = direccion
        holder.b.tvParadaEstado.text   = if (completada) "✓ Completada" else "● Pendiente"
        holder.b.tvParadaEstado.setTextColor(
            holder.itemView.context.getColor(
                if (completada) android.R.color.holo_green_dark else android.R.color.holo_orange_dark
            )
        )
        holder.b.btnCompletarParada.isEnabled = !completada
        holder.b.btnCompletarParada.setOnClickListener { onCompletar(id) }
    }
    override fun getItemCount() = paradas.size
}