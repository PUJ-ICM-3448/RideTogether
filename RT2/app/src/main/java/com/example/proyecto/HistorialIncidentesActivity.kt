package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityHistorialIncidentesBinding

class HistorialIncidentesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorialIncidentesBinding
    private val todos = listOf(
        IncidentItem("Comportamiento inapropiado", "13 de Marzo, 2026 - 1:10pm", "Sofia estuvo peleando durante el trayecto con otra compañera", "Comentario añadido por Luis Martinez"),
        IncidentItem("Avería en el bus", "10 de Marzo, 2026 - 7:15am", "Pinchazo en el neumático delantero, genera demoras en la llegada al colegio", "Comentario añadido por Luis Martinez"),
        IncidentItem("Comportamiento inapropiado", "08 de Marzo, 2026 - 1:05pm", "Sofia no atiende las indicaciones dadas por el conductor", "Comentario añadido por Luis Martinez"),
        IncidentItem("Retraso en ruta", "05 de Marzo, 2026 - 7:40am", "Trancón en la avenida principal causó 25 minutos de retraso", "Comentario añadido por Luis Martinez"),
        IncidentItem("Avería en el bus", "01 de Marzo, 2026 - 3:20pm", "Falla en el sistema de frenos — bus detenido preventivamente", "Comentario añadido por Luis Martinez"),
        IncidentItem("Retraso en ruta", "20 de Febrero, 2026 - 7:30am", "Desvío por obras viales en el sector norte", "Comentario añadido por Luis Martinez")
    )
    private var hasta = 3
    private val visible = mutableListOf<IncidentItem>()
    private lateinit var adapter: HistorialIncidentesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialIncidentesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        visible.addAll(todos.take(hasta))
        adapter = HistorialIncidentesAdapter(visible)
        binding.rvIncidents.adapter = adapter
        actualizarBoton()
        binding.btnSeeMore.setOnClickListener {
            val sig = (hasta + 2).coerceAtMost(todos.size)
            val nuevos = todos.subList(hasta, sig)
            visible.addAll(nuevos)
            adapter.notifyItemRangeInserted(hasta, nuevos.size)
            hasta = sig
            actualizarBoton()
        }
    }
    private fun actualizarBoton() {
        if (hasta >= todos.size) { binding.btnSeeMore.text = "No hay más incidentes"; binding.btnSeeMore.isEnabled = false }
        else { binding.btnSeeMore.text = "Ver más .... (${todos.size - hasta})"; binding.btnSeeMore.isEnabled = true }
    }
}
