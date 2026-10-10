package com.example.cerrajeria24horas.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

enum class EstadoServicio(val valor: String, val etiqueta: String) {
    PENDIENTE("PENDIENTE", "Pendiente"),
    EN_PROCESO("EN_PROCESO", "En proceso"),
    REALIZADO("REALIZADO", "Realizado"),
    CANCELADO("CANCELADO", "Cancelado");

    companion object {
        fun desde(valor: String?): EstadoServicio =
            values().firstOrNull { it.valor == valor } ?: PENDIENTE
    }
}

object TiposServicio {
    val valores = listOf(
        "APERTURA_DE_PUERTA",
        "CAMBIO_DE_CERRADURA",
        "INSTALACION_DE_CERRADURA",
        "REPARACION_DE_CERRADURA",
        "DUPLICADO_DE_LLAVES",
        "APERTURA_DE_VEHICULO",
        "OTRO"
    )
}

/** Rol dentro del taller. El administrador crea las cuentas de sus cerrajeros. */
enum class RolUsuario(val valor: String, val etiqueta: String) {
    ADMIN("admin", "Administrador"),
    CERRAJERO("cerrajero", "Cerrajero");

    companion object {
        fun desde(valor: String?): RolUsuario =
            values().firstOrNull { it.valor == valor } ?: ADMIN
    }
}

/** Perfil del usuario. `tallerId` une a los cerrajeros con el administrador que los creó. */
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rol: RolUsuario = RolUsuario.ADMIN,
    val tallerId: String = "",
    val fechaCreacion: Timestamp? = null
) {
    fun toMap(): MutableMap<String, Any> = hashMapOf(
        "uid" to uid,
        "nombre" to nombre,
        "correo" to correo,
        "telefono" to telefono,
        "rol" to rol.valor,
        "tallerId" to tallerId,
        "fechaCreacion" to (fechaCreacion ?: Timestamp.now())
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): Usuario {
            val datos = doc.data ?: return Usuario()
            return Usuario(
                uid = datos["uid"] as? String ?: doc.id,
                nombre = datos["nombre"] as? String ?: "",
                correo = datos["correo"] as? String ?: "",
                telefono = datos["telefono"] as? String ?: "",
                rol = RolUsuario.desde(datos["rol"] as? String),
                tallerId = datos["tallerId"] as? String ?: "",
                fechaCreacion = datos["fechaCreacion"] as? Timestamp
            )
        }
    }
}

data class Cliente(
    val id: String = "",
    val clienteId: String = "",
    val nombreCompleto: String = "",
    val telefono: String = "",
    val correo: String = "",
    val ciudad: String = "",
    val direccion: String = "",
    val notas: String = "",
    val fechaRegistro: Timestamp? = null,
    val registradoPor: String = ""
) {
    fun toMap(): MutableMap<String, Any> = hashMapOf(
        "clienteId" to clienteId,
        "nombreCompleto" to nombreCompleto,
        "telefono" to telefono,
        "correo" to correo,
        "ciudad" to ciudad,
        "direccion" to direccion,
        "notas" to notas,
        "fechaRegistro" to (fechaRegistro ?: Timestamp.now()),
        "registradoPor" to registradoPor
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): Cliente {
            val datos = doc.data ?: return Cliente()
            return Cliente(
                id = doc.id,
                clienteId = datos["clienteId"] as? String ?: doc.id,
                nombreCompleto = datos["nombreCompleto"] as? String ?: "",
                telefono = datos["telefono"] as? String ?: "",
                correo = datos["correo"] as? String ?: "",
                ciudad = datos["ciudad"] as? String ?: "",
                direccion = datos["direccion"] as? String ?: "",
                notas = datos["notas"] as? String ?: "",
                fechaRegistro = datos["fechaRegistro"] as? Timestamp,
                registradoPor = datos["registradoPor"] as? String ?: ""
            )
        }
    }
}

data class Servicio(
    val id: String = "",
    val servicioId: String = "",
    val clienteId: String = "",
    val nombreCliente: String = "",
    val tipoServicio: String = "",
    val descripcion: String = "",
    val ciudad: String = "",
    val direccion: String = "",
    val fecha: Timestamp? = null,
    val valor: Double = 0.0,
    val estado: EstadoServicio = EstadoServicio.PENDIENTE,
    val fechaCreacion: Timestamp? = null,
    val fechaFinalizacion: Timestamp? = null,
    val cerrajeroId: String = "",
    val asignadoA: String = ""
) {
    fun toMap(): MutableMap<String, Any> {
        val mapa = hashMapOf<String, Any>(
            "servicioId" to servicioId,
            "clienteId" to clienteId,
            "nombreCliente" to nombreCliente,
            "tipoServicio" to tipoServicio,
            "descripcion" to descripcion,
            "ciudad" to ciudad,
            "direccion" to direccion,
            "fecha" to (fecha ?: Timestamp.now()),
            "valor" to valor,
            "estado" to estado.valor,
            "fechaCreacion" to (fechaCreacion ?: Timestamp.now()),
            "cerrajeroId" to cerrajeroId,
            "asignadoA" to asignadoA
        )
        if (fechaFinalizacion != null) {
            mapa["fechaFinalizacion"] = fechaFinalizacion
        }
        return mapa
    }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): Servicio {
            val datos = doc.data ?: return Servicio()
            return Servicio(
                id = doc.id,
                servicioId = datos["servicioId"] as? String ?: doc.id,
                clienteId = datos["clienteId"] as? String ?: "",
                nombreCliente = datos["nombreCliente"] as? String ?: "",
                tipoServicio = datos["tipoServicio"] as? String ?: "",
                descripcion = datos["descripcion"] as? String ?: "",
                ciudad = datos["ciudad"] as? String ?: "",
                direccion = datos["direccion"] as? String ?: "",
                fecha = datos["fecha"] as? Timestamp,
                valor = (datos["valor"] as? Number)?.toDouble() ?: 0.0,
                estado = EstadoServicio.desde(datos["estado"] as? String),
                fechaCreacion = datos["fechaCreacion"] as? Timestamp,
                fechaFinalizacion = datos["fechaFinalizacion"] as? Timestamp,
                cerrajeroId = datos["cerrajeroId"] as? String ?: "",
                asignadoA = datos["asignadoA"] as? String ?: ""
            )
        }
    }
}

data class HistorialEstado(
    val id: String = "",
    val historialId: String = "",
    val servicioId: String = "",
    val estadoAnterior: String = "",
    val estadoNuevo: String = "",
    val fecha: Timestamp? = null,
    val usuarioId: String = ""
) {
    fun toMap(): MutableMap<String, Any> = hashMapOf(
        "historialId" to historialId,
        "servicioId" to servicioId,
        "estadoAnterior" to estadoAnterior,
        "estadoNuevo" to estadoNuevo,
        "fecha" to (fecha ?: Timestamp.now()),
        "usuarioId" to usuarioId
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): HistorialEstado {
            val datos = doc.data ?: return HistorialEstado()
            return HistorialEstado(
                id = doc.id,
                historialId = datos["historialId"] as? String ?: doc.id,
                servicioId = datos["servicioId"] as? String ?: "",
                estadoAnterior = datos["estadoAnterior"] as? String ?: "",
                estadoNuevo = datos["estadoNuevo"] as? String ?: "",
                fecha = datos["fecha"] as? Timestamp,
                usuarioId = datos["usuarioId"] as? String ?: ""
            )
        }
    }
}