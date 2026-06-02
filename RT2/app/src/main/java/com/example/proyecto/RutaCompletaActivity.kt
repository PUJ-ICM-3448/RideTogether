package com.example.proyecto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.proyecto.databinding.ActivityRutaCompletaBinding
import com.google.firebase.firestore.ListenerRegistration
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.util.Locale

class RutaCompletaActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityRutaCompletaBinding
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private val paradaX       = GeoPoint(4.710989, -74.072092)
    private val paradaXNombre = "Parada X - Museo Nacional"
    private var conductorListener: ListenerRegistration? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val httpClient  = OkHttpClient()
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) loadCurrentLocationAndRoute()
        else {
            Toast.makeText(this, "Se requiere permiso de ubicación", Toast.LENGTH_LONG).show()
            showDefaultRoute()
        }
    }
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateSpeed(location)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        binding = ActivityRutaCompletaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        setupMap()
        setupSensors()
        validateLocationPermission()
        binding.btnShareRoute.setOnClickListener { shareRoute() }
        val miUid = FirebaseRepository.usuarioActual()?.uid ?: return
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("estudiantes")
            .whereEqualTo("tutorUid", miUid)
            .get()
            .addOnSuccessListener { snap ->
                val est = snap.documents.firstOrNull()?.data ?: return@addOnSuccessListener
                val conductorUid = est["conductorUid"] as? String ?: ""
                if (conductorUid.isNotEmpty()) {
                    conductorListener = FirebaseRepository.escucharUbicacionConductor(
                        conductorUid
                    ) { lat, lng ->
                        drawOriginDestinationAndRoute(GeoPoint(lat, lng))
                    }
                }
            }
    }
    private fun setupMap() {
        mapView = binding.mapViewRoute
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
        mapView.controller.setCenter(paradaX)
    }
    private fun validateLocationPermission() {
        val fine   = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) loadCurrentLocationAndRoute()
        else locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    private fun loadCurrentLocationAndRoute() {
        if (!hasLocationPermission()) return
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (lastKnown != null) updateSpeed(lastKnown)
            else binding.tvRouteInfo.text = "Esperando ubicación del conductor..."
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 3000L, 5f, locationListener)
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 3000L, 5f, locationListener)
        } catch (e: SecurityException) { showDefaultRoute() }
    }
    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    private fun showDefaultRoute() {
        drawOriginDestinationAndRoute(GeoPoint(4.609710, -74.081750))
    }
    private fun drawOriginDestinationAndRoute(origin: GeoPoint) {
        mapView.overlays.clear()
        addMarker(origin, "Ubicación actual del conductor")
        addMarker(paradaX, paradaXNombre)
        mapView.controller.setCenter(origin)
        binding.tvRouteInfo.text = "Calculando ruta hacia $paradaXNombre..."
        requestRouteWithOsrm(origin, paradaX) { routePoints, distanceKm ->
            mapView.overlays.removeAll { it is Polyline }
            if (routePoints.isNotEmpty()) {
                val polyline = Polyline().apply {
                    setPoints(routePoints)
                    outlinePaint.color       = Color.rgb(0, 94, 184)
                    outlinePaint.strokeWidth = 10f
                }
                mapView.overlays.add(polyline)
                mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(routePoints), true, 80)
                binding.tvRouteInfo.text = String.format(
                    Locale.US, "Ruta a %s · %.2f km", paradaXNombre, distanceKm)
            } else {
                binding.tvRouteInfo.text = "No fue posible calcular la ruta."
            }
            mapView.invalidate()
        }
    }
    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(mapView).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
        }
        mapView.overlays.add(marker)
    }
    private fun requestRouteWithOsrm(
        origin: GeoPoint,
        destination: GeoPoint,
        onResult: (List<GeoPoint>, Double) -> Unit
    ) {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${origin.longitude},${origin.latitude};" +
                "${destination.longitude},${destination.latitude}" +
                "?overview=full&geometries=geojson"

        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post { onResult(emptyList(), 0.0) }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string().orEmpty()
                    if (!it.isSuccessful || body.isBlank()) {
                        mainHandler.post { onResult(emptyList(), 0.0) }
                        return
                    }
                    try {
                        val json   = JSONObject(body)
                        val routes = json.optJSONArray("routes")
                        if (routes == null || routes.length() == 0) {
                            mainHandler.post { onResult(emptyList(), 0.0) }
                            return
                        }
                        val route      = routes.getJSONObject(0)
                        val distanceKm = route.optDouble("distance", 0.0) / 1000.0
                        val coords     = route.getJSONObject("geometry")
                            .getJSONArray("coordinates")
                        val points = mutableListOf<GeoPoint>()
                        for (i in 0 until coords.length()) {
                            val c = coords.getJSONArray(i)
                            points.add(GeoPoint(c.getDouble(1), c.getDouble(0)))
                        }
                        mainHandler.post { onResult(points, distanceKm) }
                    } catch (_: Exception) {
                        mainHandler.post { onResult(emptyList(), 0.0) }
                    }
                }
            }
        })
    }
    private fun updateSpeed(location: Location) {
        val speedKmh = if (location.hasSpeed()) location.speed * 3.6 else 0.0
        binding.tvSpeed.text = String.format(Locale.US, "Velocidad: %.1f km/h", speedKmh)
    }
    private fun setupSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> binding.tvAccelerometer.text = String.format(
                Locale.US, "Acelerómetro X: %.1f  Y: %.1f  Z: %.1f",
                event.values[0], event.values[1], event.values[2])
            Sensor.TYPE_GYROSCOPE -> binding.tvGyroscope.text = String.format(
                Locale.US, "Giroscopio X: %.1f  Y: %.1f  Z: %.1f",
                event.values[0], event.values[1], event.values[2])
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun shareRoute() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,
                "¡Hola! Estoy siguiendo la ruta escolar en RideTogether.")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir ruta vía"))
    }
    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) mapView.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }
    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized) mapView.onPause()
        if (::sensorManager.isInitialized) sensorManager.unregisterListener(this)
        if (::locationManager.isInitialized && hasLocationPermission()) {
            locationManager.removeUpdates(locationListener)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        conductorListener?.remove()
    }
}
