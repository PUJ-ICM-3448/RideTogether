package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityPantallaPrincipalConductorBinding

class PantallaPrincipalConductorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPantallaPrincipalConductorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaPrincipalConductorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la Toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar el RecyclerView con los datos mock
        setupRouteList()
    }

    private fun setupRouteList() {
        // DATOS MOCK sugeridos en el requerimiento
        val routes = listOf(
            RutaConductor(
                nombreRuta = "Ruta norte 3",
                busAsignado = "Bus ABC 123",
                estado = "Finalizado / 25 estudiantes",
                colorEstado = android.R.color.holo_green_dark
            ),
            RutaConductor(
                nombreRuta = "Ruta centro 1",
                busAsignado = "Bus ABC 123",
                estado = "En progreso / 21 estudiantes",
                colorEstado = android.R.color.holo_orange_dark
            ),
            RutaConductor(
                nombreRuta = "Ruta centro 3",
                busAsignado = "Bus ABC 123",
                estado = "En espera / 15 estudiantes",
                colorEstado = android.R.color.holo_red_dark
            ),
            RutaConductor(
                nombreRuta = "Ruta nororiente 5",
                busAsignado = "Bus ABC 123",
                estado = "Finalizado / 25 estudiantes",
                colorEstado = android.R.color.holo_green_dark
            )
        )

        val adapter = RutaConductorAdapter(routes)
        binding.rvRoutes.adapter = adapter
    }
}
