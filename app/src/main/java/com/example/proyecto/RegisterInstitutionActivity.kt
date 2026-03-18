package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityRegisterInstitutionBinding

class RegisterInstitutionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterInstitutionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterInstitutionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterSuccessActivity::class.java))
        }
    }
}
