package com.example.cerrajeria24horas.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.cerrajeria24horas.R

/**
 * Bloque de esqueleto con brillo que reemplaza a los ProgressBar genéricos.
 * Cuando el movimiento está reducido se muestra estático (sin brillo).
 */
object Esqueleto {

    fun filas(contenedor: ViewGroup, cantidad: Int = 3) {
        contenedor.removeAllViews()
        val ctx = contenedor.context
        val densidad = ctx.resources.displayMetrics.density
        val activo = Movimiento.activo(ctx)

        repeat(cantidad) {
            val bloque = View(ctx)
            val parametros = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (84 * densidad).toInt()
            )
            parametros.bottomMargin = (12 * densidad).toInt()
            bloque.layoutParams = parametros
            val shimmer = EsqueletoDrawable(ctx)
            bloque.background = shimmer
            if (activo) animar(shimmer)
            contenedor.addView(bloque)
        }
    }

    private fun animar(shimmer: EsqueletoDrawable) {
        ValueAnimator.ofInt(0, 10_000).apply {
            duration = 1_100L
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { shimmer.level = it.animatedValue as Int }
            start()
        }
    }
}

/** Rectángulo redondeado con una banda de brillo que recorre su ancho. */
class EsqueletoDrawable(context: Context) : Drawable() {

    private val base = ContextCompat.getColor(context, R.color.fondo_suave)
    private val brillo = ContextCompat.getColor(context, R.color.fondo_tarjeta)
    private val radio = 16f * context.resources.displayMetrics.density
    private val rect = RectF()
    private val recorte = Rect()
    private val pintura = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        rect.set(bounds)
        recorte.set(bounds)
    }

    override fun draw(canvas: Canvas) {
        pintura.shader = null
        pintura.color = base
        canvas.drawRoundRect(rect, radio, radio, pintura)

        val ancho = rect.width()
        if (ancho <= 0f) return
        val centro = rect.left - ancho + (level / 10_000f) * (ancho * 2f)
        val anchoBanda = ancho * 0.45f
        pintura.shader = LinearGradient(
            centro - anchoBanda, 0f, centro + anchoBanda, 0f,
            intArrayOf(
                Color.TRANSPARENT,
                colorConAlfa(brillo, 200),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, radio, radio, pintura)
        pintura.shader = null
    }

    private fun colorConAlfa(color: Int, alfa: Int): Int =
        Color.argb(alfa, Color.red(color), Color.green(color), Color.blue(color))

    override fun setAlpha(alpha: Int) {
        pintura.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        pintura.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}
