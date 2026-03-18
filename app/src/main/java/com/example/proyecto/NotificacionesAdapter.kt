package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemNotificationBinding

class NotificacionesAdapter(private var notifications: MutableList<NotificationItem>) :
    RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notifications[position]
        with(holder.binding) {
            tvTitle.text = item.title
            tvDescription.text = item.description

            // Lógica visual según el tipo de notificación
            when (item.type) {
                "bus" -> {
                    ivIcon.setImageResource(android.R.drawable.ic_dialog_map) // Placeholder para bus
                    tvTitle.setTextColor(ContextCompat.getColor(root.context, R.color.black))
                }
                "estudiante" -> {
                    ivIcon.setImageResource(android.R.drawable.ic_menu_myplaces) // Placeholder para estudiante
                    tvTitle.setTextColor(ContextCompat.getColor(root.context, R.color.black))
                }
                "incidente" -> {
                    ivIcon.setImageResource(android.R.drawable.ic_dialog_alert) // Placeholder para alerta
                    ivIcon.setColorFilter(ContextCompat.getColor(root.context, android.R.color.holo_orange_dark))
                    tvTitle.setTextColor(ContextCompat.getColor(root.context, android.R.color.holo_orange_dark))
                }
            }
        }
    }

    override fun getItemCount() = notifications.size

    /**
     * Función para limpiar la lista de notificaciones
     */
    fun clearAll() {
        notifications.clear()
        notifyDataSetChanged()
    }
}
