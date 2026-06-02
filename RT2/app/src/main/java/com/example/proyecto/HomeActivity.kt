package com.example.proyecto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.ActivityHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private var conductorListener:  ListenerRegistration? = null
    private var paradasListener:    ListenerRegistration? = null
    private var notifListener:      ListenerRegistration? = null
    private var conductorMarker:    Marker? = null
    private var conductorUidActual: String  = ""
    private val paradasTutor = mutableListOf<Map<String, Any>>()
    private lateinit var paradasAdapter: ParadasTutorAdapter
    private val mainHandler = Handler(Looper.getMainLooper())
    private val httpClient  = OkHttpClient()
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) iniciarGPS()
    }
    private val gpsListener = LocationListener { _ -> }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext))
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupMap()
        setupParadasAdapter()
        cargarPerfil()
        pedirUbicacion()
        escucharNotificaciones()
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
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
    private fun cargarPerfil() {
        val uid = FirebaseRepository.usuarioActual()?.uid ?: return
        FirebaseRepository.obtenerPerfil(uid,
            onSuccess = { data ->
                val nombre = data["nombre"] as? String ?: ""
                binding.tvWelcome.text = "Hola, $nombre"
                val fotoUrl = data["fotoUrl"] as? String ?: ""
                if (fotoUrl.isNotEmpty())
                    Glide.with(this).load(fotoUrl).circleCrop().into(binding.ivProfile)
                cargarEstudianteDelTutor(uid)
            },
            onError = { }
        )
    }
    private fun cargarEstudianteDelTutor(tutorUid: String) {
        FirebaseFirestore.getInstance().collection("estudiantes")
            .whereEqualTo("tutorUid", tutorUid)
            .get()
            .addOnSuccessListener { snap ->
                val est = snap.documents.firstOrNull()?.data ?: return@addOnSuccessListener
                val nombre       = est["nombre"]       as? String ?: ""
                val ruta         = est["ruta"]         as? String ?: ""
                val conductorUid = est["conductorUid"] as? String ?: ""

                binding.tvStudentName.text = nombre
                binding.tvRoute.text       = "Ruta: $ruta"
                binding.tvStatus.text      = "Estado: En camino"

                if (conductorUid.isNotEmpty()) {
                    conductorUidActual = conductorUid
                    cargarDatosConductor(conductorUid)
                    suscribirUbicacionConductor(conductorUid)
                    escucharParadasEnTiempoReal(conductorUid)
                }
            }
    }
    private fun cargarDatosConductor(conductorUid: String) {
        FirebaseRepository.obtenerPerfil(conductorUid,
            onSuccess = { data ->
                val nombre = data["nombre"] as? String ?: "Conductor"
                val placa  = data["placa"]  as? String ?: ""
                binding.tvDriver.text = "Conductor: $nombre"
                binding.tvBus.text    = if (placa.isNotEmpty()) "Placa: $placa" else ""
            },
            onError = { }
        )
    }
    private fun suscribirUbicacionConductor(conductorUid: String) {
        conductorListener?.remove()
        conductorListener = FirebaseRepository.escucharUbicacionConductor(conductorUid) { lat, lng ->
            val punto = GeoPoint(lat, lng)
            if (conductorMarker == null) {
                conductorMarker = Marker(mapView).apply {
                    title = "Conductor"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(conductorMarker)
            }
            conductorMarker?.position = punto
            mapView.controller.animateTo(punto)
            mapView.invalidate()
        }
    }
    private fun escucharParadasEnTiempoReal(conductorUid: String) {
        paradasListener?.remove()
        paradasListener = FirebaseFirestore.getInstance()
            .collection("viajes").document(conductorUid)
            .collection("paradas")
            .orderBy("orden")
            .addSnapshotListener { snap, _ ->
                val lista = snap?.documents?.mapNotNull { it.data } ?: emptyList()
                paradasTutor.clear()
                paradasTutor.addAll(lista)
                paradasAdapter.notifyDataSetChanged()

                val completadas = lista.count { it["completada"] == true }
                binding.tvEstadoViaje.text = when {
                    lista.isEmpty()           -> "Esperando que el conductor inicie el viaje"
                    completadas == lista.size -> "✓ Todas las paradas completadas"
                    else -> "En progreso — $completadas/${lista.size} paradas completadas"
                }

                if (lista.isNotEmpty()) dibujarPolylineParadas(lista)
            }
    }
    private fun dibujarPolylineParadas(paradas: List<Map<String, Any>>) {
        mapView.overlays.removeIf { it is Polyline }
        val direcciones = paradas.mapNotNull { it["direccion"] as? String }
        if (direcciones.isEmpty()) return
        val puntosGeo  = mutableListOf<GeoPoint>()
        var pendientes = direcciones.size
        direcciones.forEachIndexed { index, dir ->
            geocodificarDireccion(dir) { punto ->
                puntosGeo.add(punto)
                val completada = paradas[index]["completada"] as? Boolean ?: false
                val marker = Marker(mapView).apply {
                    position = punto
                    title    = paradas[index]["nombre"] as? String ?: "Parada ${index + 1}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    alpha    = if (completada) 0.5f else 1.0f
                }
                mapView.overlays.add(marker)
                pendientes--
                if (pendientes == 0 && puntosGeo.size >= 2) {
                    trazarRutaMultiPunto(puntosGeo)
                }
                mapView.invalidate()
            }
        }
    }
    private fun trazarRutaMultiPunto(puntos: List<GeoPoint>) {
        if (puntos.size < 2) return
        val coords = puntos.joinToString(";") { "${it.longitude},${it.latitude}" }
        val url    = "https://router.project-osrm.org/route/v1/driving/$coords" +
                "?overview=full&geometries=geojson"
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string() ?: return
                    try {
                        val json    = JSONObject(body)
                        val routes  = json.optJSONArray("routes") ?: return
                        if (routes.length() == 0) return
                        val coords2 = routes.getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONArray("coordinates")
                        val routePoints = mutableListOf<GeoPoint>()
                        for (i in 0 until coords2.length()) {
                            val c = coords2.getJSONArray(i)
                            routePoints.add(GeoPoint(c.getDouble(1), c.getDouble(0)))
                        }
                        mainHandler.post {
                            mapView.overlays.removeIf { it is Polyline }
                            val polyline = Polyline().apply {
                                setPoints(routePoints)
                                outlinePaint.color       = Color.rgb(0, 94, 184)
                                outlinePaint.strokeWidth = 8f
                            }
                            mapView.overlays.add(polyline)
                            mapView.invalidate()
                        }
                    } catch (_: Exception) {}
                }
            }
        })
    }
    private fun geocodificarDireccion(direccion: String, onResult: (GeoPoint) -> Unit) {
        val url = "https://nominatim.openstreetmap.org/search?q=${
            android.net.Uri.encode(direccion)
        }&format=json&limit=1&countrycodes=co"
        Thread {
            try {
                val request  = Request.Builder().url(url)
                    .header("User-Agent", "RideTogether/1.0").build()
                val response = httpClient.newCall(request).execute()
                val body     = response.body?.string() ?: return@Thread
                val arr      = org.json.JSONArray(body)
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    val lat = obj.getDouble("lat")
                    val lng = obj.getDouble("lon")
                    mainHandler.post { onResult(GeoPoint(lat, lng)) }
                }
            } catch (_: Exception) {}
        }.start()
    }
    private fun escucharNotificaciones() {
        val uid = FirebaseRepository.usuarioActual()?.uid ?: return
        notifListener = FirebaseRepository.escucharNotificaciones(uid) { _, mensaje ->
            Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.primary_blue))
                .setTextColor(getColor(android.R.color.white))
                .show()
        }
    }
    private fun setupParadasAdapter() {
        paradasAdapter = ParadasTutorAdapter(paradasTutor)
        binding.rvParadasTutor.adapter = paradasAdapter
    }
    private fun setupMap() {
        mapView = binding.mapViewHome
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
        mapView.controller.setCenter(GeoPoint(4.6097, -74.0817))
    }
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
                LocationManager.GPS_PROVIDER, 3000L, 10f, gpsListener)
        } catch (e: SecurityException) { e.printStackTrace() }
    }
    override fun onResume()  { super.onResume();  mapView.onResume() }
    override fun onPause()   {
        super.onPause()
        mapView.onPause()
        if (::locationManager.isInitialized) locationManager.removeUpdates(gpsListener)
    }
    override fun onDestroy() {
        super.onDestroy()
        conductorListener?.remove()
        paradasListener?.remove()
        notifListener?.remove()
    }
}