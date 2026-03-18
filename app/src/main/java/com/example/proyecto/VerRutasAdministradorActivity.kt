package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityVerRutasAdministradorBinding

class VerRutasAdministradorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerRutasAdministradorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerRutasAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón de retroceso
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
