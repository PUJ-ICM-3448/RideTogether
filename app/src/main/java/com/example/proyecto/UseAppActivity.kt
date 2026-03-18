package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityUseAppBinding

class UseAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUseAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUseAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // El botón solo se habilita si el checkbox está marcado
        binding.cbAgree.setOnCheckedChangeListener { _, isChecked ->
            binding.btnGotIt.isEnabled = isChecked
        }

        binding.btnGotIt.setOnClickListener {
            // Retornamos que esta sección fue visitada
            val resultIntent = Intent()
            resultIntent.putExtra("section", "use_app")
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
