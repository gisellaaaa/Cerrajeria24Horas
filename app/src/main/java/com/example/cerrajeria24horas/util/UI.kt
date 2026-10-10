package com.example.cerrajeria24horas.util

import android.content.Context
import android.content.res.ColorStateList
import android.widget.TextView
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.EstadoServicio
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException

object UI {

    fun colorDeEstado(estado: EstadoServicio): Int = when (estado) {
        EstadoServicio.PENDIENTE -> R.color.estado_pendiente
        EstadoServicio.EN_PROCESO -> R.color.estado_en_proceso
        EstadoServicio.REALIZADO -> R.color.estado_realizado
        EstadoServicio.CANCELADO -> R.color.estado_cancelado
    }

    fun pintarEstado(texto: TextView, estado: EstadoServicio) {
        texto.text = estado.etiqueta
        texto.backgroundTintList =
            ColorStateList.valueOf(texto.context.getColor(colorDeEstado(estado)))
        texto.setTextColor(
            texto.context.getColor(
                if (estado == EstadoServicio.PENDIENTE) R.color.texto_sobre_dorado else R.color.white
            )
        )
    }

    fun mensajeErrorFirestore(context: Context, e: Exception): String {
        return if (e is FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    context.getString(R.string.error_permisos)
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    context.getString(R.string.error_red)
                FirebaseFirestoreException.Code.CANCELLED,
                FirebaseFirestoreException.Code.NOT_FOUND ->
                    context.getString(R.string.error_base_datos)
                FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                    context.getString(R.string.error_configuracion)
                else -> context.getString(R.string.error_guardar)
            }
        } else if (e is FirebaseNetworkException) {
            context.getString(R.string.error_red)
        } else {
            context.getString(R.string.error_guardar)
        }
    }
}