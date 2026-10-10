package com.example.cerrajeria24horas.util

import android.content.Context

/** Momento del último uso de la app, solo en memoria: se pierde al cerrar el proceso. */
object Interaccion {

    private var ultima = 0L

    fun marcar() {
        ultima = System.currentTimeMillis()
    }

    /** Minutos transcurridos desde el último uso; 0 si la app se acaba de abrir. */
    fun minutosInactivos(): Long {
        if (ultima == 0L) return 0
        return (System.currentTimeMillis() - ultima) / 60_000
    }

    fun reiniciar() {
        ultima = 0L
    }

    /** Montos ocultos a petición del usuario: se revelan solo durante esta sesión. */
    object Montos {
        private var revelados = false

        fun ocultos(context: Context): Boolean = Preferencias.ocultarMontos(context) && !revelados

        fun revelar() {
            revelados = true
        }

        fun reiniciar() {
            revelados = false
        }
    }
}