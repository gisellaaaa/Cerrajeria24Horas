package com.example.cerrajeria24horas.util

import android.content.Context
import android.util.Patterns
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.Servicio
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Formato {

    private val localeCO = Locale.forLanguageTag("es-CO")
    private val simbolosCOP = DecimalFormatSymbols(localeCO)
    private val formatoMoneda = DecimalFormat("\$#,##0.00", simbolosCOP)
    private val formatoFecha = SimpleDateFormat("EEEE, dd 'de' MMM 'de' yyyy", localeCO)
    private val formatoFechaCorta = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val formatoHora = SimpleDateFormat("hh:mm a", localeCO)
    private val formatoHoraCorta = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun moneda(cantidad: Double): String = formatoMoneda.format(cantidad)

    fun fecha(timestamp: Timestamp): String = formatoFecha.format(timestamp.toDate())

    fun fechaCorta(timestamp: Timestamp): String = formatoFechaCorta.format(timestamp.toDate())

    fun hora(timestamp: Timestamp): String = formatoHora.format(timestamp.toDate())

    fun horaCorta(calendario: Calendar): String = formatoHoraCorta.format(calendario.time)

    fun tipoServicio(constante: String, context: Context): String {
        val valores = context.resources.getStringArray(R.array.tipos_servicio)
        val etiquetas = context.resources.getStringArray(R.array.tipos_servicio_etiquetas)
        val indice = valores.indexOf(constante)
        return if (indice in etiquetas.indices) etiquetas[indice] else constante
    }

    fun esCorreoValido(correo: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(correo).matches()

    fun esTelefonoValido(telefono: String): Boolean {
        val digitos = telefono.replace(Regex("[^0-9]"), "")
        return digitos.length in 7..15
    }
}

object Fechas {

    private fun conMedianoche(calendario: Calendar): Calendar {
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        return calendario
    }

    fun inicioDeHoy(): Calendar = conMedianoche(Calendar.getInstance())

    fun inicioDeManana(): Calendar = Calendar.getInstance().apply {
        timeInMillis = inicioDeHoy().timeInMillis
        add(Calendar.DAY_OF_MONTH, 1)
    }

    fun inicioDeMes(): Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        conMedianoche(this)
    }

    fun inicioDeProximoMes(): Calendar = Calendar.getInstance().apply {
        timeInMillis = inicioDeMes().timeInMillis
        add(Calendar.MONTH, 1)
    }

    fun inicioDeSemana(): Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        conMedianoche(this)
    }

    fun inicioDeProximaSemana(): Calendar = Calendar.getInstance().apply {
        timeInMillis = inicioDeSemana().timeInMillis
        add(Calendar.DAY_OF_MONTH, 7)
    }

    fun inicioDeAnio(): Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_YEAR, 1)
        conMedianoche(this)
    }

    fun inicioDeProximoAnio(): Calendar = Calendar.getInstance().apply {
        timeInMillis = inicioDeAnio().timeInMillis
        add(Calendar.YEAR, 1)
    }
}

object Calculos {

    fun serviciosRealizadosEn(
        servicios: List<Servicio>,
        desde: Calendar,
        hasta: Calendar
    ): List<Servicio> {
        val inicio = desde.timeInMillis
        val fin = hasta.timeInMillis
        return servicios.filter { servicio ->
            if (servicio.estado != EstadoServicio.REALIZADO || servicio.fecha == null) {
                false
            } else {
                val tiempo = servicio.fecha.toDate().time
                tiempo >= inicio && tiempo < fin
            }
        }
    }

    fun sumarValores(servicios: List<Servicio>): Double =
        servicios.sumOf { it.valor }

    fun contarPorEstado(servicios: List<Servicio>, estado: EstadoServicio): Int =
        servicios.count { it.estado == estado }
}