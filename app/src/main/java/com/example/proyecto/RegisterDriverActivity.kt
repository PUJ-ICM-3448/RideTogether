package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityRegisterDriverBinding

class RegisterDriverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterDriverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterSuccessActivity::class.java))
        }
    }
}
