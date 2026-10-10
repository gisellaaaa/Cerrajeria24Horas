package com.example.cerrajeria24horas.util

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.EstadoServicio
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException

object UI {

    /**
     * Escribe el letrero de la marca en un solo [TextView] ("Cerrajería 24 Horas") con el
     * nombre en azul marino y el "24 Horas" en rojo de urgencia. Evita el corte que producía
     * tener dos etiquetas separadas en pantallas estrechas.
     */
    fun aplicarMarca(texto: TextView) {
        val ctx = texto.context
        val marca = ctx.getString(R.string.marca_cerrajeria)
        val horas = ctx.getString(R.string.marca_24_horas)
        val completo = "$marca $horas"
        val span = SpannableString(completo)
        span.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.azul_marino)),
            0,
            marca.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        span.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.rojo_urgencia)),
            marca.length + 1,
            completo.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        texto.text = span
    }

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