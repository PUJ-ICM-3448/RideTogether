package com.example.proyecto

import android.view.LayoutInflater; import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ItemIncidentBinding

class HistorialIncidentesAdapter(private val incidents: MutableList<IncidentItem>) :
    RecyclerView.Adapter<HistorialIncidentesAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemIncidentBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemIncidentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val i = incidents[position]
        holder.binding.tvIncidentTitle.text = i.title
        holder.binding.tvIncidentDate.text  = i.date
        holder.binding.tvIncidentDesc.text  = i.description
        holder.binding.tvIncidentAuthor.text = i.author
    }
    override fun getItemCount() = incidents.size
}
