package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.ActivityPantallaPrincipalAdministradorBinding

class PantallaPrincipalAdministradorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPantallaPrincipalAdministradorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaPrincipalAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { finish() }
        cargarAdmin()
        binding.ivAdminAvatar.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }
        binding.tvGreeting.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }
        binding.btnEditarPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }
        binding.cardRutas.setOnClickListener {
            startActivity(Intent(this, VerRutasAdministradorActivity::class.java))
        }
        binding.cardEstudiantes.setOnClickListener {
            startActivity(Intent(this, EstudiantesAdministradorActivity::class.java))
        }
        binding.cardIncidentes.setOnClickListener {
            startActivity(Intent(this, IncidentesAdministradorActivity::class.java))
        }
        binding.cardNotificaciones.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }
    }
    private fun cargarAdmin() {
        val uid = FirebaseRepository.usuarioActual()?.uid ?: return
        FirebaseRepository.obtenerPerfil(uid,
            onSuccess = { data ->
                val nombre = data["nombre"] as? String ?: "Administrador"
                binding.tvGreeting.text = "Hola, $nombre"
                val fotoUrl = data["fotoUrl"] as? String ?: ""
                if (fotoUrl.isNotEmpty()) {
                    Glide.with(this).load(fotoUrl).circleCrop().into(binding.ivAdminAvatar)
                }
            },
            onError = { }
        )
    }
}