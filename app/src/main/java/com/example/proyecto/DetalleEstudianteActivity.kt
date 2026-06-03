package com.example.proyecto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityDetalleEstudianteBinding

class DetalleEstudianteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEstudianteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEstudianteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Acción de Llamada
        binding.ivCallTutor.setOnClickListener {
            val phone = binding.tvTutorPhone.text.toString()
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            startActivity(intent)
        }

        // Acción de Correo
        binding.ivEmailTutor.setOnClickListener {
            val email = binding.tvTutorEmail.text.toString()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Contacto desde RideTogether")
            }
            startActivity(intent)
        }
    }
}
