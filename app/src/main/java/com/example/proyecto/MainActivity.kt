package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnStartLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterTypeActivity::class.java))
        }
    }
}
