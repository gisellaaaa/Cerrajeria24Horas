package com.example.cerrajeria24horas.util

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.PathInterpolator
import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * Único punto de entrada para el movimiento de la app.
 *
 * Ninguna animación se crea suelta: todas pasan por aquí para respetar
 * "Reducir movimiento" y la escala de animaciones del sistema. Ninguna dura
 * más de [LARGA] (450 ms) ni bloquea la interacción.
 */
object Movimiento {

    const val CORTA = 120L
    const val MEDIA = 250L
    const val LARGA = 450L
    const val ESCALON = 60L
    const val CONTADOR = 600L
    const val GRAFICO = 700L

    val decelerar: TimeInterpolator = PathInterpolator(0.05f, 0.7f, 0.1f, 1f)
    val acelerar: TimeInterpolator = PathInterpolator(0.3f, 0f, 0.8f, 0.15f)
    val estandar: TimeInterpolator = PathInterpolator(0.2f, 0f, 0f, 1f)

    /** Escala de animaciones del sistema; 0 significa "sin animaciones". */
    fun escalaAnimaciones(context: Context): Float = try {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
    } catch (_: Settings.SettingNotFoundException) {
        1f
    }

    /** Falso si el usuario pidió reducir el movimiento o el sistema no anima. */
    fun activo(context: Context): Boolean =
        !Preferencias.reducirMovimiento(context) && escalaAnimaciones(context) > 0f

    /** Duración efectiva: 0 cuando el movimiento está desactivado (solo fundidos cortos). */
    fun duracion(base: Long, activo: Boolean): Long = if (activo) base else 0L

    /** Entrada de pantalla: los elementos suben 24dp con fundido, escalonados 60 ms. */
    fun entradaEscalonada(vistas: List<View>, desplazamientoDp: Float = 24f) {
        vistas.forEachIndexed { indice, vista ->
            if (!activo(vista.context)) {
                vista.alpha = 0f
                vista.animate().alpha(1f).setDuration(CORTA).start()
                return@forEachIndexed
            }
            val dy = desplazamientoDp * vista.resources.displayMetrics.density
            vista.alpha = 0f
            vista.translationY = dy
            vista.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(indice * ESCALON)
                .setDuration(MEDIA)
                .setInterpolator(decelerar)
                .start()
        }
    }

    /** Cifras: contador 0→valor en 600 ms. Con montos ocultos no se llama. */
    fun contador(textView: TextView, hasta: Double, formato: (Double) -> String) {
        if (!activo(textView.context)) {
            textView.text = formato(hasta)
            return
        }
        val animador = ValueAnimator.ofFloat(0f, hasta.toFloat())
        animador.duration = CONTADOR
        animador.interpolator = decelerar
        animador.addUpdateListener { textView.text = formato((it.animatedValue as Float).toDouble()) }
        animador.start()
    }

    /** Resorte de presión a 0.96: reemplaza elevacion_boton y presion_tarjeta. */
    fun presionar(view: View) {
        val escala = 0.96f
        var animX: SpringAnimation? = null
        var animY: SpringAnimation? = null

        fun animar(destino: Float) {
            if (!activo(view.context) || !view.isAttachedToWindow) {
                view.scaleX = destino
                view.scaleY = destino
                return
            }
            animX?.cancel()
            animY?.cancel()
            animX = SpringAnimation(view, DynamicAnimation.SCALE_X, destino).apply {
                spring = SpringForce(destino)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW)
                start()
            }
            animY = SpringAnimation(view, DynamicAnimation.SCALE_Y, destino).apply {
                spring = SpringForce(destino)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW)
                start()
            }
        }

        view.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> animar(escala)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> animar(1f)
            }
            false
        }
    }

    /** Desliza una ficha indicadora con resorte (barra de navegación). */
    fun deslizarA(view: View, destinoX: Float) {
        if (!activo(view.context)) {
            view.translationX = destinoX
            return
        }
        SpringAnimation(view, DynamicAnimation.TRANSLATION_X, destinoX).apply {
            spring = SpringForce(destinoX)
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW)
            start()
        }
    }

    /** Giro de llave: única animación "de personalidad", con el icono genérico ic_key. */
    fun girarLlave(view: View) {
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
        )
        if (!activo(view.context)) {
            view.rotation = 0f
            view.alpha = 1f
            return
        }
        view.rotation = -90f
        view.alpha = 0.4f
        view.animate()
            .rotation(0f)
            .alpha(1f)
            .setDuration(LARGA)
            .setInterpolator(decelerar)
            .start()
    }

    /** Muestra u oculta una vista con un fundido corto (para encoger la cabecera del acceso). */
    fun fundir(vista: View, visible: Boolean) {
        vista.animate().cancel()
        if (visible) {
            vista.visibility = View.VISIBLE
            if (!activo(vista.context)) {
                vista.alpha = 1f
                return
            }
            vista.alpha = 0f
            vista.animate().alpha(1f).setDuration(CORTA).setInterpolator(decelerar).start()
        } else {
            if (!activo(vista.context)) {
                vista.visibility = View.GONE
                return
            }
            vista.animate().alpha(0f).setDuration(CORTA).setInterpolator(acelerar)
                .withEndAction { vista.visibility = View.GONE }.start()
        }
    }

    /** Pequeño pulso de escala (por ejemplo, al completar un punto del PIN). */
    fun pulso(vista: View) {
        if (!activo(vista.context)) return
        vista.animate().cancel()
        vista.scaleX = 1.12f
        vista.scaleY = 1.12f
        vista.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(CORTA)
            .setInterpolator(decelerar)
            .start()
    }

    /** Error: sacudida de 250 ms. */
    fun sacudir(view: View) {
        if (!activo(view.context)) return
        val d = 8f * view.resources.displayMetrics.density
        val animador = ObjectAnimator.ofFloat(view, "translationX", 0f, -d, d, -d * 0.75f, d * 0.75f, 0f)
        animador.duration = MEDIA
        animador.interpolator = estandar
        animador.start()
    }

    /** Aparición con fundido + escala 0.92→1 (sin deformar). */
    fun dibujarDe0(view: View) {
        if (!activo(view.context)) {
            view.alpha = 1f
            view.scaleX = 1f
            view.scaleY = 1f
            return
        }
        view.scaleX = 0.92f
        view.scaleY = 0.92f
        view.alpha = 0f
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(LARGA)
            .setInterpolator(decelerar)
            .start()
    }

    /** Extensión para marcar cualquier vista como presionable desde un único punto. */
    fun View.conPresion() = presionar(this)
}
