package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de regreso funcional
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Navegación a la pantalla de Notificaciones (NUEVO)
        // Se activa al presionar el ícono de la campana en la parte superior derecha
        binding.ivNotification.setOnClickListener {
            val intent = Intent(this, NotificacionesActivity::class.java)
            startActivity(intent)
        }

        // Navegación a la pantalla de Editar Perfil
        binding.ivProfile.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }

        // Navegación a RutaCompletaActivity
        binding.btnViewRoute.setOnClickListener {
            val intent = Intent(this, RutaCompletaActivity::class.java)
            startActivity(intent)
        }

        // Navegación a HistorialViajesActivity
        binding.btnHistory.setOnClickListener {
            val intent = Intent(this, HistorialViajesActivity::class.java)
            startActivity(intent)
        }
    }
}
