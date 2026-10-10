package com.example.cerrajeria24horas.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min
import kotlin.math.roundToInt
import com.example.cerrajeria24horas.R

/** Anillo de progreso usado en la tarjeta héroe; muestra el porcentaje centrado. */
class AnilloProgresoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pista = ContextCompat.getColor(context, R.color.onda_oscura)
    private val colorProgreso = ContextCompat.getColor(context, R.color.azul_marino)
    private val colorTexto = ContextCompat.getColor(context, R.color.azul_marino)

    private val densidad = resources.displayMetrics.density
    private val grosor = 10f * densidad
    private val rect = RectF()

    private val pintura = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = grosor
        strokeCap = Paint.Cap.ROUND
    }
    private val pinturaTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        color = colorTexto
    }

    private var fraccion = 0f

    fun mostrar(valor: Float) {
        fraccion = valor.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val ancho = width.toFloat()
        val alto = height.toFloat()
        if (ancho <= 0f || alto <= 0f) return

        val diametro = min(ancho, alto) - grosor
        val centroX = ancho / 2f
        val centroY = alto / 2f
        rect.set(
            centroX - diametro / 2f,
            centroY - diametro / 2f,
            centroX + diametro / 2f,
            centroY + diametro / 2f
        )

        pintura.color = pista
        canvas.drawArc(rect, 0f, 360f, false, pintura)
        pintura.color = colorProgreso
        canvas.drawArc(rect, -90f, 360f * fraccion, false, pintura)

        pinturaTexto.textSize = min(ancho, alto) * 0.28f
        val texto = context.getString(R.string.porcentaje, (fraccion * 100f).roundToInt())
        val metricas = pinturaTexto.fontMetrics
        canvas.drawText(texto, centroX, centroY - (metricas.ascent + metricas.descent) / 2f, pinturaTexto)
    }
}
