package com.example.cerrajeria24horas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.cerrajeria24horas.adapters.OnboardingAdapter
import com.example.cerrajeria24horas.util.Preferencias

/**
 * Pantalla de bienvenida que se muestra una sola vez, en el primer arranque de la app.
 * Después deja el paso a la pantalla de acceso (LoginActivity).
 */
class BienvenidaActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Preferencias.onboardingVisto(this)) {
            irAlAcceso()
            return
        }

        setContentView(R.layout.activity_bienvenida)

        val pager = findViewById<ViewPager2>(R.id.pagerBienvenida)
        val adaptador = OnboardingAdapter()
        pager.adapter = adaptador

        val indicador = findViewById<LinearLayout>(R.id.indicadorBienvenida)
        crearPuntos(indicador, adaptador.total)

        val btnSiguiente = findViewById<Button>(R.id.btnBienvenidaSiguiente)
        val btnSaltar = findViewById<Button>(R.id.btnBienvenidaSaltar)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                actualizarPuntos(indicador, position)
                val ultima = position == adaptador.total - 1
                btnSiguiente.setText(
                    if (ultima) R.string.onboarding_comenzar else R.string.onboarding_siguiente
                )
                btnSaltar.visibility = if (ultima) View.GONE else View.VISIBLE
            }
        })

        btnSiguiente.setOnClickListener {
            val siguiente = pager.currentItem + 1
            if (siguiente < adaptador.total) {
                pager.setCurrentItem(siguiente, true)
            } else {
                finalizar()
            }
        }
        btnSaltar.setOnClickListener { finalizar() }
    }

    private fun finalizar() {
        Preferencias.guardarOnboardingVisto(this)
        irAlAcceso()
    }

    private fun irAlAcceso() {
        abrirRaiz(LoginActivity::class.java)
    }

    private fun crearPuntos(contenedor: LinearLayout, total: Int) {
        contenedor.removeAllViews()
        val tamano = resources.getDimensionPixelSize(R.dimen.punto_onboarding)
        val margen = resources.getDimensionPixelSize(R.dimen.espacio_8)
        repeat(total) {
            val punto = View(this)
            val parametros = LinearLayout.LayoutParams(tamano, tamano)
            parametros.marginStart = margen
            parametros.marginEnd = margen
            punto.layoutParams = parametros
            punto.setBackgroundResource(R.drawable.bg_punto_onboarding)
            contenedor.addView(punto)
        }
        actualizarPuntos(contenedor, 0)
    }

    private fun actualizarPuntos(contenedor: LinearLayout, activo: Int) {
        for (indice in 0 until contenedor.childCount) {
            val punto = contenedor.getChildAt(indice)
            val seleccionado = indice == activo
            punto.alpha = if (seleccionado) 1f else 0.35f
            punto.scaleX = if (seleccionado) 1.2f else 1f
            punto.scaleY = if (seleccionado) 1.2f else 1f
        }
    }
}
