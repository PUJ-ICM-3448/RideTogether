package com.example.proyecto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemIncidentBinding

class HistorialIncidentesAdapter(private val incidents: List<IncidentItem>) :
    RecyclerView.Adapter<HistorialIncidentesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemIncidentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIncidentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val incident = incidents[position]
        with(holder.binding) {
            tvIncidentTitle.text = incident.title
            tvIncidentDate.text = incident.date
            tvIncidentDesc.text = incident.description
            tvIncidentAuthor.text = incident.author
        }
    }

    override fun getItemCount() = incidents.size
}
