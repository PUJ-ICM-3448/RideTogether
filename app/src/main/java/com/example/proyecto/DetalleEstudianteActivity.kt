package com.example.proyecto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityDetalleEstudianteBinding

class DetalleEstudianteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEstudianteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEstudianteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val tutorPhone = intent.getStringExtra("tutorPhone") ?: ""
        val tutorEmail = intent.getStringExtra("tutorEmail") ?: ""

        if (tutorPhone.isNotEmpty()) binding.tvTutorPhone.text = tutorPhone
        if (tutorEmail.isNotEmpty()) binding.tvTutorEmail.text = tutorEmail

        binding.ivCallTutor.setOnClickListener {
            val phone = binding.tvTutorPhone.text.toString()
            if (phone.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                })
            }
        }

        binding.ivEmailTutor.setOnClickListener {
            val email = binding.tvTutorEmail.text.toString()
            if (email.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                    putExtra(Intent.EXTRA_SUBJECT, "Contacto desde RideTogether")
                })
            }
        }
    }
}