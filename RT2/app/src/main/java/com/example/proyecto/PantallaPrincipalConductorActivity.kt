package com.example.proyecto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.ActivityPantallaPrincipalConductorBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale
import kotlin.math.sqrt

class PantallaPrincipalConductorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityPantallaPrincipalConductorBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor?     = null
    private lateinit var locationManager: LocationManager

    private val paradasSeleccionadas = mutableListOf<Map<String, Any>>()
    private lateinit var paradasAdapter: ParadasAdapter
    private var paradasListener: ListenerRegistration? = null

    private val estudiantesDisponibles = mutableListOf<Map<String, Any>>()

    private var viajeActivo = false
    private var placa       = ""
    private var miNombre    = ""

    private var accelX = 0f; private var accelY = 0f; private var accelZ = 0f
    private var giroX  = 0f; private var giroY  = 0f; private var giroZ  = 0f
    private val UMBRAL       = 15.0
    private var ultimaAlerta = 0L

    private val uid get() = FirebaseRepository.usuarioActual()?.uid ?: ""

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) iniciarGPS()
    }

    private val gpsListener = LocationListener { location ->
        if (uid.isNotEmpty() && viajeActivo) {
            FirebaseRepository.publicarUbicacionConductor(
                uid, location.latitude, location.longitude)
        }
        val kmh = if (location.hasSpeed() && location.speed > 0.8f)
            location.speed * 3.6 else 0.0
        binding.tvVelocidad.text =
            String.format(Locale.US, "Velocidad GPS: %.1f km/h", kmh)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPantallaPrincipalConductorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        binding.btnNotificaciones.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }

        binding.btnReportarIncidente.setOnClickListener {
            startActivity(Intent(this, ReportarIncidenteActivity::class.java))
        }

        cargarPerfil()
        cargarEstudiantesDisponibles()
        setupSensores()
        pedirUbicacion()
        setupParadasAdapter()

        binding.btnAgregarParada.setOnClickListener { mostrarSelectorEstudiantes() }
        binding.btnIniciarViaje.setOnClickListener  { iniciarViaje() }
        binding.btnFinalizarViaje.setOnClickListener { finalizarViaje() }
    }

    private fun cargarPerfil() {
        if (uid.isEmpty()) return
        FirebaseRepository.obtenerPerfil(uid,
            onSuccess = { data ->
                miNombre = data["nombre"] as? String ?: "Conductor"
                placa    = data["placa"]  as? String ?: ""
                binding.toolbar.title = "Hola, $miNombre"
            },
            onError = { }
        )
    }

    private fun cargarEstudiantesDisponibles() {
        FirebaseFirestore.getInstance().collection("estudiantes")
            .whereEqualTo("conductorUid", uid)
            .get()
            .addOnSuccessListener { snap ->
                estudiantesDisponibles.clear()
                estudiantesDisponibles.addAll(snap.documents.mapNotNull { it.data })
                if (estudiantesDisponibles.isEmpty()) {
                    FirebaseFirestore.getInstance().collection("estudiantes").get()
                        .addOnSuccessListener { all ->
                            estudiantesDisponibles.addAll(all.documents.mapNotNull { it.data })
                        }
                }
            }
    }

    private fun setupParadasAdapter() {
        paradasAdapter = ParadasAdapter(
            mutableListOf<Pair<String, Map<String, Any>>>()
        ) { paradaId ->
            FirebaseRepository.marcarParadaCompletada(uid, paradaId) {
                val parada   = paradasSeleccionadas.find { it["paradaId"] == paradaId }
                val tutorUid = parada?.get("tutorUid") as? String ?: ""
                val dir      = parada?.get("direccionParada") as? String ?: "parada"
                if (tutorUid.isNotEmpty()) {
                    FirebaseRepository.crearNotificacion(tutorUid,
                        "El bus con placas $placa llegó a la parada: $dir",
                        "parada_completada")
                }
            }
        }
        binding.rvParadas.adapter = paradasAdapter
        escucharParadasFirestore()
    }
    private fun escucharParadasFirestore() {
        if (uid.isEmpty()) return
        paradasListener = FirebaseRepository.escucharParadas(uid) { lista: List<Pair<String, Map<String, Any>>> ->
            val adaptadas = lista.toMutableList()
            (binding.rvParadas.adapter as? ParadasAdapter)?.let { adapter ->
                adapter.paradas.clear()
                adapter.paradas.addAll(adaptadas)
                adapter.notifyDataSetChanged()
            }
            val completadas = lista.count { it.second["completada"] == true }
            binding.tvEstadoParadas.text =
                "${lista.size} paradas (${completadas} completadas)"
        }
    }
    private fun mostrarSelectorEstudiantes() {
        if (estudiantesDisponibles.isEmpty()) {
            Toast.makeText(this,
                "No hay estudiantes disponibles. El administrador debe registrarlos primero.",
                Toast.LENGTH_LONG).show()
            return
        }
        val nombres = estudiantesDisponibles.map { data ->
            val nombre = data["nombre"] as? String ?: "Sin nombre"
            val dir    = data["direccionParada"] as? String ?: "Sin dirección"
            "$nombre — $dir"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Seleccionar estudiante como parada")
            .setItems(nombres) { _, index ->
                val estudiante = estudiantesDisponibles[index]
                agregarEstudianteComoParada(estudiante)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun agregarEstudianteComoParada(estudiante: Map<String, Any>) {
        val nombre    = estudiante["nombre"]          as? String ?: ""
        val direccion = estudiante["direccionParada"] as? String ?: ""
        val tutorUid  = estudiante["tutorUid"]        as? String ?: ""

        val datosParada = mapOf(
            "nombre"          to nombre,
            "direccion"       to direccion,
            "direccionParada" to direccion,
            "tutorUid"        to tutorUid,
            "orden"           to paradasSeleccionadas.size,
            "completada"      to false,
            "timestamp"       to System.currentTimeMillis()
        )

        FirebaseRepository.guardarParada(uid, datosParada,
            onSuccess = { paradaId ->
                val conId = datosParada.toMutableMap()
                conId["paradaId"] = paradaId
                paradasSeleccionadas.add(conId)
                Toast.makeText(this, "Parada agregada: $nombre", Toast.LENGTH_SHORT).show()
                if (tutorUid.isNotEmpty()) {
                    FirebaseRepository.crearNotificacion(tutorUid,
                        "El conductor con placas $placa te agregó como parada del viaje",
                        "parada_agregada")
                }
            },
            onError = { msg ->
                Toast.makeText(this, "Error: $msg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun iniciarViaje() {
        if (paradasSeleccionadas.isEmpty()) {
            Toast.makeText(this,
                "Agrega al menos un estudiante como parada antes de iniciar",
                Toast.LENGTH_SHORT).show()
            return
        }
        viajeActivo = true
        binding.tvEstadoViaje.text         = "En progreso"
        binding.btnIniciarViaje.isEnabled  = false
        binding.btnFinalizarViaje.isEnabled = true

        FirebaseRepository.iniciarViaje(uid, placa)

        val tutoresNotificados = mutableSetOf<String>()
        for (parada in paradasSeleccionadas) {
            val tutorUid = parada["tutorUid"] as? String ?: continue
            if (tutorUid.isNotEmpty() && tutoresNotificados.add(tutorUid)) {
                FirebaseRepository.crearNotificacion(tutorUid,
                    "El conductor con placas $placa inició el viaje",
                    "viaje_iniciado")
            }
        }
        Toast.makeText(this,
            "Viaje iniciado — ubicación compartida con tutores",
            Toast.LENGTH_SHORT).show()
    }

    private fun finalizarViaje() {
        viajeActivo = false
        binding.tvEstadoViaje.text         = "Finalizado"
        binding.btnIniciarViaje.isEnabled  = true
        binding.btnFinalizarViaje.isEnabled = false

        FirebaseRepository.finalizarViaje(uid)

        val tutoresNotificados = mutableSetOf<String>()
        for (parada in paradasSeleccionadas) {
            val tutorUid = parada["tutorUid"] as? String ?: continue
            if (tutorUid.isNotEmpty() && tutoresNotificados.add(tutorUid)) {
                FirebaseRepository.crearNotificacion(tutorUid,
                    "El conductor con placas $placa finalizó el viaje",
                    "viaje_finalizado")
            }
        }

        paradasSeleccionadas.clear()
        binding.tvEstadoParadas.text = "0 paradas registradas"
        Toast.makeText(this, "Viaje finalizado", Toast.LENGTH_SHORT).show()
    }

    private fun setupSensores() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]; accelY = event.values[1]; accelZ = event.values[2]
                val mag = sqrt((accelX*accelX + accelY*accelY + accelZ*accelZ).toDouble())
                binding.tvAccelerometer.text = String.format(
                    Locale.US,
                    "Acelerómetro: X=%.1f Y=%.1f Z=%.1f |g|=%.1f",
                    accelX, accelY, accelZ, mag)
                val ahora = System.currentTimeMillis()
                if (mag > UMBRAL && (ahora - ultimaAlerta) > 5000) {
                    ultimaAlerta = ahora
                    binding.tvAlertaFrenazo.visibility = View.VISIBLE
                    binding.tvAlertaFrenazo.postDelayed(
                        { binding.tvAlertaFrenazo.visibility = View.GONE }, 4000)
                    FirebaseRepository.reportarIncidente(uid,
                        "Frenazo automático",
                        "Sensor detectó movimiento brusco",
                        accelX, accelY, accelZ, giroX, giroY, giroZ, {}, {})
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                giroX = event.values[0]; giroY = event.values[1]; giroZ = event.values[2]
                val rot = sqrt((giroX*giroX + giroY*giroY + giroZ*giroZ).toDouble())
                binding.tvGyroscope.text = String.format(
                    Locale.US,
                    "Giroscopio: X=%.2f Y=%.2f Z=%.2f |r|=%.2f rad/s",
                    giroX, giroY, giroZ, rot)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun pedirUbicacion() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) iniciarGPS()
        else permLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun iniciarGPS() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2000L, 5f, gpsListener)
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        if (::locationManager.isInitialized)
            locationManager.removeUpdates(gpsListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        paradasListener?.remove()
    }
}