package com.example.proyecto

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityReportarIncidenteBinding

class ReportarIncidenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportarIncidenteBinding
    private var imageUri: Uri? = null

    // Activity Result API para la Galería
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            binding.ivPreview.setImageURI(it)
        }
    }

    // Activity Result API para la Cámara
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                binding.ivPreview.setImageBitmap(it)
                // En una app real, guardaríamos el bitmap en un archivo y obtendríamos su Uri
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportarIncidenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar azul
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Cargar datos del estudiante (Mock o desde Intent)
        setupStudentData()

        // Configurar Dropdown de incidentes
        setupIncidentDropdown()

        // Botones de Imagen
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnCamera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(takePictureIntent)
        }

        // Botón Enviar
        binding.btnSendReport.setOnClickListener {
            validateAndSend()
        }
    }

    private fun setupStudentData() {
        val name = intent.getStringExtra("STUDENT_NAME") ?: "Camila Ruiz"
        val route = intent.getStringExtra("STUDENT_ROUTE") ?: "Central"
        val status = intent.getStringExtra("STUDENT_STATUS") ?: "No asistió"

        binding.tvStudentName.text = name
        binding.tvRouteLabel.text = "Ruta: $route"
        binding.tvStatus.text = "Estado: $status"
    }

    private fun setupIncidentDropdown() {
        val options = arrayOf(
            "Avería, daño en el vehículo",
            "Ausencia del estudiante en la recogida",
            "Comportamental y/o falta de respeto",
            "Agentes externos al bus escolar",
            "Inconvenientes de salud"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        binding.actvIncidentType.setAdapter(adapter)
    }

    private fun validateAndSend() {
        val type = binding.actvIncidentType.text.toString()
        val description = binding.etDescription.text.toString()

        if (type.isEmpty()) {
            binding.tilIncidentType.error = "Seleccione un tipo"
            return
        } else { binding.tilIncidentType.error = null }

        if (description.trim().isEmpty()) {
            binding.etDescription.error = "Escriba una descripción"
            return
        }

        // Simulación de envío exitoso
        Toast.makeText(this, "Incidente reportado correctamente", Toast.LENGTH_LONG).show()
        finish()
    }
}
