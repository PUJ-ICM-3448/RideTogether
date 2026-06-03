package com.example.proyecto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityTermsBinding

class TermsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsBinding

    // Variables para controlar la visita a cada sección
    private var visitedUseApp = false
    private var visitedPrivacy = false
    private var visitedResponsibilities = false
    private var visitedLimitations = false

    // ActivityResultLauncher para recibir el resultado de las sub-pantallas
    private val startSubActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val section = result.data?.getStringExtra("section")
            when (section) {
                "use_app" -> visitedUseApp = true
                "privacy" -> visitedPrivacy = true
                "responsibilities" -> visitedResponsibilities = true
                "limitations" -> visitedLimitations = true
            }
            validateAcceptButton()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        validateAcceptButton()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Navegación a las secciones
        binding.cvUseApp.setOnClickListener {
            val intent = Intent(this, UseAppActivity::class.java)
            startSubActivity.launch(intent)
        }

        binding.cvPrivacy.setOnClickListener {
            val intent = Intent(this, PrivacyActivity::class.java)
            startSubActivity.launch(intent)
        }

        binding.cvResponsibilities.setOnClickListener {
            val intent = Intent(this, ResponsibilitiesActivity::class.java)
            startSubActivity.launch(intent)
        }

        binding.cvLimitations.setOnClickListener {
            val intent = Intent(this, LimitationsActivity::class.java)
            startSubActivity.launch(intent)
        }

        // Checkbox principal
        binding.cbAcceptTerms.setOnCheckedChangeListener { _, _ ->
            validateAcceptButton()
        }

        // Botón Aceptar
        binding.btnAccept.setOnClickListener {
            if (visitedAllSections() && binding.cbAcceptTerms.isChecked) {
                saveTermsAccepted()
                finishWithResult()
            } else {
                Toast.makeText(this, "Debes leer todas las secciones y aceptar los términos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateAcceptButton() {
        // El botón solo se habilita si el checkbox está marcado y todas las secciones fueron visitadas
        binding.btnAccept.isEnabled = binding.cbAcceptTerms.isChecked && visitedAllSections()
    }

    private fun visitedAllSections(): Boolean {
        return visitedUseApp && visitedPrivacy && visitedResponsibilities && visitedLimitations
    }

    private fun saveTermsAccepted() {
        val sharedPref = getSharedPreferences("RideTogetherPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("termsAccepted", true)
            apply()
        }
    }

    private fun finishWithResult() {
        // Retornamos OK a RegisterTypeActivity para que sepa que puede continuar
        setResult(RESULT_OK)
        finish()
    }
}
