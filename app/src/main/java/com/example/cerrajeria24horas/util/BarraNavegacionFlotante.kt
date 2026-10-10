package com.example.cerrajeria24horas.util

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cerrajeria24horas.R

/**
 * Barra de navegación flotante: una píldora redondeada con cinco destinos de
 * igual ancho y una pastilla que resalta el destino activo. El botón de
 * "Nuevo servicio" ya no vive dentro de la barra: se muestra como un botón
 * circular flotante en la esquina, igual que el acceso rápido de la web.
 */
class BarraNavegacionFlotante @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class Destino(val etiquetaRes: Int, val iconoRes: Int) {
        INICIO(R.string.menu_inicio, R.drawable.ic_inicio),
        SERVICIOS(R.string.menu_servicios, R.drawable.ic_llave_doble),
        CLIENTES(R.string.menu_clientes, R.drawable.ic_person),
        GANANCIAS(R.string.menu_ganancias, R.drawable.ic_grafico),
        EQUIPO(R.string.menu_equipo, R.drawable.ic_equipo)
    }

    var alSeleccionar: ((Destino) -> Unit)? = null

    var destinoActual: Destino = Destino.INICIO
        private set

    private val colorActivo = ContextCompat.getColor(context, R.color.white)
    private val colorInactivo = ContextCompat.getColor(context, R.color.nav_texto_inactivo)

    private val items = mutableMapOf<Destino, LinearLayout>()

    init {
        LayoutInflater.from(context).inflate(R.layout.barra_navegacion, this, true)

        enlazar(R.id.navInicio, Destino.INICIO)
        enlazar(R.id.navServicios, Destino.SERVICIOS)
        enlazar(R.id.navClientes, Destino.CLIENTES)
        enlazar(R.id.navGanancias, Destino.GANANCIAS)
        enlazar(R.id.navEquipo, Destino.EQUIPO)

        aplicar(Destino.INICIO)
    }

    private fun enlazar(id: Int, destino: Destino) {
        val vista = findViewById<LinearLayout>(id)
        items[destino] = vista
        vista.contentDescription = context.getString(destino.etiquetaRes)
        vista.findViewById<ImageView>(R.id.navIcono).setImageResource(destino.iconoRes)
        vista.findViewById<TextView>(R.id.navTexto).setText(destino.etiquetaRes)
        vista.setOnClickListener {
            if (destinoActual != destino) {
                aplicar(destino)
                alSeleccionar?.invoke(destino)
            }
        }
    }

    /** Muestra u oculta una pestaña según el rol del usuario (por ejemplo, Equipo solo para el admin). */
    fun mostrarItem(destino: Destino, visible: Boolean) {
        items[destino]?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /** Permite que la pantalla anfitriona cambie de pestaña (por ejemplo, al volver atrás). */
    fun mostrar(destino: Destino) {
        if (destinoActual != destino) aplicar(destino)
    }

    private fun aplicar(destino: Destino) {
        destinoActual = destino
        items.forEach { (d, vista) ->
            val activo = d == destino
            val color = if (activo) colorActivo else colorInactivo
            val pildora = vista.findViewById<FrameLayout>(R.id.navPildora)
            val icono = vista.findViewById<ImageView>(R.id.navIcono)
            val texto = vista.findViewById<TextView>(R.id.navTexto)
            pildora.background =
                if (activo) ContextCompat.getDrawable(context, R.drawable.bg_indicador_nav) else null
            icono.imageTintList = ColorStateList.valueOf(color)
            texto.setTextColor(color)
        }
    }
}
