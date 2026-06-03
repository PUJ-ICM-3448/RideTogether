package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityEditarPerfilBinding

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la Toolbar (Botón volver)
        binding.toolbar.setNavigationOnClickListener {
            // Regresa a la pantalla anterior
            finish()
        }

        // Simulación de "Guardar"
        binding.btnSave.setOnClickListener {
            // En una app real, aquí se enviarían los datos a un servidor o base de datos
            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            finish() // Regresa al home después de guardar
        }

        // Configuración del botón "Cerrar sesión"
        binding.btnLogoutContainer.setOnClickListener {
            // Cerramos la sesión navegando al Login o MainActivity (según flujo de la app)
            // Usamos flags para limpiar el stack de actividades y que no pueda volver atrás
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Mock: Simular clic en cambiar foto
        binding.ivProfileCamera.setOnClickListener {
            Toast.makeText(this, "Abrir galería o cámara", Toast.LENGTH_SHORT).show()
        }

        setupMockData()
    }

    private fun setupMockData() {
        // Precargamos los datos sugeridos en el requerimiento
        binding.etName.setText("Carlos Guzman")
        binding.etEmail.setText("Carlosguzman09@eamil.com")
        binding.etPhone.setText("300 000 1111")
    }
}
