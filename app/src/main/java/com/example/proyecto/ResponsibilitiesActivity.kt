package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityResponsibilitiesBinding

class ResponsibilitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResponsibilitiesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponsibilitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.cbAgree.setOnCheckedChangeListener { _, isChecked ->
            binding.btnGotIt.isEnabled = isChecked
        }

        binding.btnGotIt.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("section", "responsibilities")
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
