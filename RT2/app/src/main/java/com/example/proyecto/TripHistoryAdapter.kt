package com.example.proyecto

import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup
import androidx.core.content.ContextCompat; import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemTripHistoryBinding
class TripHistoryAdapter(private val trips: MutableList<TripHistory>) :
    RecyclerView.Adapter<TripHistoryAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemTripHistoryBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTripHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val t = trips[position]
        with(holder.binding) {
            tvStudentName.text = t.studentName
            tvRoute.text = "Ruta: ${t.route}"
            tvBus.text = "Bus: ${t.bus ?: ""}"; tvBus.visibility = if (t.bus != null) View.VISIBLE else View.GONE
            tvDriver.text = "Conductor: ${t.driver ?: ""}"; tvDriver.visibility = if (t.driver != null) View.VISIBLE else View.GONE
            tvDate.text = t.date; tvTimeInfo.text = t.timeInfo
            when (t.status) {
                TripStatus.COMPLETADO -> { tvStatusBadge.visibility = View.VISIBLE; tvStatusBadge.text = "Completado"; tvStatusBadge.background = ContextCompat.getDrawable(root.context, R.drawable.bg_badge_completed) }
                TripStatus.INCIDENTE  -> { tvStatusBadge.visibility = View.VISIBLE; tvStatusBadge.text = "Incidente";  tvStatusBadge.background = ContextCompat.getDrawable(root.context, R.drawable.bg_badge_incident) }
                else -> tvStatusBadge.visibility = View.GONE
            }
        }
    }
    override fun getItemCount() = trips.size
}
