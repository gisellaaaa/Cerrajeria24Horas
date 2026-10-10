package com.example.cerrajeria24horas.util

import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.Servicio
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalculosTest {

    @Test
    fun soloServiciosRealizadosY_deHoy_cuentan() {
        val hoy = Calendar.getInstance()

        val hoyMediodia = hoy.clone() as Calendar
        hoyMediodia.set(Calendar.HOUR_OF_DAY, 12)
        hoyMediodia.set(Calendar.MINUTE, 0)
        hoyMediodia.set(Calendar.SECOND, 0)
        hoyMediodia.set(Calendar.MILLISECOND, 0)

        val ayerMediodia = hoy.clone() as Calendar
        ayerMediodia.add(Calendar.DAY_OF_YEAR, -1)
        ayerMediodia.set(Calendar.HOUR_OF_DAY, 12)
        ayerMediodia.set(Calendar.MINUTE, 0)
        ayerMediodia.set(Calendar.SECOND, 0)
        ayerMediodia.set(Calendar.MILLISECOND, 0)

        val servicios = listOf(
            Servicio(valor = 1000.0, estado = EstadoServicio.REALIZADO, fecha = Timestamp(hoyMediodia.time)),
            Servicio(valor = 2000.0, estado = EstadoServicio.REALIZADO, fecha = Timestamp(ayerMediodia.time)),
            Servicio(valor = 5000.0, estado = EstadoServicio.PENDIENTE, fecha = Timestamp(hoyMediodia.time)),
            Servicio(valor = 7000.0, estado = EstadoServicio.CANCELADO, fecha = Timestamp(hoyMediodia.time)),
            Servicio(valor = 3000.0, estado = EstadoServicio.EN_PROCESO, fecha = Timestamp(hoyMediodia.time))
        )

        val gananciaDeHoy = Calculos.serviciosRealizadosEn(
            servicios,
            Fechas.inicioDeHoy(),
            Fechas.inicioDeManana()
        )

        assertEquals(1, gananciaDeHoy.size)
        assertEquals(1000.0, gananciaDeHoy[0].valor, 0.0)
        assertEquals(1000.0, Calculos.sumarValores(gananciaDeHoy), 0.0)
    }

    @Test
    fun gananciasDelAnioIncluyenTodosLosRealizadosDelAnioActual() {
        val hoy = Calendar.getInstance()

        val eneroDelAnio = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val servicios = listOf(
            Servicio(valor = 4000.0, estado = EstadoServicio.REALIZADO, fecha = Timestamp(eneroDelAnio.time)),
            Servicio(valor = 6000.0, estado = EstadoServicio.REALIZADO, fecha = Timestamp(hoy.time)),
            Servicio(valor = 9000.0, estado = EstadoServicio.PENDIENTE, fecha = Timestamp(hoy.time))
        )

        val gananciaDelAnio = Calculos.serviciosRealizadosEn(
            servicios,
            Fechas.inicioDeAnio(),
            Fechas.inicioDeProximoAnio()
        )

        assertEquals(2, gananciaDelAnio.size)
        assertEquals(10000.0, Calculos.sumarValores(gananciaDelAnio), 0.0)
    }

    @Test
    fun inicioDeMesCorrespondeAlPrimerDia() {
        val inicioDelMes = Fechas.inicioDeMes()
        assertTrue(inicioDelMes.get(Calendar.DAY_OF_MONTH) == 1)
        assertEquals(0, inicioDelMes.get(Calendar.HOUR_OF_DAY))
    }
}