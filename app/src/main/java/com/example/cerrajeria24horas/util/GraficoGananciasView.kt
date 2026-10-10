package com.example.cerrajeria24horas.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.max
import com.example.cerrajeria24horas.R

/** Gráfico de barras verticales sencillo para las ganancias diarias. */
class GraficoGananciasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val colorBarra = ContextCompat.getColor(context, R.color.azul_acento_claro)
    private val colorDestaque = ContextCompat.getColor(context, R.color.oro)
    private val colorBase = ContextCompat.getColor(context, R.color.borde)
    private val colorTexto = ContextCompat.getColor(context, R.color.texto_secundario)

    private val densidad = resources.displayMetrics.density
    private val radioBarra = 6f * densidad
    private val altoEtiqueta = 20f * densidad
    private val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pinturaTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 11f * densidad
        color = colorTexto
    }
    private val rect = RectF()

    private var valores = floatArrayOf()
    private var etiquetas = emptyArray<String>()

    fun datos(valores: List<Float>, etiquetas: List<String>) {
        this.valores = valores.toFloatArray()
        this.etiquetas = etiquetas.toTypedArray()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val ancho = width.toFloat()
        val alto = height.toFloat()
        if (ancho <= 0f || alto <= 0f || valores.isEmpty()) return

        val baseY = alto - altoEtiqueta
        pintura.color = colorBase
        canvas.drawRect(0f, baseY, ancho, baseY + densidad, pintura)

        val maximo = max(valores.maxOrNull() ?: 0f, 1f)
        val cantidad = valores.size
        val separacion = 8f * densidad
        val anchoBarra = (ancho - separacion * (cantidad + 1)) / cantidad
        val altoDisponible = baseY - 8f * densidad

        valores.forEachIndexed { indice, valor ->
            val izquierda = separacion + indice * (anchoBarra + separacion)
            val altura = if (valor <= 0f) radioBarra else (valor / maximo) * altoDisponible
            val arriba = baseY - altura
            pintura.color = if (indice == cantidad - 1) colorDestaque else colorBarra
            rect.set(izquierda, arriba, izquierda + anchoBarra, baseY)
            canvas.drawRoundRect(rect, radioBarra, radioBarra, pintura)

            val etiqueta = etiquetas.getOrNull(indice).orEmpty()
            if (etiqueta.isNotEmpty()) {
                canvas.drawText(etiqueta, izquierda + anchoBarra / 2f, alto - 6f * densidad, pinturaTexto)
            }
        }
    }
}
