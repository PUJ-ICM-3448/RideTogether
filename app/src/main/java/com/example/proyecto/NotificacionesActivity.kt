package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityNotificacionesBinding

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificacionesBinding
    private lateinit var adapter: NotificacionesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BOTÓN VOLVER: Configuración de la flecha de navegación en la Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish() // Regresa a la pantalla anterior
        }

        // Configuración de la lista de notificaciones
        setupNotificationList()

        // BOTÓN LIMPIAR: Lógica para vaciar la lista
        binding.btnClear.setOnClickListener {
            adapter.clearAll() // Llama a la función del adapter para vaciar los datos
        }
    }

    private fun setupNotificationList() {
        // DATOS MOCK: Carga inicial sugerida en el requerimiento
        val mockData = mutableListOf(
            NotificationItem(
                id = 1,
                type = "bus",
                title = "Bus cerca",
                description = "El bus ABC123 (12) se encuentra cerca de la ubicación marcada como llegada"
            ),
            NotificationItem(
                id = 2,
                type = "estudiante",
                title = "Estudiante recogido",
                description = "Sofia Guzman se subió a la ruta #12 del colegio cambridge school"
            ),
            NotificationItem(
                id = 3,
                type = "estudiante",
                title = "Estudiante entregado",
                description = "Sofia Guzman fue entregada en la ubicación marcada como destino final"
            ),
            NotificationItem(
                id = 4,
                type = "incidente",
                title = "Incidente detectado",
                description = "Se registró una avería en el bus #12, se presentaron retrasos en la entrega de la estudiante"
            ),
            NotificationItem(
                id = 5,
                type = "incidente",
                title = "Incidente detectado",
                description = "La alumna Sofia Gomez no se está comportando adecuadamente en el trayecto al colegio"
            )
        )

        // Inicializar el Adapter y asignarlo al RecyclerView
        adapter = NotificacionesAdapter(mockData)
        binding.rvNotifications.adapter = adapter
    }
}
