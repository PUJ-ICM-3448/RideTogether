package com.example.proyecto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.ActivityRegisterInstitutionBinding

class RegisterInstitutionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterInstitutionBinding
    private var fotoUri: Uri? = null
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            fotoUri = it
            Glide.with(this).load(it).circleCrop().into(binding.ivInstitution)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterInstitutionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.ivInstitution.setOnClickListener { galleryLauncher.launch("image/*") }

        binding.btnCreateAccount.setOnClickListener {
            val nombre  = binding.tilFullName.editText?.text.toString().trim()
            val email   = binding.tilEmail.editText?.text.toString().trim()
            val pass    = binding.tilPassword.editText?.text.toString()
            val confirm = binding.tilConfirmPassword.editText?.text.toString()
            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirm) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.btnCreateAccount.isEnabled = false

            FirebaseRepository.registrarUsuario(email, pass, nombre, getString(R.string.administrador),
                onSuccess = { uid ->
                    val uri = fotoUri
                    if (uri != null) FirebaseRepository.subirFotoPerfil(uid, uri,
                        { _ -> exito() }, { _ -> exito() })
                    else exito()
                },
                onError = { msg ->
                    binding.btnCreateAccount.isEnabled = true
                    Toast.makeText(this, "Error: $msg", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
    private fun exito() {
        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, RegisterSuccessActivity::class.java))
        finish()
    }
}