package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityHistorialViajesBinding

class HistorialViajesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialViajesBinding
    private var nombreEstudiante = "Estudiante"
    private val todos = mutableListOf<TripHistory>()
    private val lista = mutableListOf<TripHistory>()
    private lateinit var adapter: TripHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialViajesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        val uid = FirebaseRepository.usuarioActual()?.uid ?: ""
        if (uid.isNotEmpty()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("estudiantes")
                .whereEqualTo("tutorUid", uid)
                .get()
                .addOnSuccessListener { snap ->
                    nombreEstudiante = snap.documents.firstOrNull()
                        ?.getString("nombre") ?: "Estudiante"
                    cargarHistorial()
                }
                .addOnFailureListener { cargarHistorial() }
        } else {
            cargarHistorial()
        }
        binding.btnFilter.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Filtrar viajes por")
                .setItems(arrayOf("Todos", "Completados", "Con incidente", "Sin estado")) { _, which ->
                    val filtrados = when (which) {
                        1    -> todos.filter { it.status == TripStatus.COMPLETADO }
                        2    -> todos.filter { it.status == TripStatus.INCIDENTE }
                        3    -> todos.filter { it.status == null }
                        else -> todos
                    }
                    lista.clear()
                    lista.addAll(filtrados)
                    adapter.notifyDataSetChanged()
                }.show()
        }
        binding.btnIncidentHistory.setOnClickListener {
            startActivity(Intent(this, HistorialIncidentesActivity::class.java))
        }
    }
    private fun cargarHistorial() {
        todos.clear()
        todos.addAll(listOf(
            TripHistory(nombreEstudiante, "Norte 3", "ABC 123 (12)", "Luis Martinez",
                "Hoy", null, ""),
            TripHistory(nombreEstudiante, "Norte 3", "ABC 123 (12)", "Luis Martinez",
                "Ayer", TripStatus.COMPLETADO, "Recogida 7:15am - Entregada 7:45am"),
            TripHistory(nombreEstudiante, "Norte 3", "ABC 123 (12)", "Luis Martinez",
                "14 de Marzo 2026", TripStatus.INCIDENTE, "Recogida 7:15am - Entregada 7:45am"),
            TripHistory(nombreEstudiante, "Norte 3", null, null,
                "13 de Marzo, 2026", null, "Salida 3:15pm - Llegada 4:00pm"),
            TripHistory(nombreEstudiante, "Norte 3", null, null,
                "12 de Marzo, 2026", null, "Salida 3:15pm - Llegada 4:10pm"),
            TripHistory(nombreEstudiante, "Norte 3", "ABC 123 (12)", "Luis Martinez",
                "11 de Marzo, 2026", TripStatus.COMPLETADO, "Recogida 7:20am - Entregada 7:50am"),
            TripHistory(nombreEstudiante, "Norte 3", "ABC 123 (12)", "Luis Martinez",
                "10 de Marzo, 2026", TripStatus.COMPLETADO, "Recogida 7:15am - Entregada 7:45am")
        ))
        lista.clear()
        lista.addAll(todos)
        adapter = TripHistoryAdapter(lista)
        binding.rvHistory.adapter = adapter
    }
}