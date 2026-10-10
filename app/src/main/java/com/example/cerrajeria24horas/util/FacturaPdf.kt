package com.example.cerrajeria24horas.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.Servicio
import java.io.File

/**
 * Genera la factura del cliente en PDF con los datos que ya existen en el servicio.
 * No guarda nada nuevo en la base de datos: el número de factura se deriva del id del servicio.
 */
object FacturaPdf {

    private const val ANCHO = 595
    private const val ALTO = 842
    private const val MARGEN = 48f
    private const val CARPETA = "facturas"

    fun generar(contexto: Context, servicio: Servicio): Uri? {
        val numero = "FAC-" + servicio.id.take(6).uppercase()
        val documento = PdfDocument()
        return try {
            val pagina = documento.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, 1).create())
            dibujar(contexto, pagina.canvas, servicio, numero)
            documento.finishPage(pagina)

            val carpeta = File(contexto.cacheDir, CARPETA)
            if (!carpeta.exists()) carpeta.mkdirs()
            val archivo = File(carpeta, "$numero.pdf")
            archivo.outputStream().use { documento.writeTo(it) }

            FileProvider.getUriForFile(
                contexto,
                "${contexto.packageName}.fileprovider",
                archivo
            )
        } catch (_: Exception) {
            null
        } finally {
            documento.close()
        }
    }

    private fun dibujar(contexto: Context, canvas: Canvas, servicio: Servicio, numero: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val fondoClaro = contexto.getColor(R.color.fondo)

        // Banda superior azul marino con el título de la factura.
        paint.color = contexto.getColor(R.color.azul_marino)
        canvas.drawRect(0f, 0f, ANCHO.toFloat(), 128f, paint)
        paint.color = Color.WHITE
        paint.isFakeBoldText = true
        paint.textSize = 28f
        canvas.drawText(contexto.getString(R.string.factura_titulo), MARGEN, 58f, paint)
        paint.isFakeBoldText = false
        paint.textSize = 14f
        canvas.drawText(
            contexto.getString(R.string.factura_numero) + " " + numero,
            MARGEN,
            88f,
            paint
        )

        // Nombre de marca en dos tonos, igual que en la app.
        paint.isFakeBoldText = true
        paint.textSize = 22f
        paint.color = contexto.getColor(R.color.azul_marino)
        val marca = contexto.getString(R.string.marca_cerrajeria)
        canvas.drawText(marca, MARGEN, 176f, paint)
        paint.color = contexto.getColor(R.color.rojo_urgencia)
        canvas.drawText(
            contexto.getString(R.string.marca_24_horas),
            MARGEN + paint.measureText(marca),
            176f,
            paint
        )
        paint.isFakeBoldText = false
        paint.textSize = 13f
        paint.color = contexto.getColor(R.color.texto_secundario)
        canvas.drawText(contexto.getString(R.string.eslogan), MARGEN, 200f, paint)

        var y = 252f
        y = dato(
            contexto, canvas, paint,
            contexto.getString(R.string.factura_cliente),
            servicio.nombreCliente.ifBlank { servicio.clienteId },
            y
        )
        y = dato(
            contexto, canvas, paint,
            contexto.getString(R.string.factura_servicio),
            Formato.tipoServicio(servicio.tipoServicio, contexto),
            y
        )
        y = dato(
            contexto, canvas, paint,
            contexto.getString(R.string.factura_direccion),
            servicio.direccion,
            y
        )
        y = dato(
            contexto, canvas, paint,
            contexto.getString(R.string.ciudad),
            servicio.ciudad,
            y
        )
        y = dato(
            contexto, canvas, paint,
            contexto.getString(R.string.factura_fecha),
            servicio.fecha?.let { Formato.fecha(it) + " · " + Formato.hora(it) }
                ?: contexto.getString(R.string.sin_datos),
            y
        )
        y = dato(
            contexto, canvas, paint,
            contexto.getString(R.string.factura_estado),
            servicio.estado.etiqueta,
            y
        )

        // Total destacado: el número domina y su etiqueta va pequeña encima.
        val altoCaja = 86f
        val caja = y + 14f
        paint.color = fondoClaro
        canvas.drawRoundRect(MARGEN, caja, ANCHO - MARGEN, caja + altoCaja, 18f, 18f, paint)
        paint.color = contexto.getColor(R.color.azul_acento)
        canvas.drawRect(MARGEN, caja, MARGEN + 6f, caja + altoCaja, paint)
        paint.textSize = 13f
        paint.color = contexto.getColor(R.color.texto_secundario)
        canvas.drawText(contexto.getString(R.string.factura_total), MARGEN + 24f, caja + 30f, paint)
        paint.isFakeBoldText = true
        paint.textSize = 30f
        paint.color = contexto.getColor(R.color.texto_principal)
        canvas.drawText(Formato.moneda(servicio.valor), MARGEN + 24f, caja + 66f, paint)
        paint.isFakeBoldText = false

        // Pie de página.
        val pie = ALTO - 64f
        paint.color = contexto.getColor(R.color.borde)
        canvas.drawRect(MARGEN, pie, ANCHO - MARGEN, pie + 1f, paint)
        paint.textSize = 11f
        paint.color = contexto.getColor(R.color.texto_secundario)
        canvas.drawText(
            contexto.getString(R.string.factura_gracias) + "   ·   " + numero,
            MARGEN,
            pie + 24f,
            paint
        )
    }

    /** Dibuja una etiqueta pequeña con su valor debajo y devuelve la nueva posición vertical. */
    private fun dato(
        contexto: Context,
        canvas: Canvas,
        paint: Paint,
        etiqueta: String,
        valor: String,
        y: Float
    ): Float {
        paint.textSize = 12f
        paint.color = contexto.getColor(R.color.texto_secundario)
        canvas.drawText(etiqueta.uppercase(), MARGEN, y, paint)
        paint.textSize = 15f
        paint.color = contexto.getColor(R.color.texto_principal)
        canvas.drawText(valor, MARGEN, y + 20f, paint)
        return y + 48f
    }
}