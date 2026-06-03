package com.example.proyecto

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityVerRutasAdministradorBinding
import com.google.firebase.firestore.ListenerRegistration
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class VerRutasAdministradorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerRutasAdministradorBinding
    private var conductorListener: ListenerRegistration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        binding = ActivityVerRutasAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        setupMapa()
        escucharConductores()
    }
    private fun setupMapa() {
        binding.mapViewRutas.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapViewRutas.setMultiTouchControls(true)
        binding.mapViewRutas.controller.setZoom(13.0)
        binding.mapViewRutas.controller.setCenter(GeoPoint(4.6097, -74.0817))
    }
    private fun escucharConductores() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        conductorListener = db.collection("ubicaciones")
            .addSnapshotListener { snap, _ ->
                binding.mapViewRutas.overlays.clear()
                snap?.documents?.forEach { doc ->
                    val lat = doc.getDouble("lat") ?: return@forEach
                    val lng = doc.getDouble("lng") ?: return@forEach
                    val conductorUid = doc.id
                    val marker = Marker(binding.mapViewRutas).apply {
                        position = GeoPoint(lat, lng)
                        title    = "Conductor activo"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    binding.mapViewRutas.overlays.add(marker)
                }
                binding.mapViewRutas.invalidate()
            }
    }
    override fun onResume()  { super.onResume();  binding.mapViewRutas.onResume() }
    override fun onPause()   { super.onPause();   binding.mapViewRutas.onPause() }
    override fun onDestroy() { super.onDestroy(); conductorListener?.remove() }
}