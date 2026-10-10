package com.example.cerrajeria24horas.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
 * Barra de navegación flotante con una muesca real en el centro, recortada con
 * [Path.op] sobre el cuerpo redondeado. El botón central sobresale por el hueco.
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
    var alAccionCentral: (() -> Unit)? = null

    var destinoActual: Destino = Destino.INICIO
        private set

    private val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colorBarra = ContextCompat.getColor(context, R.color.nav_fondo)
    private val colorActivo = ContextCompat.getColor(context, R.color.white)
    private val colorInactivo = ContextCompat.getColor(context, R.color.nav_texto_inactivo)
    private val colorAcento = ContextCompat.getColor(context, R.color.azul_marino)
    private val colorSombra = ContextCompat.getColor(context, R.color.onda_oscura)

    private val densidad = resources.displayMetrics.density
    private val realce = resources.getDimension(R.dimen.realce_barra_navegacion)
    private val radioNotch = resources.getDimension(R.dimen.notch_barra_navegacion)
    private val radioBarra = resources.getDimension(R.dimen.radio_barra_navegacion)

    private val items = mutableMapOf<Destino, LinearLayout>()

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        pintura.setShadowLayer(6f * densidad, 0f, 3f * densidad, colorSombra)

        LayoutInflater.from(context).inflate(R.layout.barra_navegacion, this, true)

        enlazar(R.id.navInicio, Destino.INICIO)
        enlazar(R.id.navServicios, Destino.SERVICIOS)
        enlazar(R.id.navClientes, Destino.CLIENTES)
        enlazar(R.id.navGanancias, Destino.GANANCIAS)
        enlazar(R.id.navEquipo, Destino.EQUIPO)

        findViewById<ImageView>(R.id.btnCentral).apply {
            imageTintList = ColorStateList.valueOf(colorAcento)
            setOnClickListener { alAccionCentral?.invoke() }
        }

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

    /** Muestra u oculta el botón central de "Nuevo servicio". */
    fun mostrarAccionCentral(visible: Boolean) {
        findViewById<ImageView>(R.id.btnCentral).visibility = if (visible) View.VISIBLE else View.GONE
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val ancho = width.toFloat()
        val alto = height.toFloat()
        if (ancho <= 0f || alto <= 0f) return

        val barra = Path().apply {
            addRoundRect(0f, realce, ancho, alto, radioBarra, radioBarra, Path.Direction.CW)
        }
        val muesca = Path().apply {
            addCircle(ancho / 2f, realce, radioNotch, Path.Direction.CW)
        }
        barra.op(muesca, Path.Op.DIFFERENCE)

        pintura.color = colorBarra
        canvas.drawPath(barra, pintura)
    }
}
