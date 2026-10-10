package com.example.cerrajeria24horas.util

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cerrajeria24horas.R

/** Ajustes visuales compartidos; no registra eventos de navegación ni de datos. */
object Interfaz {

    @Suppress("DEPRECATION")
    fun configurarBarras(activity: Activity) {
        val ventana = activity.window
        WindowCompat.setDecorFitsSystemWindows(ventana, false)
        ventana.statusBarColor = Color.TRANSPARENT
        // Android 7 necesita fondo oscuro porque no admite iconos oscuros abajo.
        ventana.navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Color.TRANSPARENT
        } else {
            activity.getColor(R.color.azul_marino_oscuro)
        }
        WindowCompat.getInsetsController(ventana, ventana.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }
    }

    fun ajustarContenido(
        raiz: View,
        recursoAnchoMaximo: Int,
        alCambiarTeclado: ((Int) -> Unit)? = null
    ) {
        val anchoMaximo = raiz.resources.getDimensionPixelSize(recursoAnchoMaximo)
        val inicial = Insets.of(raiz.paddingLeft, raiz.paddingTop, raiz.paddingRight, raiz.paddingBottom)
        var barras = Insets.NONE
        var altoTeclado = 0
        var altoTecladoPrevio = -1

        fun aplicarMargenes() {
            val anchoUtil = (raiz.width - barras.left - barras.right).coerceAtLeast(0)
            val margenLateral = ((anchoUtil - anchoMaximo) / 2).coerceAtLeast(0)
            val izquierda = inicial.left + barras.left + margenLateral
            val arriba = inicial.top + barras.top
            val derecha = inicial.right + barras.right + margenLateral
            val abajo = inicial.bottom + maxOf(barras.bottom, altoTeclado)
            if (raiz.paddingLeft != izquierda || raiz.paddingTop != arriba ||
                raiz.paddingRight != derecha || raiz.paddingBottom != abajo) {
                raiz.setPadding(izquierda, arriba, derecha, abajo)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(raiz) { _, insets ->
            barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            altoTeclado = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            aplicarMargenes()
            if (altoTeclado != altoTecladoPrevio) {
                altoTecladoPrevio = altoTeclado
                alCambiarTeclado?.invoke(altoTeclado)
            }
            WindowInsetsCompat.CONSUMED
        }
        // Se recalcula con el ancho real, también en pantalla dividida y ventanas.
        raiz.addOnLayoutChangeListener { _, left, _, right, _, oldLeft, _, oldRight, _ ->
            if (right - left != oldRight - oldLeft) aplicarMargenes()
        }
        raiz.post { ViewCompat.requestApplyInsets(raiz) }
    }
}
