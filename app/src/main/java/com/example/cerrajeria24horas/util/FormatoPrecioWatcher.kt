package com.example.cerrajeria24horas.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Formato y lectura de valores de dinero mientras se escriben.
 *
 * Es coherente con [Formato.moneda]: usa el mismo separador de miles ('.') y el mismo
 * separador decimal (',') del locale es-CO. El campo guarda siempre un número limpio.
 */
object Precio {

    /** Tope de dígitos enteros para evitar desbordes. */
    const val MAX_DIGITOS_ENTEROS = 12
    const val MAX_DECIMALES = 2

    /**
     * Reda el texto que el usuario escribe y lo devuelve agrupado por miles.
     * Con [permitirDecimales] admite una única coma decimal de hasta dos dígitos.
     */
    fun formatear(entrada: String, permitirDecimales: Boolean = false): String {
        if (entrada.isEmpty()) return ""

        if (!permitirDecimales) {
            val digitos = entrada.filter { it.isDigit() }.take(MAX_DIGITOS_ENTEROS)
            if (digitos.isEmpty()) return ""
            return agrupar(quitarCerosIzquierda(digitos))
        }

        val permiteComa = entrada.filter { it.isDigit() || it == ',' || it == '.' }
        val separador = permiteComa.indexOfFirst { it == ',' || it == '.' }
        val enteros = if (separador < 0) {
            permiteComa.filter { it.isDigit() }
        } else {
            permiteComa.take(separador).filter { it.isDigit() }
        }.take(MAX_DIGITOS_ENTEROS)
        val decimales = if (separador < 0) {
            ""
        } else {
            permiteComa.drop(separador + 1).filter { it.isDigit() }.take(MAX_DECIMALES)
        }

        val hayDecimal = separador >= 0 || decimales.isNotEmpty()
        val enterosAgrupados = agrupar(quitarCerosIzquierda(enteros))
        val base = enterosAgrupados.ifEmpty { if (hayDecimal) "0" else "" }
        return if (hayDecimal) "$base,$decimales" else base
    }

    /** Convierte un texto ya formateado (". " miles y "," decimal) en un número limpio. */
    fun aDouble(texto: String): Double {
        val normalizado = texto
            .replace("\$", "")
            .replace(".", "")
            .replace(',', '.')
            .filter { it.isDigit() || it == '.' }
        return normalizado.toDoubleOrNull() ?: 0.0
    }

    private fun quitarCerosIzquierda(digitos: String): String {
        val sinCeros = digitos.trimStart('0')
        return if (sinCeros.isEmpty() && digitos.isNotEmpty()) "0" else sinCeros
    }

    private fun agrupar(enteros: String): String {
        if (enteros.isEmpty()) return ""
        val salida = StringBuilder()
        val largo = enteros.length
        for (i in 0 until largo) {
            if (i > 0 && (largo - i) % 3 == 0) salida.append('.')
            salida.append(enteros[i])
        }
        return salida.toString()
    }
}

/**
 * Aplica [Precio.formatear] a un [EditText] en cada tecla, sin disparar bucles y
 * conservando la posición del cursor respecto a los dígitos a su izquierda.
 */
class FormatoPrecioWatcher @JvmOverloads constructor(
    private val editText: EditText,
    private val permitirDecimales: Boolean = false
) : TextWatcher {

    private var actualizando = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (actualizando) return
        val actual = s?.toString().orEmpty()
        val formateado = Precio.formatear(actual, permitirDecimales)
        if (formateado == actual) return

        val cursor = (editText.selectionStart.takeIf { it >= 0 } ?: actual.length)
            .coerceIn(0, actual.length)
        val digitosIzquierda = actual.take(cursor).count { it.isDigit() }

        actualizando = true
        editText.setText(formateado)
        val nueva = posicionTrasDigitos(formateado, digitosIzquierda)
        editText.setSelection(nueva.coerceIn(0, formateado.length))
        actualizando = false
    }

    override fun afterTextChanged(s: Editable?) {}

    companion object {
        /** Posición del texto justo después del n-ésimo dígito. */
        fun posicionTrasDigitos(texto: String, digitos: Int): Int {
            if (digitos <= 0) return 0
            var contador = 0
            for (i in texto.indices) {
                if (texto[i].isDigit()) {
                    contador++
                    if (contador == digitos) return i + 1
                }
            }
            return texto.length
        }
    }
}
