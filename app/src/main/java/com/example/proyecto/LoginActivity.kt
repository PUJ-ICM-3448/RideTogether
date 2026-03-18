package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la Toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // CONFIGURACIÓN DEL DROPDOWN
        setupUserTypeDropdown()

        // Lógica del botón Iniciar sesión
        binding.btnLogin.setOnClickListener {
            val userType = binding.actvUserType.text.toString()
            val email = binding.tilEmail.editText?.text.toString()
            val password = binding.tilPassword.editText?.text.toString()

            // 1. VALIDACIÓN DEL TIPO DE USUARIO
            if (userType.isEmpty()) {
                binding.tilUserType.error = getString(R.string.user_type_required)
                Toast.makeText(this, getString(R.string.user_type_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                binding.tilUserType.error = null
            }

            // 2. VALIDACIÓN DE CAMPOS DE AUTENTICACIÓN
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Mensaje de éxito genérico
                Toast.makeText(this, "Inicio de sesión como $userType exitoso", Toast.LENGTH_SHORT).show()
                
                // 3. NAVEGACIÓN CONDICIONAL
                // Se redirige al usuario según el rol seleccionado en la lista desplegable
                navigateToPrincipal(userType)
            } else {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Funcionalidad no implementada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUserTypeDropdown() {
        val userTypes = arrayOf(
            getString(R.string.tutor),
            getString(R.string.conductor),
            getString(R.string.administrador)
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userTypes)
        binding.actvUserType.setAdapter(adapter)
    }

    /**
     * Redirige a la pantalla correspondiente según el tipo de usuario seleccionado.
     */
    private fun navigateToPrincipal(type: String) {
        val intent = when (type) {
            getString(R.string.tutor) -> {
                // Navega a la pantalla principal del Tutor
                Intent(this, HomeActivity::class.java)
            }
            getString(R.string.conductor) -> {
                // Navega a la pantalla principal del Conductor
                Intent(this, PantallaPrincipalConductorActivity::class.java)
            }
            getString(R.string.administrador) -> {
                // Navega a la nueva pantalla principal del Administrador
                Intent(this, PantallaPrincipalAdministradorActivity::class.java)
            }
            else -> Intent(this, HomeActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
