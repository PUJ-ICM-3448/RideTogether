package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityHistorialViajesBinding

class HistorialViajesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialViajesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialViajesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón volver atrás
        binding.toolbar.setNavigationOnClickListener {
            // Regresa a la pantalla anterior
            finish()
        }

        // Navegación con Intent al Historial de Incidentes
        binding.btnIncidentHistory.setOnClickListener {
            val intent = Intent(this, HistorialIncidentesActivity::class.java)
            startActivity(intent)
        }

        setupHistoryList()
    }

    private fun setupHistoryList() {
        // Datos Mock sugeridos
        val historyData = listOf(
            TripHistory(
                studentName = "Sofia Guzman",
                route = "Norte 3",
                bus = "ABC 123 (12)",
                driver = "Luis Martinez",
                date = "Hoy",
                status = null,
                timeInfo = ""
            ),
            TripHistory(
                studentName = "Sofia Guzman",
                route = "Norte 3",
                bus = "ABC 123 (12)",
                driver = "Luis Martinez",
                date = "Ayer",
                status = TripStatus.COMPLETADO,
                timeInfo = "Recogida 7:15am - Entregada 7:45am"
            ),
            TripHistory(
                studentName = "Sofia Guzman",
                route = "Norte 3",
                bus = "ABC 123 (12)",
                driver = "Luis Martinez",
                date = "14 de Marzo 2026",
                status = TripStatus.INCIDENTE,
                timeInfo = "Recogida 7:15am - Entregada 7:45am"
            ),
            TripHistory(
                studentName = "Sofia Guzman",
                route = "Norte 3",
                bus = null,
                driver = null,
                date = "13 de Marzo, 2026",
                status = null,
                timeInfo = "Salida 3:15pm - Llegada 4:00pm"
            ),
            TripHistory(
                studentName = "Sofia Guzman",
                route = "Norte 3",
                bus = null,
                driver = null,
                date = "12 de Marzo, 2026",
                status = null,
                timeInfo = "Salida 3:15pm - Llegada 4:10pm"
            )
        )

        // Configurar RecyclerView con el Adapter
        val adapter = TripHistoryAdapter(historyData)
        binding.rvHistory.adapter = adapter
    }
}
