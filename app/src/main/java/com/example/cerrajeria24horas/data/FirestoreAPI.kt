package com.example.cerrajeria24horas.data

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
    private const val CAMPO_ASIGNADO_A = "asignadoA"
    private const val CAMPO_TALLER = "tallerId"

    /** Nombre de la instancia de Firebase aparte, para crear cuentas sin cerrar la sesión del admin. */
    private const val APP_SECUNDARIA = "altaEquipo"

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ---------- Usuario ----------

    fun guardarUsuario(
        uid: String,
        nombre: String,
        correo: String,
        telefono: String,
        rol: RolUsuario = RolUsuario.ADMIN,
        tallerId: String = uid
    ): Task<Void> =
        db.collection(COL_USUARIOS).document(uid).set(
            Usuario(
                uid = uid,
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                rol = rol,
                tallerId = tallerId,
                fechaCreacion = Timestamp.now()
            ).toMap()
        )

    fun obtenerUsuario(uid: String): Task<DocumentSnapshot> =
        db.collection(COL_USUARIOS).document(uid).get()

    /** Todos los miembros (administrador y cerrajeros) del taller indicado. */
    fun obtenerUsuariosDelTaller(tallerId: String): Task<QuerySnapshot> =
        db.collection(COL_USUARIOS)
            .whereEqualTo(CAMPO_TALLER, tallerId)
            .get()

    /**
     * Crea la cuenta de un cerrajero desde el administrador. Usa una instancia de Firebase aparte
     * ([APP_SECUNDARIA]) para no cerrar la sesión del administrador al crear la cuenta.
     */
    fun crearCuentaCerrajero(
        nombre: String,
        correo: String,
        password: String,
        tallerId: String
    ): Task<String> {
        val principal = FirebaseApp.getInstance()
        val secundaria = try {
            FirebaseApp.getInstance(APP_SECUNDARIA)
        } catch (_: IllegalStateException) {
            FirebaseApp.initializeApp(principal.applicationContext, principal.options, APP_SECUNDARIA)
        }
        val authSecundaria = FirebaseAuth.getInstance(secundaria)
        return authSecundaria.createUserWithEmailAndPassword(correo, password)
            .continueWithTask { tarea ->
                val uid = tarea.result.user!!.uid
                guardarUsuario(
                    uid = uid,
                    nombre = nombre,
                    correo = correo,
                    telefono = "",
                    rol = RolUsuario.CERRAJERO,
                    tallerId = tallerId
                ).continueWith { uid }
            }
            .addOnCompleteListener { authSecundaria.signOut() }
    }

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

    /** Servicios asignados a un cerrajero concreto (vista del cerrajero). */
    fun obtenerServiciosAsignados(cerrajeroUid: String): Task<QuerySnapshot> =
        db.collection(COL_SERVICIOS)
            .whereEqualTo(CAMPO_ASIGNADO_A, cerrajeroUid)
            .get()

    /** El administrador asigna (o reasigna) un servicio a un cerrajero de su taller. */
    fun asignarServicio(servicioId: String, cerrajeroUid: String): Task<Void> =
        db.collection(COL_SERVICIOS).document(servicioId).update(CAMPO_ASIGNADO_A, cerrajeroUid)

    /**
     * Servicios que corresponden al usuario según su rol: el administrador ve todo su taller y el
     * cerrajero solo los servicios que tiene asignados.
     */
    fun obtenerServiciosSegunRol(uid: String): Task<QuerySnapshot> =
        obtenerUsuario(uid).continueWithTask { tarea ->
            val esCerrajero = tarea.result.getString("rol") == RolUsuario.CERRAJERO.valor
            if (esCerrajero) obtenerServiciosAsignados(uid) else obtenerServicios(uid)
        }

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