package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseRepository.usuarioActual()?.uid?.let { uid ->
            consultarRolYNavegar(uid); return
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnLogin.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val pass  = binding.tilPassword.editText?.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnLogin.isEnabled = false
            FirebaseRepository.iniciarSesion(email, pass,
                onSuccess = { _, rol, nombre ->
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Bienvenido, $nombre", Toast.LENGTH_SHORT).show()
                    navegarSegunRol(rol)
                },
                onError = { msg ->
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Error: $msg", Toast.LENGTH_LONG).show()
                }
            )
        }
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Escribe tu correo primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseRepository.enviarResetPassword(email,
                onSuccess = { Toast.makeText(this, "Correo de recuperación enviado a $email", Toast.LENGTH_LONG).show() },
                onError   = { msg -> Toast.makeText(this, "Error: $msg", Toast.LENGTH_LONG).show() }
            )
        }
    }
    private fun consultarRolYNavegar(uid: String) {
        FirebaseRepository.obtenerPerfil(uid,
            onSuccess = { data -> navegarSegunRol(data["rol"] as? String ?: "tutor") },
            onError   = { FirebaseRepository.cerrarSesion() }
        )
    }
    private fun navegarSegunRol(rol: String) {
        val dest = when (rol) {
            getString(R.string.tutor),         "tutor"         -> HomeActivity::class.java
            getString(R.string.conductor),     "conductor"     -> PantallaPrincipalConductorActivity::class.java
            getString(R.string.administrador), "administrador" -> PantallaPrincipalAdministradorActivity::class.java
            else -> HomeActivity::class.java
        }
        startActivity(Intent(this, dest).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
