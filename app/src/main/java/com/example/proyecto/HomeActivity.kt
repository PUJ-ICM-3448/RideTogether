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
import android.preference.PreferenceManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.proyecto.databinding.ActivityHomeBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class HomeActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mapView: MapView
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private lateinit var locationManager: LocationManager

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            setupLocationUpdates()
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Velocímetro estable para el tutor (km/h)
            // Filtro de ruido: si la velocidad es menor a 0.8 m/s (~2.8 km/h), marcar como 0
            val speedKmH = if (location.hasSpeed() && location.speed > 0.8f) {
                location.speed * 3.6
            } else {
                0.0
            }
            binding.tvAccelerometer.text = String.format(Locale.US, "Velocidad: %.1f km/h", speedKmH)
            
            updateMapLocation(location)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()
        setupSensors()
        checkLocationPermissions()

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivNotification.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        binding.btnViewRoute.setOnClickListener {
            startActivity(Intent(this, RutaCompletaActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistorialViajesActivity::class.java))
        }
    }

    private fun setupMap() {
        mapView = binding.mapViewHome
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(4.6097, -74.0817))
    }

    private fun setupSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        // Inicializar el texto de velocidad
        binding.tvAccelerometer.text = "Velocidad: 0.0 km/h"
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupLocationUpdates()
        } else {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun setupLocationUpdates() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            // Mayor frecuencia de actualización para el velocímetro
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, locationListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun updateMapLocation(location: Location) {
        val currentPoint = GeoPoint(location.latitude, location.longitude)
        mapView.overlays.clear()
        
        val marker = Marker(mapView)
        marker.position = currentPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Ubicación del Conductor"
        mapView.overlays.add(marker)
        
        mapView.controller.animateTo(currentPoint)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            binding.tvGyroscope.text = String.format(
                Locale.US,
                "Giroscopio X: %.2f Y: %.2f Z: %.2f",
                event.values[0], event.values[1], event.values[2]
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        gyroscope?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        sensorManager.unregisterListener(this)
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
    }
}
