package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityPantallaPrincipalAdministradorBinding

/**
 * Pantalla Principal para el perfil Administrador.
 * Muestra un dashboard con accesos rápidos y una visión general del estado del sistema.
 */
class PantallaPrincipalAdministradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPantallaPrincipalAdministradorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaPrincipalAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de retroceso en el encabezado
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Manejo de clics en la cuadrícula de accesos rápidos
        setupQuickAccessClicks()
    }

    /**
     * Configura los clics para las opciones de acceso rápido.
     */
    private fun setupQuickAccessClicks() {
        binding.cardRutas.setOnClickListener {
            // Navegación a la pantalla de Ver Rutas para Administrador
            startActivity(Intent(this, VerRutasAdministradorActivity::class.java))
        }

        binding.cardEstudiantes.setOnClickListener {
            // Navegación a la pantalla de Estudiantes para Administrador
            startActivity(Intent(this, EstudiantesAdministradorActivity::class.java))
        }

        binding.cardIncidentes.setOnClickListener {
            // Navegación a la pantalla de Incidentes para Administrador
            startActivity(Intent(this, IncidentesAdministradorActivity::class.java))
        }

        binding.cardNotificaciones.setOnClickListener {
            Toast.makeText(this, "Abrir notificaciones", Toast.LENGTH_SHORT).show()
        }
    }
}
