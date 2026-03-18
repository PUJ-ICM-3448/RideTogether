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
            
            // Aplicar color al estado
            val color = ContextCompat.getColor(root.context, ruta.colorEstado)
            tvStatus.setTextColor(color)
            ivStatusIcon.setColorFilter(color)

            // NAVEGACIÓN: Al presionar "Ver ruta" se abre VerRutaConductorActivity
            btnViewRoute.setOnClickListener {
                val context = root.context
                val intent = Intent(context, VerRutaConductorActivity::class.java)
                // Opcional: Pasar el nombre de la ruta como extra
                intent.putExtra("ROUTE_NAME", ruta.nombreRuta)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = rutas.size
}
