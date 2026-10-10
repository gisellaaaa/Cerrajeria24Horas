package com.example.cerrajeria24horas.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MovimientoTest {

    @Test
    fun `con movimiento activo la duracion se conserva`() {
        assertEquals(250L, Movimiento.duracion(Movimiento.MEDIA, activo = true))
        assertEquals(450L, Movimiento.duracion(Movimiento.LARGA, activo = true))
    }

    @Test
    fun `con reducir movimiento la duracion es cero`() {
        assertEquals(0L, Movimiento.duracion(Movimiento.MEDIA, activo = false))
        assertEquals(0L, Movimiento.duracion(Movimiento.LARGA, activo = false))
    }

    @Test
    fun `ninguna animacion supera el limite de 450ms`() {
        assertEquals(450L, Movimiento.LARGA)
        assert(Movimiento.CORTA <= Movimiento.LARGA)
        assert(Movimiento.MEDIA <= Movimiento.LARGA)
    }
}
