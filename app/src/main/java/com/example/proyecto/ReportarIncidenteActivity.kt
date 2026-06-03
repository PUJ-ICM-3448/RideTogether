package com.example.proyecto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.proyecto.databinding.ActivityReportarIncidenteBinding

class ReportarIncidenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportarIncidenteBinding
    private var imageUri: Uri? = null

    // Launcher para solicitar permisos de cámara
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

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
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportarIncidenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupStudentData()
        setupIncidentDropdown()

        // Botones de Imagen
        binding.btnGallery.setOnClickListener { 
            galleryLauncher.launch("image/*") 
        }
        
        binding.btnCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        binding.btnSendReport.setOnClickListener {
            validateAndSend()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(takePictureIntent)
        } else {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show()
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

        Toast.makeText(this, "Incidente reportado correctamente", Toast.LENGTH_LONG).show()
        finish()
    }
}
