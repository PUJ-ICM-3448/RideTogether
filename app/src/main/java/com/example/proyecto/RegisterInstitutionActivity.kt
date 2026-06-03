package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
            val name = binding.tilFullName.editText?.text.toString()
            val email = binding.tilEmail.editText?.text.toString()
            val password = binding.tilPassword.editText?.text.toString()
            val confirmPassword = binding.tilConfirmPassword.editText?.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (password != confirmPassword) {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (UserManager.userExists(this, email)) {
                    Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newUser = UserManager.User(email, password, getString(R.string.administrador), name)
                UserManager.saveUser(this, newUser)

                Toast.makeText(this, "Registro de Institución exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, RegisterSuccessActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
