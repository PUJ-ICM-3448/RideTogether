package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityVerRutaConductorBinding

class VerRutaConductorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerRutaConductorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerRutaConductorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la Toolbar azul
        binding.toolbar.setNavigationOnClickListener {
            finish() // Regresa a la pantalla anterior
        }

        // Obtener el nombre de la ruta si se pasó como extra (opcional)
        val routeName = intent.getStringExtra("ROUTE_NAME") ?: "Ver ruta"
        binding.toolbar.title = routeName

        setupStudentList()
    }

    private fun setupStudentList() {
        // DATOS MOCK: Diferenciamos estados por color según el requerimiento
        val students = listOf(
            EstudianteRuta(
                nombre = "Sofia Guzman",
                ruta = "Norte",
                estado = "En direccion a destino",
                colorEstado = android.R.color.holo_green_dark
            ),
            EstudianteRuta(
                nombre = "Camilo Lopez",
                ruta = "Nororiental",
                estado = "Tres paradas faltantes",
                colorEstado = android.R.color.holo_orange_dark
            ),
            EstudianteRuta(
                nombre = "Camila Ruiz",
                ruta = "Central",
                estado = "No asistió",
                colorEstado = android.R.color.holo_red_dark
            ),
            EstudianteRuta(
                nombre = "Felipe Ortiz",
                ruta = "Central",
                estado = "Retraso por tráfico",
                colorEstado = android.R.color.holo_orange_dark
            )
        )

        val adapter = EstudianteRutaAdapter(students)
        binding.rvStudents.adapter = adapter
    }
}
