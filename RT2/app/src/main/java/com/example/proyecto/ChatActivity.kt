package com.example.proyecto

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.databinding.ActivityChatBinding
import com.google.firebase.firestore.ListenerRegistration

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private var mensajesListener: ListenerRegistration? = null
    private val otroUid    get() = intent.getStringExtra("otroUid")    ?: ""
    private val otroNombre get() = intent.getStringExtra("otroNombre") ?: "Chat"
    private val miUid      get() = FirebaseRepository.usuarioActual()?.uid ?: ""
    private var miNombre   = ""
    private val mensajes = mutableListOf<Map<String, Any>>()
    private lateinit var adapter: ChatMensajeAdapter
    private val chatId get() = FirebaseRepository.getChatId(miUid, otroUid)
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { enviarFoto(it) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarChat.title = otroNombre
        binding.toolbarChat.setNavigationOnClickListener { finish() }
        FirebaseRepository.obtenerPerfil(miUid,
            onSuccess = { data -> miNombre = data["nombre"] as? String ?: "" },
            onError = { }
        )
        val lm = LinearLayoutManager(this)
        lm.stackFromEnd = true
        binding.rvMensajes.layoutManager = lm
        adapter = ChatMensajeAdapter(mensajes, miUid)
        binding.rvMensajes.adapter = adapter
        binding.btnEnviar.setOnClickListener {
            val texto = binding.etMensaje.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener
            binding.etMensaje.setText("")
            FirebaseRepository.enviarMensaje(chatId, miUid, miNombre, texto,
                onSuccess = { }, onError = { msg -> Toast.makeText(this, "Error: $msg", Toast.LENGTH_SHORT).show() })
        }
        binding.btnEnviarFoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        escucharMensajes()
    }
    private fun enviarFoto(uri: Uri) {
        FirebaseRepository.subirImagenChat(chatId, uri,
            onSuccess = { url ->
                FirebaseRepository.enviarMensaje(chatId, miUid, miNombre, "", url,
                    onSuccess = { }, onError = { })
            },
            onError = { msg -> Toast.makeText(this, "Error enviando foto: $msg", Toast.LENGTH_SHORT).show() }
        )
    }
    private fun escucharMensajes() {
        mensajesListener = FirebaseRepository.escucharMensajes(chatId) { lista: List<Map<String, Any>> ->
            mensajes.clear()
            mensajes.addAll(lista)
            adapter.notifyDataSetChanged()
            if (mensajes.isNotEmpty()) {
                binding.rvMensajes.scrollToPosition(mensajes.size - 1)
            }
        }
    }
    override fun onDestroy() { super.onDestroy(); mensajesListener?.remove() }
}