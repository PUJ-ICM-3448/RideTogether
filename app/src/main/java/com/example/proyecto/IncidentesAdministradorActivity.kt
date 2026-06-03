package com.example.proyecto

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityIncidentesAdministradorBinding
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidentesAdministradorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncidentesAdministradorBinding
    private var listener: ListenerRegistration? = null
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val incidentesData = mutableListOf<Map<String, Any>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentesAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { finish() }
        binding.btnVer1.setOnClickListener { mostrarDetalle(0) }
        binding.btnVer2.setOnClickListener { mostrarDetalle(1) }
        binding.btnVer3.setOnClickListener { mostrarDetalle(2) }

        escucharIncidentes()
    }
    private fun escucharIncidentes() {
        listener = FirebaseRepository.escucharTodosLosIncidentes { lista ->
            incidentesData.clear()
            incidentesData.addAll(
                lista.sortedByDescending { it["timestamp"] as? Long ?: 0L }
            )
        }
    }
    private fun mostrarDetalle(index: Int) {
        val data = incidentesData.getOrNull(index)
        if (data == null) {
            Toast.makeText(this, "No hay incidente registrado aquí", Toast.LENGTH_SHORT).show()
            return
        }
        val tipo  = data["tipo"] as? String ?: "Incidente"
        val desc  = data["descripcion"] as? String ?: ""
        val ts    = data["timestamp"] as? Long ?: 0L
        val fecha = if (ts > 0) sdf.format(Date(ts)) else "Fecha desconocida"
        Toast.makeText(this, "$tipo\n$fecha\n$desc", Toast.LENGTH_LONG).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}