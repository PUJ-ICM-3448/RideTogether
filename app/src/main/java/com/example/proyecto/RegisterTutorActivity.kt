package com.example.proyecto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.proyecto.databinding.ActivityRegisterTutorBinding

class RegisterTutorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterTutorBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnScanQR.setOnClickListener {
            checkCameraPermission()
        }

        binding.btnCreateAccount.setOnClickListener {
            // Mock registration logic
            startActivity(Intent(this, RegisterSuccessActivity::class.java))
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "Abriendo cámara...", Toast.LENGTH_SHORT).show()
            }
            else -> {
                showPermissionDialog()
            }
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.camera_permission_title))
            .setMessage(getString(R.string.camera_permission_msg))
            .setPositiveButton(getString(R.string.allow)) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton(getString(R.string.deny)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
