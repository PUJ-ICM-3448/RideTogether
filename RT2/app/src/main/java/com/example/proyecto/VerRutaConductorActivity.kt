package com.example.proyecto

import android.Manifest
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
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.example.proyecto.databinding.ActivityVerRutaConductorBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.util.Locale

class VerRutaConductorActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityVerRutaConductorBinding
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var speedText     = "Velocidad: 0.0 km/h"
    private var gyroscopeText = "Giroscopio: esperando datos"
    private var currentOrigin: GeoPoint? = null
    private var currentDestination     = GeoPoint(4.710989, -74.072092)
    private var currentDestinationName = "Parada X - Museo Nacional"
    private val mainHandler = Handler(Looper.getMainLooper())
    private val httpClient  = OkHttpClient()
    private var ubicacionListener: ListenerRegistration? = null
    private var paradasListener:   ListenerRegistration? = null
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
            val speedKmH = if (location.hasSpeed() && location.speed > 0.8f)
                location.speed * 3.6 else 0.0
            speedText = String.format(Locale.US, "Velocidad: %.1f km/h", speedKmH)
            updateSensorInfoDisplay()

            val origin = GeoPoint(location.latitude, location.longitude)
            if (currentOrigin == null) {
                drawOriginDestinationAndRoute(origin, currentDestination, currentDestinationName)
            } else {
                updateUserMarker(origin)
            }
            currentOrigin = origin
            val uid = FirebaseRepository.usuarioActual()?.uid ?: return
            FirebaseRepository.publicarUbicacionConductor(uid, location.latitude, location.longitude)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        binding = ActivityVerRutaConductorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        setupMap()
        setupSearch()
        setupSensors()
        validateLocationPermission()
        cargarParadasFirestore()
    }
    private fun cargarParadasFirestore() {
        val uid = FirebaseRepository.usuarioActual()?.uid ?: return
        paradasListener = FirebaseFirestore.getInstance()
            .collection("viajes").document(uid).collection("paradas")
            .orderBy("orden")
            .addSnapshotListener { snap, _ ->
                val lista = snap?.documents?.mapNotNull { doc ->
                    val dir    = doc.getString("direccion") ?: return@mapNotNull null
                    val nombre = doc.getString("nombre")    ?: "Parada"
                    EstudianteRuta(nombre, dir, "Pendiente", android.R.color.holo_orange_dark)
                } ?: emptyList()

                if (lista.isNotEmpty()) {
                    binding.rvStudents.adapter = EstudianteRutaAdapter(lista)
                } else {
                    // Sin paradas en Firebase, mostrar lista vacía
                    binding.rvStudents.adapter = EstudianteRutaAdapter(emptyList())
                }
            }
    }
    private fun updateSensorInfoDisplay() {
        binding.tvSensorInfo.text = "$speedText\n$gyroscopeText"
    }
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) searchLocation(query)
                return true
            }
            override fun onQueryTextChange(newText: String?) = false
        })
    }
    private fun searchLocation(query: String) {
        val url = "https://nominatim.openstreetmap.org/search?q=${
            query.replace(" ", "+")}&format=json&limit=1"
        val request = Request.Builder().url(url)
            .header("User-Agent", packageName).build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body      = it.body?.string().orEmpty()
                    val jsonArray = JSONArray(body)
                    if (jsonArray.length() > 0) {
                        val first = jsonArray.getJSONObject(0)
                        currentDestination     = GeoPoint(
                            first.getDouble("lat"), first.getDouble("lon"))
                        currentDestinationName = first.getString("display_name")
                            .split(",")[0]
                        mainHandler.post {
                            currentOrigin?.let { orig ->
                                drawOriginDestinationAndRoute(
                                    orig, currentDestination, currentDestinationName)
                            }
                        }
                    }
                }
            }
        })
    }
    private fun setupSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        updateSensorInfoDisplay()
    }
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            gyroscopeText = String.format(Locale.US,
                "Giroscopio X: %.1f  Y: %.1f  Z: %.1f",
                event.values[0], event.values[1], event.values[2])
            updateSensorInfoDisplay()
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    private fun setupMap() {
        mapView = binding.mapViewRoute
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(17.0)
    }
    private fun validateLocationPermission() {
        if (hasLocationPermission()) loadCurrentLocationAndRoute()
        else locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    private fun loadCurrentLocationAndRoute() {
        if (!hasLocationPermission()) return
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2000L, 1f, locationListener)
        } catch (e: SecurityException) { showDefaultRoute() }
    }
    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    private fun showDefaultRoute() {
        val bogotaCenter = GeoPoint(4.609710, -74.081750)
        currentOrigin = bogotaCenter
        drawOriginDestinationAndRoute(bogotaCenter, currentDestination, currentDestinationName)
    }
    private fun updateUserMarker(point: GeoPoint) {
        val markers      = mapView.overlays.filterIsInstance<Marker>()
        val driverMarker = markers.find { it.title == "Ubicación actual del conductor" }
        if (driverMarker != null) {
            driverMarker.position = point
            mapView.invalidate()
        }
    }
    private fun drawOriginDestinationAndRoute(
        origin: GeoPoint, destination: GeoPoint, destinationName: String
    ) {
        mapView.overlays.clear()
        addMarker(origin, "Ubicación actual del conductor")
        addMarker(destination, destinationName)
        mapView.controller.animateTo(origin)

        requestRouteWithOsrm(origin, destination) { routePoints, distanceKm ->
            if (routePoints.isNotEmpty()) {
                val polyline = Polyline().apply {
                    setPoints(routePoints)
                    outlinePaint.color       = Color.rgb(0, 94, 184)
                    outlinePaint.strokeWidth = 12f
                }
                mapView.overlays.add(polyline)
                binding.tvRouteInfo.text = String.format(
                    Locale.US, "Ruta a %s · %.2f km", destinationName, distanceKm)
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
        origin: GeoPoint, destination: GeoPoint,
        onResult: (List<GeoPoint>, Double) -> Unit
    ) {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${origin.longitude},${origin.latitude};" +
                "${destination.longitude},${destination.latitude}" +
                "?overview=full&geometries=geojson"
        httpClient.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post { onResult(emptyList(), 0.0) }
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful || body.isBlank()) {
                    mainHandler.post { onResult(emptyList(), 0.0) }
                    return
                }
                try {
                    val json   = JSONObject(body)
                    val routes = json.optJSONArray("routes") ?: return
                    val route  = routes.getJSONObject(0)
                    val dist   = route.optDouble("distance", 0.0) / 1000.0
                    val coords = route.getJSONObject("geometry").getJSONArray("coordinates")
                    val points = mutableListOf<GeoPoint>()
                    for (i in 0 until coords.length()) {
                        val c = coords.getJSONArray(i)
                        points.add(GeoPoint(c.getDouble(1), c.getDouble(0)))
                    }
                    mainHandler.post { onResult(points, dist) }
                } catch (_: Exception) {
                    mainHandler.post { onResult(emptyList(), 0.0) }
                }
            }
        })
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        gyroscope?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
        sensorManager.unregisterListener(this)
        if (::locationManager.isInitialized && hasLocationPermission()) {
            locationManager.removeUpdates(locationListener)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        ubicacionListener?.remove()
        paradasListener?.remove()
    }
}