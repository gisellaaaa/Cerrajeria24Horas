package com.example.cerrajeria24horas.data

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

object FirestoreAPI {

    private const val COL_USUARIOS = "usuarios"
    private const val COL_CLIENTES = "clientes"
    private const val COL_SERVICIOS = "servicios"
    private const val COL_HISTORIAL = "historialEstados"

    private const val CAMPO_REGISTRADO_POR = "registradoPor"
    private const val CAMPO_CERRAJERO = "cerrajeroId"
    private const val CAMPO_USUARIO = "usuarioId"

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ---------- Usuario ----------

    fun guardarUsuario(
        uid: String,
        nombre: String,
        correo: String,
        telefono: String
    ): Task<Void> =
        db.collection(COL_USUARIOS).document(uid).set(
            hashMapOf(
                "uid" to uid,
                "nombre" to nombre,
                "correo" to correo,
                "telefono" to telefono,
                "rol" to "admin",
                "fechaCreacion" to Timestamp.now()
            )
        )

    fun obtenerUsuario(uid: String): Task<DocumentSnapshot> =
        db.collection(COL_USUARIOS).document(uid).get()

    // ---------- Clientes ----------

    fun guardarCliente(cliente: Cliente): Task<DocumentReference> {
        val referencia = db.collection(COL_CLIENTES).document()
        return referencia
            .set(cliente.copy(clienteId = referencia.id).toMap())
            .continueWith { referencia }
    }

    fun obtenerClientes(uid: String): Task<QuerySnapshot> =
        db.collection(COL_CLIENTES)
            .whereEqualTo(CAMPO_REGISTRADO_POR, uid)
            .get()

    fun ordenarClientes(lista: List<Cliente>): List<Cliente> =
        lista.sortedByDescending { it.fechaRegistro?.toDate()?.time ?: 0L }

    fun obtenerCliente(id: String): Task<DocumentSnapshot> =
        db.collection(COL_CLIENTES).document(id).get()

    // ---------- Servicios ----------

    fun guardarServicio(servicio: Servicio): Task<DocumentReference> {
        val referencia = db.collection(COL_SERVICIOS).document()
        return referencia
            .set(servicio.copy(servicioId = referencia.id).toMap())
            .continueWith { referencia }
    }

    fun obtenerServicios(uid: String): Task<QuerySnapshot> =
        db.collection(COL_SERVICIOS)
            .whereEqualTo(CAMPO_CERRAJERO, uid)
            .get()

    fun obtenerServicio(id: String): Task<DocumentSnapshot> =
        db.collection(COL_SERVICIOS).document(id).get()

    fun ordenarServicios(lista: List<Servicio>): List<Servicio> =
        lista.sortedByDescending { it.fecha?.toDate()?.time ?: 0L }

    // ---------- Estado de servicio ----------

    fun actualizarEstadoServicio(servicioId: String, estadoNuevo: EstadoServicio): Task<Void> {
        val actualizaciones = hashMapOf<String, Any>("estado" to estadoNuevo.valor)
        if (estadoNuevo == EstadoServicio.REALIZADO) {
            actualizaciones["fechaFinalizacion"] = Timestamp.now()
        }
        return db.collection(COL_SERVICIOS).document(servicioId).update(actualizaciones)
    }

    // ---------- Historial de estados ----------

    fun registrarHistorialEstado(
        servicioId: String,
        estadoAnterior: String,
        estadoNuevo: String,
        usuarioId: String
    ): Task<DocumentReference> {
        val referencia = db.collection(COL_HISTORIAL).document()
        return referencia
            .set(
                HistorialEstado(
                    historialId = referencia.id,
                    servicioId = servicioId,
                    estadoAnterior = estadoAnterior,
                    estadoNuevo = estadoNuevo,
                    usuarioId = usuarioId
                ).toMap()
            )
            .continueWith { referencia }
    }
}