package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.cbAgree.setOnCheckedChangeListener { _, isChecked ->
            binding.btnGotIt.isEnabled = isChecked
        }

        binding.btnGotIt.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("section", "privacy")
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
