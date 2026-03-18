package com.example.proyecto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityRegisterTypeBinding
import com.google.android.material.card.MaterialCardView

class RegisterTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterTypeBinding

    // Launcher para TermsActivity
    private val startTermsActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Si el usuario aceptó términos, continuamos con el registro
            navigateToRegistration()
        } else {
            // Si no aceptó, mostramos el mensaje de advertencia
            Toast.makeText(this, "Debes aceptar los términos y condiciones para continuar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar selección inicial
        updateSelection(binding.cvTutor)

        binding.cvTutor.setOnClickListener {
            updateSelection(binding.cvTutor)
        }

        binding.cvDriver.setOnClickListener {
            updateSelection(binding.cvDriver)
        }

        binding.cvAdmin.setOnClickListener {
            updateSelection(binding.cvAdmin)
        }

        binding.btnContinue.setOnClickListener {
            checkTermsAndContinue()
        }
    }

    private fun checkTermsAndContinue() {
        val sharedPref = getSharedPreferences("RideTogetherPrefs", Context.MODE_PRIVATE)
        val termsAccepted = sharedPref.getBoolean("termsAccepted", false)

        if (termsAccepted) {
            // Si ya aceptó, navega directamente al registro
            navigateToRegistration()
        } else {
            // Si no, abre TermsActivity para validación
            val intent = Intent(this, TermsActivity::class.java)
            startTermsActivity.launch(intent)
        }
    }

    private fun navigateToRegistration() {
        val intent = when {
            binding.rbTutor.isChecked -> Intent(this, RegisterTutorActivity::class.java)
            binding.rbDriver.isChecked -> Intent(this, RegisterDriverActivity::class.java)
            binding.rbAdmin.isChecked -> Intent(this, RegisterInstitutionActivity::class.java)
            else -> null
        }
        intent?.let { startActivity(it) }
    }

    private fun updateSelection(selectedCard: MaterialCardView) {
        // Desmarcar todos
        binding.rbTutor.isChecked = false
        binding.rbDriver.isChecked = false
        binding.rbAdmin.isChecked = false

        // Quitar bordes de selección
        binding.cvTutor.strokeWidth = 1
        binding.cvDriver.strokeWidth = 1
        binding.cvAdmin.strokeWidth = 1

        // Marcar el seleccionado
        when (selectedCard.id) {
            binding.cvTutor.id -> {
                binding.rbTutor.isChecked = true
                binding.cvTutor.strokeWidth = 4
            }
            binding.cvDriver.id -> {
                binding.rbDriver.isChecked = true
                binding.cvDriver.strokeWidth = 4
            }
            binding.cvAdmin.id -> {
                binding.rbAdmin.isChecked = true
                binding.cvAdmin.strokeWidth = 4
            }
        }
    }
}
