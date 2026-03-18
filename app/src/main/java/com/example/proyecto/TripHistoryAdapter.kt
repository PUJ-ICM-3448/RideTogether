package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemTripHistoryBinding

class TripHistoryAdapter(private val trips: List<TripHistory>) :
    RecyclerView.Adapter<TripHistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTripHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTripHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = trips[position]
        with(holder.binding) {
            tvStudentName.text = trip.studentName
            tvRoute.text = "Ruta: ${trip.route}"
            
            // Mostrar u ocultar bus y conductor
            if (trip.bus != null) {
                tvBus.text = "Bus: ${trip.bus}"
                tvBus.visibility = View.VISIBLE
            } else {
                tvBus.visibility = View.GONE
            }

            if (trip.driver != null) {
                tvDriver.text = "Conductor: ${trip.driver}"
                tvDriver.visibility = View.VISIBLE
            } else {
                tvDriver.visibility = View.GONE
            }

            tvDate.text = trip.date
            tvTimeInfo.text = trip.timeInfo

            // Lógica de estados (Completado / Incidente)
            when (trip.status) {
                TripStatus.COMPLETADO -> {
                    tvStatusBadge.visibility = View.VISIBLE
                    tvStatusBadge.text = "Completado"
                    tvStatusBadge.background = ContextCompat.getDrawable(root.context, R.drawable.bg_badge_completed)
                }
                TripStatus.INCIDENTE -> {
                    tvStatusBadge.visibility = View.VISIBLE
                    tvStatusBadge.text = "Incidente"
                    tvStatusBadge.background = ContextCompat.getDrawable(root.context, R.drawable.bg_badge_incident)
                }
                else -> {
                    tvStatusBadge.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount() = trips.size
}
