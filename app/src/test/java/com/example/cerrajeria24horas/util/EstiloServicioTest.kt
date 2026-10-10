package com.example.cerrajeria24horas.util

import com.example.cerrajeria24horas.data.TiposServicio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstiloServicioTest {

    @Test
    fun `cada tipo de servicio devuelve forma, fondo e icono validos`() {
        TiposServicio.valores.forEach { tipo ->
            val estilo = EstiloServicio.para(tipo)
            assertTrue("forma $tipo", estilo.forma != 0)
            assertTrue("fondo $tipo", estilo.fondo != 0)
            assertTrue("icono $tipo", estilo.icono != 0)
        }
    }

    @Test
    fun `un tipo desconocido cae en Otro`() {
        val desconocido = EstiloServicio.para("NO_EXISTE")
        val otro = EstiloServicio.para("OTRO")
        assertEquals(otro, desconocido)
    }

    @Test
    fun `un tipo nulo no revienta`() {
        val estilo = EstiloServicio.para(null)
        assertTrue(estilo.icono != 0)
    }
}
