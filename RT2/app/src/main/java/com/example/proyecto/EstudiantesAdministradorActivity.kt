package com.example.proyecto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityEstudiantesAdministradorBinding
import com.google.firebase.firestore.ListenerRegistration

class EstudiantesAdministradorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEstudiantesAdministradorBinding
    private var estudiantesListener: ListenerRegistration? = null
    private val todosLosEstudiantes = mutableListOf<Pair<String, Map<String, Any>>>()
    private val listaVisible        = mutableListOf<Pair<String, Map<String, Any>>>()
    private lateinit var adapter: EstudiantesFirestoreAdapter
    private val tutores    = mutableListOf<Pair<String, String>>()
    private val conductores = mutableListOf<Pair<String, String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstudiantesAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = EstudiantesFirestoreAdapter(listaVisible) { id: String, datos: Map<String, Any> ->
            mostrarDialogoEditar(id, datos)
        }
        binding.rvStudents.adapter = adapter
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filtrarPorNombre(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.chipTodos.setOnClickListener   { filtrarPorRuta("") }
        binding.chipNorte.setOnClickListener   { filtrarPorRuta("Norte") }
        binding.chipCentro.setOnClickListener  { filtrarPorRuta("Centro") }
        binding.chipOriente.setOnClickListener { filtrarPorRuta("Oriental") }
        binding.fabAgregarEstudiante.setOnClickListener { mostrarDialogoAgregar() }
        FirebaseRepository.obtenerUsuariosConRol(getString(R.string.tutor)) { lista ->
            tutores.clear()
            tutores.addAll(lista.mapNotNull { (uid, data) ->
                val nombre = data["nombre"] as? String ?: return@mapNotNull null
                Pair(uid, nombre)
            })
        }
        FirebaseRepository.obtenerUsuariosConRol(getString(R.string.conductor)) { lista ->
            conductores.clear()
            conductores.addAll(lista.mapNotNull { (uid, data) ->
                val nombre = data["nombre"] as? String ?: return@mapNotNull null
                Pair(uid, nombre)
            })
        }

        escucharEstudiantes()
    }
    private fun escucharEstudiantes() {
        estudiantesListener = FirebaseRepository.escucharEstudiantes { lista: List<Pair<String, Map<String, Any>>> ->
            todosLosEstudiantes.clear()
            todosLosEstudiantes.addAll(lista)
            listaVisible.clear()
            listaVisible.addAll(lista)
            adapter.notifyDataSetChanged()
        }
    }
    private fun filtrarPorNombre(texto: String) {
        listaVisible.clear()
        val filtrados: List<Pair<String, Map<String, Any>>> =
            if (texto.isEmpty()) todosLosEstudiantes
            else todosLosEstudiantes.filter { par ->
                (par.second["nombre"] as? String ?: "").contains(texto, ignoreCase = true)
            }
        listaVisible.addAll(filtrados)
        adapter.notifyDataSetChanged()
    }
    private fun filtrarPorRuta(ruta: String) {
        listaVisible.clear()
        val filtrados: List<Pair<String, Map<String, Any>>> =
            if (ruta.isEmpty()) todosLosEstudiantes
            else todosLosEstudiantes.filter { par ->
                (par.second["ruta"] as? String ?: "").contains(ruta, ignoreCase = true)
            }
        listaVisible.addAll(filtrados)
        adapter.notifyDataSetChanged()
    }
    private fun mostrarDialogoAgregar() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 20, 60, 20)
        }

        val etNombre = EditText(this).apply { hint = "Nombre completo del estudiante" }
        val etDoc    = EditText(this).apply {
            hint = "Número de identificación"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etDir  = EditText(this).apply { hint = "Dirección de la parada" }
        val etRuta = EditText(this).apply { hint = "Ruta (Norte, Centro, Oriental)" }
        var tutorSeleccionado: Pair<String, String>? = null
        val spTutor = AutoCompleteTextView(this).apply {
            hint = "Seleccionar tutor"
            threshold = 1
            setAdapter(ArrayAdapter(this@EstudiantesAdministradorActivity,
                android.R.layout.simple_dropdown_item_1line,
                tutores.map { it.second }.toTypedArray()))
            setOnItemClickListener { _, _, pos, _ ->
                tutorSeleccionado = tutores.getOrNull(pos)
            }
        }
        var conductorSeleccionado: Pair<String, String>? = null
        val spConductor = AutoCompleteTextView(this).apply {
            hint = "Seleccionar conductor"
            threshold = 1
            setAdapter(ArrayAdapter(this@EstudiantesAdministradorActivity,
                android.R.layout.simple_dropdown_item_1line,
                conductores.map { it.second }.toTypedArray()))
            setOnItemClickListener { _, _, pos, _ ->
                conductorSeleccionado = conductores.getOrNull(pos)
            }
        }
        layout.addView(etNombre)
        layout.addView(etDoc)
        layout.addView(etDir)
        layout.addView(etRuta)
        layout.addView(spTutor)
        layout.addView(spConductor)

        AlertDialog.Builder(this)
            .setTitle("Agregar estudiante")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val doc    = etDoc.text.toString().trim()
                val dir    = etDir.text.toString().trim()
                val ruta   = etRuta.text.toString().trim()

                if (nombre.isEmpty() || doc.isEmpty()) {
                    Toast.makeText(this, "Nombre y documento son obligatorios",
                        Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val datos = mapOf(
                    "nombre"          to nombre,
                    "documento"       to doc,
                    "direccionParada" to dir,
                    "ruta"            to ruta,
                    "tutorUid"        to (tutorSeleccionado?.first    ?: ""),
                    "tutorNombre"     to (tutorSeleccionado?.second   ?: ""),
                    "conductorUid"    to (conductorSeleccionado?.first ?: ""),
                    "conductorNombre" to (conductorSeleccionado?.second ?: ""),
                    "timestamp"       to System.currentTimeMillis()
                )
                FirebaseRepository.guardarEstudiante(datos,
                    onSuccess = { id ->
                        Toast.makeText(this,
                            "Estudiante guardado — ID: ${id.take(8)}...",
                            Toast.LENGTH_LONG).show()
                    },
                    onError = { msg ->
                        Toast.makeText(this, "Error: $msg", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun mostrarDialogoEditar(id: String, datos: Map<String, Any>) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 20, 60, 20)
        }
        val etNombre = EditText(this).apply {
            hint = "Nombre completo"
            setText(datos["nombre"] as? String ?: "")
        }
        val etDoc = EditText(this).apply {
            hint = "Número de identificación"
            setText(datos["documento"] as? String ?: "")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etDir = EditText(this).apply {
            hint = "Dirección de la parada"
            setText(datos["direccionParada"] as? String ?: "")
        }
        val etRuta = EditText(this).apply {
            hint = "Ruta"
            setText(datos["ruta"] as? String ?: "")
        }
        layout.addView(etNombre)
        layout.addView(etDoc)
        layout.addView(etDir)
        layout.addView(etRuta)

        AlertDialog.Builder(this)
            .setTitle("Editar — ID: ${id.take(8)}...")
            .setView(layout)
            .setPositiveButton("Guardar cambios") { _, _ ->
                FirebaseRepository.actualizarEstudiante(id,
                    mapOf(
                        "nombre"          to etNombre.text.toString().trim(),
                        "documento"       to etDoc.text.toString().trim(),
                        "direccionParada" to etDir.text.toString().trim(),
                        "ruta"            to etRuta.text.toString().trim()
                    ),
                    onSuccess = {
                        Toast.makeText(this, "Datos actualizados",
                            Toast.LENGTH_SHORT).show()
                    },
                    onError = { msg ->
                        Toast.makeText(this, "Error: $msg", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    override fun onDestroy() {
        super.onDestroy()
        estudiantesListener?.remove()
    }
}