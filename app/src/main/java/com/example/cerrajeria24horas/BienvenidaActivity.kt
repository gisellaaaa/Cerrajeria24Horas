package com.example.cerrajeria24horas

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.cerrajeria24horas.adapters.BienvenidaServiciosAdapter
import com.example.cerrajeria24horas.util.Movimiento
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.auth.FirebaseAuth

/**
 * Portada de la app: se muestra siempre al abrir. Presenta los servicios en un carrusel que
 * avanza solo, ofrece registrarse o iniciar sesión y enlaza a la página web. "Iniciar sesión"
 * lleva directo al panel si ya hay una sesión activa; si no, abre la pantalla de acceso.
 */
class BienvenidaActivity : BaseActivity() {

    private companion object {
        const val URL_WEB = "https://www.cerrajeria24horas.com.co"
        const val INTERVALO_MS = 4000L
    }

    private lateinit var pager: ViewPager2
    private lateinit var adaptador: BienvenidaServiciosAdapter
    private lateinit var indicador: LinearLayout
    private val manejador = Handler(Looper.getMainLooper())
    private val avanzar = object : Runnable {
        override fun run() {
            if (adaptador.total > 1) {
                val siguiente = (pager.currentItem + 1) % adaptador.total
                pager.setCurrentItem(siguiente, Movimiento.activo(this@BienvenidaActivity))
            }
            manejador.postDelayed(this, INTERVALO_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)
        UI.aplicarMarca(findViewById(R.id.tvMarcaBienvenida))

        pager = findViewById(R.id.pagerBienvenida)
        indicador = findViewById(R.id.indicadorBienvenida)
        adaptador = BienvenidaServiciosAdapter()
        pager.adapter = adaptador
        pager.offscreenPageLimit = 3
        pager.setPageTransformer { pagina, posicion ->
            val cercania = 1f - kotlin.math.abs(posicion).coerceAtMost(1f)
            val escala = 0.82f + 0.18f * cercania
            pagina.scaleX = escala
            pagina.scaleY = escala
            pagina.alpha = 0.3f + 0.7f * cercania
        }
        crearPuntos(indicador, adaptador.total)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                actualizarPuntos(indicador, position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    manejador.removeCallbacks(avanzar)
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    programarAvance()
                }
            }
        })

        findViewById<View>(R.id.btnBienvenidaIniciar).setOnClickListener { irAlAcceso() }
        findViewById<View>(R.id.btnBienvenidaCrear).setOnClickListener {
            abrirActividad(RegisterActivity::class.java)
        }
        findViewById<View>(R.id.btnBienvenidaWeb).setOnClickListener { abrirWeb() }
    }

    override fun onResume() {
        super.onResume()
        programarAvance()
    }

    override fun onPause() {
        super.onPause()
        manejador.removeCallbacks(avanzar)
    }

    /**
     * El carrusel avanza solo siempre. Con "Reducir movimiento" activo el cambio es instantáneo
     * (sin deslizamiento) para no forzar animación, pero sigue siendo automático.
     */
    private fun programarAvance() {
        manejador.removeCallbacks(avanzar)
        if (adaptador.total > 1) {
            manejador.postDelayed(avanzar, INTERVALO_MS)
        }
    }

    private fun irAlAcceso() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            abrirRaiz(MainActivity::class.java)
        } else {
            abrirActividad(LoginActivity::class.java)
        }
    }

    private fun abrirWeb() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_WEB)))
        } catch (_: ActivityNotFoundException) {
            // Sin navegador instalado no hay nada que abrir.
        }
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
        for (posicion in 0 until contenedor.childCount) {
            val punto = contenedor.getChildAt(posicion)
            val seleccionado = posicion == activo
            punto.alpha = if (seleccionado) 1f else 0.35f
            punto.scaleX = if (seleccionado) 1.2f else 1f
            punto.scaleY = if (seleccionado) 1.2f else 1f
        }
    }
}
