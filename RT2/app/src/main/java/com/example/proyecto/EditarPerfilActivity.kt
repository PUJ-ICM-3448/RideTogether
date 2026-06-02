package com.example.proyecto

import android.content.Intent; import android.net.Uri; import android.os.Bundle; import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts; import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.ActivityEditarPerfilBinding

class EditarPerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditarPerfilBinding
    private var fotoUri: Uri? = null
    private val uid get() = FirebaseRepository.usuarioActual()?.uid ?: ""
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { fotoUri = it; Glide.with(this).load(it).circleCrop().into(binding.ivProfilePicture) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.ivProfileCamera.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.ivProfilePicture.setOnClickListener { galleryLauncher.launch("image/*") }
        cargarPerfil()
        binding.btnSave.setOnClickListener {
            val nombre = binding.etName.text.toString().trim()
            val telefono = binding.etPhone.text.toString().trim()
            val nuevaPass = binding.etPassword.text.toString()
            if (nombre.isEmpty()) { Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            binding.btnSave.isEnabled = false
            val uri = fotoUri
            if (uri != null) {
                FirebaseRepository.subirFotoPerfil(uid, uri, { _ -> guardar(nombre, telefono, nuevaPass) }, { _ -> guardar(nombre, telefono, nuevaPass) })
            } else guardar(nombre, telefono, nuevaPass)
        }
        binding.btnLogoutContainer.setOnClickListener {
            FirebaseRepository.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
            finish()
        }
    }
    private fun cargarPerfil() {
        if (uid.isEmpty()) return
        FirebaseRepository.obtenerPerfil(uid,
            onSuccess = { data ->
                binding.etName.setText(data["nombre"] as? String ?: "")
                binding.etEmail.setText(data["email"] as? String ?: "")
                binding.etPhone.setText(data["telefono"] as? String ?: "")
                val fotoUrl = data["fotoUrl"] as? String ?: ""
                if (fotoUrl.isNotEmpty()) Glide.with(this).load(fotoUrl).circleCrop().into(binding.ivProfilePicture)
            },
            onError = { Toast.makeText(this, "Error cargando perfil", Toast.LENGTH_SHORT).show() }
        )
    }
    private fun guardar(nombre: String, telefono: String, nuevaPass: String) {
        FirebaseRepository.actualizarPerfil(uid, nombre, telefono,
            onSuccess = {
                if (nuevaPass.length >= 6) {
                    FirebaseRepository.usuarioActual()?.updatePassword(nuevaPass)
                        ?.addOnSuccessListener { listo() }
                        ?.addOnFailureListener { listo() }
                } else listo()
            },
            onError = { msg -> binding.btnSave.isEnabled = true; Toast.makeText(this, "Error: $msg", Toast.LENGTH_LONG).show() }
        )
    }
    private fun listo() {
        binding.btnSave.isEnabled = true
        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
        finish()
    }
}
