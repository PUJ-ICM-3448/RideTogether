package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityEstudiantesAdministradorBinding
import com.example.proyecto.databinding.ItemEstudianteAdministradorBinding

class EstudiantesAdministradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstudiantesAdministradorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstudiantesAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Cargar datos mock usando los IDs definidos en el include de activity_estudiantes_administrador.xml
        setupMockData()
    }

    private fun setupMockData() {
        // Obtenemos los bindings de los items incluidos
        val s1 = ItemEstudianteAdministradorBinding.bind(binding.student1.root)
        val s2 = ItemEstudianteAdministradorBinding.bind(binding.student2.root)
        val s3 = ItemEstudianteAdministradorBinding.bind(binding.student3.root)
        val s4 = ItemEstudianteAdministradorBinding.bind(binding.student4.root)
        val s5 = ItemEstudianteAdministradorBinding.bind(binding.student5.root)

        setupStudentItem(s1, "Sofia Guzman", "Norte", "#3")
        setupStudentItem(s2, "Camilo Lopez", "Oriental", "#1")
        setupStudentItem(s3, "Mariana Torres", "Central", "#4")
        setupStudentItem(s4, "Rene Castillo", "Norte", "#1")
        setupStudentItem(s5, "Cristina Hurtado", "Oriental", "#9")
    }

    private fun setupStudentItem(
        itemBinding: ItemEstudianteAdministradorBinding,
        name: String,
        route: String,
        stop: String
    ) {
        itemBinding.tvName.text = name
        itemBinding.tvRoute.text = "Ruta: $route"
        itemBinding.tvStop.text = "Parada $stop"
        
        itemBinding.btnDetails.setOnClickListener {
            // Navegación a la pantalla de Detalles del Estudiante
            val intent = Intent(this, DetalleEstudianteActivity::class.java)
            startActivity(intent)
        }
    }
}
