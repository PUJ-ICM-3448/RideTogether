package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatMensajeAdapter(
    private val mensajes: MutableList<Map<String, Any>>,
    private val miUid: String
) : RecyclerView.Adapter<ChatMensajeAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTexto:  TextView  = view.findViewById(R.id.tvTextoMensaje)
        val tvNombre: TextView  = view.findViewById(R.id.tvNombreMensaje)
        val ivFoto:   ImageView = view.findViewById(R.id.ivFotoMensaje)
    }

    override fun getItemViewType(position: Int): Int {
        val senderId = mensajes[position]["senderId"] as? String ?: ""
        return if (senderId == miUid) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = if (viewType == 1) R.layout.item_mensaje_propio
        else R.layout.item_mensaje_otro
        return VH(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg      = mensajes[position]
        val texto    = msg["texto"]      as? String ?: ""
        val nombre   = msg["senderName"] as? String ?: ""
        val imageUrl = msg["imageUrl"]   as? String ?: ""

        holder.tvNombre.text = nombre

        if (imageUrl.isNotEmpty()) {
            holder.tvTexto.visibility = View.GONE
            holder.ivFoto.visibility  = View.VISIBLE
            Glide.with(holder.itemView.context).load(imageUrl).into(holder.ivFoto)
        } else {
            holder.tvTexto.visibility = View.VISIBLE
            holder.ivFoto.visibility  = View.GONE
            holder.tvTexto.text       = texto
        }
    }
    override fun getItemCount() = mensajes.size
}