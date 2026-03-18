package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityRutaCompletaBinding

class RutaCompletaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRutaCompletaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRutaCompletaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del botón Volver en la Toolbar
        binding.toolbar.setNavigationOnClickListener {
            // Cierra la actividad actual y regresa a la anterior
            finish()
        }

        // Configuración del botón "Compartir ruta"
        binding.btnShareRoute.setOnClickListener {
            shareRoute()
        }
    }

    /**
     * Implementa la lógica para compartir la ruta mediante un Intent de envío (SEND)
     */
    private fun shareRoute() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            // Mensaje que se compartirá
            putExtra(Intent.EXTRA_TEXT, "¡Hola! Estoy siguiendo la ruta escolar en RideTogether. La parada actual es: Segunda parada (actual).")
            type = "text/plain"
        }
        // Abre el selector de aplicaciones para compartir
        startActivity(Intent.createChooser(shareIntent, "Compartir ruta vía"))
    }
}
