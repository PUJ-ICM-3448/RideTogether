package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.databinding.ActivityNotificacionesBinding

class NotificacionesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificacionesBinding
    private val contactos = mutableListOf<Pair<String, Map<String, Any>>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        val uid = FirebaseRepository.usuarioActual()?.uid ?: return
        FirebaseRepository.obtenerPerfil(uid,
            onSuccess = { data ->
                val miRol = data["rol"] as? String ?: "tutor"
                val rolBuscar = when (miRol) {
                    getString(R.string.conductor), "conductor" -> getString(R.string.tutor)
                    else -> getString(R.string.conductor)
                }
                FirebaseRepository.obtenerUsuariosConRol(rolBuscar) { lista ->
                    contactos.clear()
                    contactos.addAll(lista)
                    setupRecycler()
                }
            },
            onError = { }
        )
    }
    private fun setupRecycler() {
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
                val tvTitle:  TextView = view.findViewById(R.id.tvTitle)
                val tvDesc:   TextView = view.findViewById(R.id.tvDescription)
                val btnChat:  Button   = view.findViewById(R.id.btnChat)
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                VH(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification, parent, false))
            override fun getItemCount() = contactos.size
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val vh = holder as VH
                val (uid, data) = contactos[position]
                val nombre = data["nombre"] as? String ?: "Usuario"
                val rol    = data["rol"]    as? String ?: ""
                vh.tvTitle.text = nombre
                vh.tvDesc.text  = rol
                if (position == 0) {
                    vh.btnChat.isEnabled = true
                    vh.btnChat.text = "Abrir chat"
                    vh.btnChat.setOnClickListener {
                        startActivity(
                            Intent(this@NotificacionesActivity, ChatActivity::class.java).apply {
                                putExtra("otroUid", uid)
                                putExtra("otroNombre", nombre)
                            }
                        )
                    }
                } else {
                    vh.btnChat.isEnabled = false
                    vh.btnChat.text = "Chat"
                }
            }
        }
    }
}