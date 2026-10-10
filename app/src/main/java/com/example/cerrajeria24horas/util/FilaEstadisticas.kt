package com.example.cerrajeria24horas.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

/** Apila las tarjetas cuando el ancho o el tamaño de letra necesitan más espacio. */
class FilaEstadisticas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var apilada: Boolean? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val ancho = View.MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val densidad = resources.displayMetrics.density
        val escalaTexto = resources.configuration.fontScale.coerceAtLeast(1f)
        val anchoMinimo = childCount * 100f * densidad * escalaTexto
        val debeApilar = ancho < anchoMinimo
        if (apilada != debeApilar) {
            apilada = debeApilar
            orientation = if (debeApilar) VERTICAL else HORIZONTAL
            val espacio = (10f * densidad).toInt()
            for (i in 0 until childCount) {
                val tarjeta = getChildAt(i)
                val medidas = tarjeta.layoutParams as LayoutParams
                medidas.width = if (debeApilar) LayoutParams.MATCH_PARENT else 0
                medidas.height = LayoutParams.WRAP_CONTENT
                medidas.weight = if (debeApilar) 0f else 1f
                medidas.marginStart = if (!debeApilar && i > 0) espacio else 0
                medidas.topMargin = if (debeApilar && i > 0) espacio else 0
                tarjeta.layoutParams = medidas
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
