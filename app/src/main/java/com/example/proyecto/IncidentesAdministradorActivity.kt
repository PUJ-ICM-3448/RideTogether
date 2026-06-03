package com.example.proyecto

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityIncidentesAdministradorBinding

class IncidentesAdministradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncidentesAdministradorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentesAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar flecha atrás
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Configurar botones "Ver" con Toasts mock
        binding.btnVer1.setOnClickListener {
            Toast.makeText(this, "Detalles de: Se dejó la mochila olvidada", Toast.LENGTH_SHORT).show()
        }

        binding.btnVer2.setOnClickListener {
            Toast.makeText(this, "Detalles de: Discusión entre estudiantes", Toast.LENGTH_SHORT).show()
        }

        binding.btnVer3.setOnClickListener {
            Toast.makeText(this, "Detalles de: Se sintió mal durante el trayecto", Toast.LENGTH_SHORT).show()
        }
    }
}
