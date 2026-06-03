package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityHistorialIncidentesBinding

class HistorialIncidentesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialIncidentesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialIncidentesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del botón volver atrás en la Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupIncidentList()
    }

    private fun setupIncidentList() {
        // Datos Mock sugeridos de incidentes
        val incidents = listOf(
            IncidentItem(
                title = "Comportamiento inapropiado",
                date = "13 de Marzo, 2026 - 1:10pm",
                description = "Sofia estuvo peleando durante el trayecto con otra compañera",
                author = "Comentario añadido por Luis Martinez"
            ),
            IncidentItem(
                title = "Avería en el bus",
                date = "10 de Marzo, 2026 - 7:15am",
                description = "Pinchazo en el neumático delantero, genera demoras en la llegada al colegio",
                author = "Comentario añadido por Luis Martinez"
            ),
            IncidentItem(
                title = "Comportamiento inapropiado",
                date = "08 de Marzo, 2026 - 1:05pm",
                description = "Sofia no atiende las indicaciones dadas por el conductor",
                author = "Comentario añadido por Luis Martinez"
            )
        )

        // Configurar RecyclerView con el Adapter
        val adapter = HistorialIncidentesAdapter(incidents)
        binding.rvIncidents.adapter = adapter
    }
}
