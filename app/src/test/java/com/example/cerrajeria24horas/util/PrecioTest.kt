package com.example.cerrajeria24horas.util

import com.example.cerrajeria24horas.util.FormatoPrecioWatcher.Companion.posicionTrasDigitos
import org.junit.Assert.assertEquals
import org.junit.Test

class PrecioTest {

    @Test
    fun formateaEnterosConSeparadorDeMiles() {
        assertEquals("", Precio.formatear(""))
        assertEquals("0", Precio.formatear("0"))
        assertEquals("1", Precio.formatear("1"))
        assertEquals("12.500", Precio.formatear("12500"))
        assertEquals("1.250.000", Precio.formatear("1250000"))
    }

    @Test
    fun limpiaEntradasPegadasOSucias() {
        assertEquals("1.250.000", Precio.formatear("\$1.250.000"))
        assertEquals("12.500", Precio.formatear("00012500"))
        assertEquals("", Precio.formatear("abc"))
        assertEquals("12.500", Precio.formatear("1.2.5.0.0"))
    }

    @Test
    fun respetaElLimiteDeDigitos() {
        assertEquals("123.456.789.012", Precio.formatear("1234567890123"))
    }

    @Test
    fun admiteDecimalesCuandoSePiden() {
        assertEquals("1,5", Precio.formatear("1,5", permitirDecimales = true))
        assertEquals("1,56", Precio.formatear("1,567", permitirDecimales = true))
        assertEquals("1.250.000,5", Precio.formatear("1250000,5", permitirDecimales = true))
        assertEquals("0,5", Precio.formatear(",5", permitirDecimales = true))
    }

    @Test
    fun leeElNumeroFormateado() {
        assertEquals(0.0, Precio.aDouble(""), 0.0)
        assertEquals(0.0, Precio.aDouble("abc"), 0.0)
        assertEquals(12500.0, Precio.aDouble("12.500"), 0.0)
        assertEquals(1250000.0, Precio.aDouble("\$1.250.000"), 0.0)
        assertEquals(1250000.5, Precio.aDouble("1.250.000,50"), 0.0)
    }

    @Test
    fun calculaElCursorTrasLosDigitos() {
        assertEquals(0, posicionTrasDigitos("1.250.000", 0))
        assertEquals(4, posicionTrasDigitos("1.250.000", 3))
        assertEquals(9, posicionTrasDigitos("1.250.000", 7))
        assertEquals(9, posicionTrasDigitos("1.250.000", 99))
    }
}
