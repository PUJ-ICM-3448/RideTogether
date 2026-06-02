package com.example.proyecto

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemRutaConductorBinding

class RutaConductorAdapter(private val rutas: List<RutaConductor>) :
    RecyclerView.Adapter<RutaConductorAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemRutaConductorBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRutaConductorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ruta = rutas[position]
        with(holder.binding) {
            tvRouteName.text = ruta.nombreRuta
            tvBusInfo.text = ruta.busAsignado
            tvStatus.text = ruta.estado
            val color = ContextCompat.getColor(root.context, ruta.colorEstado)
            tvStatus.setTextColor(color)
            ivStatusIcon.setColorFilter(color)
            btnViewRoute.setOnClickListener {
                val context = root.context
                val intent = Intent(context, VerRutaConductorActivity::class.java)
                intent.putExtra("ROUTE_NAME", ruta.nombreRuta)
                context.startActivity(intent)
            }
        }
    }
    override fun getItemCount() = rutas.size
}
