package com.example.proyecto

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage

object FirebaseRepository {
    private val auth    = FirebaseAuth.getInstance()
    private val db      = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun usuarioActual(): FirebaseUser? = auth.currentUser
    fun cerrarSesion() = auth.signOut()
    fun registrarUsuario(
        email: String, password: String, nombre: String, rol: String,
        onSuccess: (uid: String) -> Unit, onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                db.collection("usuarios").document(uid).set(
                    mapOf("uid" to uid, "email" to email, "nombre" to nombre,
                        "rol" to rol, "telefono" to "", "fotoUrl" to "")
                ).addOnSuccessListener { onSuccess(uid) }
                    .addOnFailureListener { e -> onError(e.message ?: "Error") }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Error al registrar") }
    }
    fun iniciarSesion(
        email: String, password: String,
        onSuccess: (uid: String, rol: String, nombre: String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        onSuccess(uid, doc.getString("rol") ?: "tutor", doc.getString("nombre") ?: "")
                    }
                    .addOnFailureListener { e -> onError(e.message ?: "Error consultando rol") }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Credenciales incorrectas") }
    }
    fun enviarResetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun obtenerPerfil(uid: String, onSuccess: (Map<String, Any>) -> Unit, onError: (String) -> Unit) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc -> onSuccess(doc.data ?: emptyMap()) }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun actualizarPerfil(uid: String, nombre: String, telefono: String,
                         onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("usuarios").document(uid)
            .update(mapOf("nombre" to nombre, "telefono" to telefono))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun actualizarCamposExtra(uid: String, campos: Map<String, Any>) {
        db.collection("usuarios").document(uid).update(campos)
    }
    fun subirFotoPerfil(uid: String, uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val ref = storage.reference.child("fotos/$uid.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    val url = downloadUri.toString()
                    db.collection("usuarios").document(uid).update("fotoUrl", url)
                        .addOnSuccessListener { onSuccess(url) }
                        .addOnFailureListener { e -> onError(e.message ?: "Error") }
                }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Error subiendo foto") }
    }
    fun obtenerUsuariosConRol(rol: String, onSuccess: (List<Pair<String, Map<String, Any>>>) -> Unit) {
        db.collection("usuarios").whereEqualTo("rol", rol).get()
            .addOnSuccessListener { snap ->
                val lista = snap.documents.mapNotNull { doc -> doc.data?.let { Pair(doc.id, it) } }
                onSuccess(lista)
            }
    }
    fun publicarUbicacionConductor(uid: String, lat: Double, lng: Double) {
        db.collection("ubicaciones").document(uid)
            .set(mapOf("lat" to lat, "lng" to lng, "ts" to System.currentTimeMillis()))
    }
    fun escucharUbicacionConductor(conductorUid: String,
                                   onUpdate: (Double, Double) -> Unit): ListenerRegistration {
        return db.collection("ubicaciones").document(conductorUid)
            .addSnapshotListener { snap, _ ->
                val lat = snap?.getDouble("lat") ?: return@addSnapshotListener
                val lng = snap.getDouble("lng") ?: return@addSnapshotListener
                onUpdate(lat, lng)
            }
    }
    fun guardarParada(conductorUid: String, datos: Map<String, Any>,
                      onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        db.collection("viajes").document(conductorUid).collection("paradas").add(datos)
            .addOnSuccessListener { ref -> onSuccess(ref.id) }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun escucharParadas(conductorUid: String,
                        onUpdate: (List<Pair<String, Map<String, Any>>>) -> Unit): ListenerRegistration {
        return db.collection("viajes").document(conductorUid).collection("paradas")
            .orderBy("orden")
            .addSnapshotListener { snap, _ ->
                val lista = snap?.documents?.mapNotNull { doc ->
                    doc.data?.let { Pair(doc.id, it) }
                } ?: emptyList()
                onUpdate(lista)
            }
    }
    fun marcarParadaCompletada(conductorUid: String, paradaId: String, onSuccess: () -> Unit) {
        db.collection("viajes").document(conductorUid).collection("paradas").document(paradaId)
            .update("completada", true)
            .addOnSuccessListener { onSuccess() }
    }
    fun iniciarViaje(conductorUid: String, placa: String) {
        db.collection("viajes").document(conductorUid).set(mapOf(
            "activo" to true, "placa" to placa, "inicio" to System.currentTimeMillis()
        ), com.google.firebase.firestore.SetOptions.merge())
    }
    fun finalizarViaje(conductorUid: String) {
        db.collection("viajes").document(conductorUid)
            .update(mapOf("activo" to false, "fin" to System.currentTimeMillis()))
    }
    fun guardarEstudiante(datos: Map<String, Any>,
                          onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        db.collection("estudiantes").add(datos)
            .addOnSuccessListener { ref -> onSuccess(ref.id) }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun escucharEstudiantes(onUpdate: (List<Pair<String, Map<String, Any>>>) -> Unit): ListenerRegistration {
        return db.collection("estudiantes")
            .addSnapshotListener { snap, _ ->
                val lista = snap?.documents?.mapNotNull { doc ->
                    doc.data?.let { Pair(doc.id, it) }
                } ?: emptyList()
                onUpdate(lista)
            }
    }
    fun actualizarEstudiante(id: String, datos: Map<String, Any>,
                             onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("estudiantes").document(id).update(datos)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun obtenerEstudiantesPorConductor(conductorUid: String,
                                       onSuccess: (List<Map<String, Any>>) -> Unit) {
        db.collection("estudiantes").whereEqualTo("conductorUid", conductorUid).get()
            .addOnSuccessListener { snap -> onSuccess(snap.documents.mapNotNull { it.data }) }
    }
    fun reportarIncidente(
        conductorUid: String, tipo: String, descripcion: String,
        accelX: Float, accelY: Float, accelZ: Float,
        giroX: Float, giroY: Float, giroZ: Float,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        db.collection("incidentes").add(mapOf(
            "conductorUid" to conductorUid, "tipo" to tipo,
            "descripcion" to descripcion,
            "timestamp" to System.currentTimeMillis(),
            "sensores" to mapOf("ax" to accelX, "ay" to accelY, "az" to accelZ,
                "gx" to giroX,  "gy" to giroY,  "gz" to giroZ)
        )).addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun escucharTodosLosIncidentes(onUpdate: (List<Map<String, Any>>) -> Unit): ListenerRegistration {
        return db.collection("incidentes")
            .addSnapshotListener { snap, _ ->
                onUpdate(snap?.documents?.mapNotNull { it.data } ?: emptyList())
            }
    }
    fun getChatId(uid1: String, uid2: String): String =
        if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    fun enviarMensaje(chatId: String, senderId: String, senderName: String,
                      texto: String, imageUrl: String = "",
                      onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("chats").document(chatId).collection("mensajes").add(mapOf(
            "senderId" to senderId, "senderName" to senderName,
            "texto" to texto, "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )).addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun escucharMensajes(chatId: String,
                         onUpdate: (List<Map<String, Any>>) -> Unit): ListenerRegistration {
        return db.collection("chats").document(chatId).collection("mensajes")
            .orderBy("timestamp")
            .addSnapshotListener { snap, _ ->
                onUpdate(snap?.documents?.mapNotNull { it.data } ?: emptyList())
            }
    }
    fun subirImagenChat(chatId: String, uri: Uri,
                        onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val ref = storage.reference.child("chats/$chatId/${System.currentTimeMillis()}.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url -> onSuccess(url.toString()) }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Error") }
    }
    fun crearNotificacion(destinatarioUid: String, mensaje: String, tipo: String) {
        db.collection("notificaciones").document(destinatarioUid).collection("items").add(mapOf(
            "mensaje" to mensaje, "tipo" to tipo,
            "timestamp" to System.currentTimeMillis(), "leida" to false
        ))
    }
    fun escucharNotificaciones(uid: String,
                               onNueva: (id: String, mensaje: String) -> Unit): ListenerRegistration {
        return db.collection("notificaciones").document(uid).collection("items")
            .whereEqualTo("leida", false)
            .addSnapshotListener { snap, _ ->
                snap?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val msg = change.document.getString("mensaje") ?: ""
                        val id  = change.document.id
                        db.collection("notificaciones").document(uid)
                            .collection("items").document(id).update("leida", true)
                        onNueva(id, msg)
                    }
                }
            }
    }
    fun escucharTodosLosUsuarios(onUpdate: (List<Map<String, Any>>) -> Unit): ListenerRegistration {
        return db.collection("usuarios")
            .addSnapshotListener { snap, _ ->
                onUpdate(snap?.documents?.mapNotNull { it.data } ?: emptyList())
            }
    }
}