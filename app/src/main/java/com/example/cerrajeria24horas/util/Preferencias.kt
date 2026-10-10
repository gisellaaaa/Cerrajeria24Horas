package com.example.cerrajeria24horas.util

import android.content.Context
import java.security.MessageDigest

/**
 * Preferencias que viven solo en este dispositivo: no tocan Firestore ni las reglas de seguridad,
 * por lo que sobreviven al salir de la app y al reiniciar el teléfono.
 */
object Preferencias {

    private const val ARCHIVO = "cerrajeria_preferencias"
    private const val CLAVE_CUENTAS = "cuentas"
    private const val CLAVE_NOTIFICACIONES = "notificaciones"
    private const val CLAVE_REDUCIR_MOVIMIENTO = "reducir_movimiento"
    private const val CLAVE_PIN = "pin"
    private const val CLAVE_OCULTAR_MONTOS = "ocultar_montos"
    private const val CLAVE_ONBOARDING = "onboarding_visto"

    private const val MAX_CUENTAS = 4

    data class CuentaGuardada(val correo: String, val nombre: String)

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    private fun limpiar(texto: String) = texto.replace("\t", " ").replace("\n", " ").trim()

    // ---------- Apariencia y avisos ----------

    fun notificaciones(context: Context): Boolean =
        prefs(context).getBoolean(CLAVE_NOTIFICACIONES, true)

    fun guardarNotificaciones(context: Context, valor: Boolean) {
        prefs(context).edit().putBoolean(CLAVE_NOTIFICACIONES, valor).apply()
    }

    fun reducirMovimiento(context: Context): Boolean =
        prefs(context).getBoolean(CLAVE_REDUCIR_MOVIMIENTO, false)

    fun guardarReducirMovimiento(context: Context, valor: Boolean) {
        prefs(context).edit().putBoolean(CLAVE_REDUCIR_MOVIMIENTO, valor).apply()
    }

    /** La pantalla de bienvenida se muestra una sola vez, en el primer arranque. */
    fun onboardingVisto(context: Context): Boolean =
        prefs(context).getBoolean(CLAVE_ONBOARDING, false)

    fun guardarOnboardingVisto(context: Context) {
        prefs(context).edit().putBoolean(CLAVE_ONBOARDING, true).apply()
    }

    // ---------- Seguridad ----------

    /** El PIN nunca se guarda en texto plano: solo su huella SHA-256. */
    private fun huella(texto: String): String {
        val resumen = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
        return resumen.joinToString("") { "%02x".format(it) }
    }

    fun tienePin(context: Context): Boolean =
        prefs(context).getString(CLAVE_PIN, "").orEmpty().isNotEmpty()

    fun pinCorrecto(context: Context, valor: String): Boolean =
        prefs(context).getString(CLAVE_PIN, "").orEmpty() == huella(valor)

    fun guardarPin(context: Context, valor: String) {
        prefs(context).edit().putString(CLAVE_PIN, huella(valor)).apply()
    }

    fun quitarPin(context: Context) {
        prefs(context).edit().remove(CLAVE_PIN).apply()
    }

/** 0 = la sesión no se cierra sola; en otro caso son minutos de inactividad. */
    fun tiempoSesionMinutos(context: Context): Int =
        prefs(context).getInt("tiempo_sesion", 0)

    fun guardarTiempoSesion(context: Context, minutos: Int) {
        prefs(context).edit().putInt("tiempo_sesion", minutos).apply()
    }

    // ---------- Privacidad ----------

    fun ocultarMontos(context: Context): Boolean =
        prefs(context).getBoolean(CLAVE_OCULTAR_MONTOS, false)

    fun guardarOcultarMontos(context: Context, valor: Boolean) {
        prefs(context).edit().putBoolean(CLAVE_OCULTAR_MONTOS, valor).apply()
    }

    /** Borra los datos locales del teléfono. No toca la base de datos. */
    fun borrarDatosLocales(context: Context) {
        prefs(context).edit().clear().apply()
    }

    // ---------- Cuentas del dispositivo ----------

    /** Cuentas con las que se ha iniciado sesión en este teléfono, para poder cambiar rápido. */
    fun cuentas(context: Context): List<CuentaGuardada> {
        val guardado = prefs(context).getString(CLAVE_CUENTAS, null) ?: return emptyList()
        return guardado.split("\n").mapNotNull { linea ->
            val partes = linea.split("\t")
            if (partes.size < 2 || partes[0].isBlank()) {
                null
            } else {
                CuentaGuardada(partes[0], partes[1])
            }
        }
    }

    fun guardarCuenta(context: Context, correo: String, nombre: String) {
        val correoNormalizado = correo.trim()
        if (correoNormalizado.isEmpty()) return

        val lista = cuentas(context).toMutableList()
        lista.removeAll { it.correo.equals(correoNormalizado, ignoreCase = true) }
        lista.add(0, CuentaGuardada(correoNormalizado, limpiar(nombre)))
        escribir(context, lista.take(MAX_CUENTAS))
    }

    fun quitarCuenta(context: Context, correo: String) {
        escribir(
            context,
            cuentas(context).filterNot { it.correo.equals(correo, ignoreCase = true) }
        )
    }

    private fun escribir(context: Context, lista: List<CuentaGuardada>) {
        prefs(context).edit()
            .putString(CLAVE_CUENTAS, lista.joinToString("\n") { "${it.correo}\t${it.nombre}" })
            .apply()
    }
}