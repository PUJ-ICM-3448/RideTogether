package com.example.proyecto

import android.Manifest; import android.content.Intent; import android.content.pm.PackageManager
import android.graphics.Bitmap; import android.hardware.Sensor; import android.hardware.SensorEvent
import android.hardware.SensorEventListener; import android.hardware.SensorManager
import android.net.Uri; import android.os.Bundle; import android.provider.MediaStore; import android.widget.ArrayAdapter; import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts; import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.proyecto.databinding.ActivityReportarIncidenteBinding

class ReportarIncidenteActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityReportarIncidenteBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var accelX = 0f; private var accelY = 0f; private var accelZ = 0f
    private var giroX = 0f;  private var giroY = 0f;  private var giroZ = 0f
    private val uid get() = FirebaseRepository.usuarioActual()?.uid ?: ""
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) openCamera() }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { binding.ivPreview.setImageURI(it) }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) (result.data?.extras?.get("data") as? Bitmap)?.let { binding.ivPreview.setImageBitmap(it) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportarIncidenteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val name   = intent.getStringExtra("STUDENT_NAME") ?: ""
        val route  = intent.getStringExtra("STUDENT_ROUTE") ?: ""
        val status = intent.getStringExtra("STUDENT_STATUS") ?: ""
        binding.tvStudentName.text = name
        binding.tvRouteLabel.text = "Ruta: $route"
        binding.tvStatus.text = "Estado: $status"
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) openCamera()
            else permLauncher.launch(Manifest.permission.CAMERA)
        }
        setupDropdown()
        binding.btnSendReport.setOnClickListener { enviarReporte() }
    }
    private fun setupDropdown() {
        val options = arrayOf("Avería, daño en el vehículo", "Ausencia del estudiante en la recogida",
            "Comportamental y/o falta de respeto", "Agentes externos al bus escolar", "Inconvenientes de salud")
        binding.actvIncidentType.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, options))
    }
    private fun enviarReporte() {
        val tipo = binding.actvIncidentType.text.toString()
        val desc = binding.etDescription.text.toString().trim()
        if (tipo.isEmpty()) { binding.tilIncidentType.error = "Seleccione un tipo"; return }
        binding.tilIncidentType.error = null
        if (desc.isEmpty()) { binding.etDescription.error = "Escriba una descripción"; return }
        if (uid.isEmpty()) { Toast.makeText(this, "Debe iniciar sesión", Toast.LENGTH_SHORT).show(); return }
        binding.btnSendReport.isEnabled = false
        FirebaseRepository.reportarIncidente(uid, tipo, desc, accelX, accelY, accelZ, giroX, giroY, giroZ,
            onSuccess = { Toast.makeText(this, "Incidente reportado correctamente", Toast.LENGTH_LONG).show(); finish() },
            onError = { msg -> binding.btnSendReport.isEnabled = true; Toast.makeText(this, "Error: $msg", Toast.LENGTH_LONG).show() }
        )
    }
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) cameraLauncher.launch(intent)
    }
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> { accelX = event.values[0]; accelY = event.values[1]; accelZ = event.values[2] }
            Sensor.TYPE_GYROSCOPE     -> { giroX  = event.values[0]; giroY  = event.values[1]; giroZ  = event.values[2] }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onResume() { super.onResume()
        accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscope?.also     { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }
    override fun onPause() { super.onPause(); sensorManager.unregisterListener(this) }
}
