package com.example.cerrajeria24horas

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import com.example.cerrajeria24horas.util.Preferencias
import kotlin.math.roundToInt

/**
 * Bloqueo por PIN de 4 dígitos. El PIN se guarda solo como huella SHA-256 en el dispositivo,
 * nunca como texto plano. Cubre desbloqueo, creación y eliminación del PIN.
 */
class BloqueoActivity : BaseActivity() {

    companion object {
        const val MODO = "modo"
        const val MODO_DESBLOQUEO = "desbloqueo"
        const val MODO_CONFIGURAR = "configurar"
        const val MODO_QUITAR = "quitar"

        private const val LARGO_PIN = 4
        private const val BORRAR = "borrar"

        /** Desbloqueado mientras la app está abierta; se pierde al salir del proceso. */
        var desbloqueado = false
            private set

        /** Al cerrar sesión el siguiente acceso vuelve a pedir el PIN. */
        fun olvidarDesbloqueo() {
            desbloqueado = false
        }

        fun abrir(actividad: android.app.Activity, modo: String) {
            actividad.startActivity(
                android.content.Intent(actividad, BloqueoActivity::class.java)
                    .putExtra(MODO, modo)
            )
            if (modo != MODO_DESBLOQUEO) {
                actividad.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private lateinit var tvTitulo: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var tvError: TextView
    private var modo = MODO_DESBLOQUEO
    private var pinIngresado = StringBuilder()
    private var pinNuevo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modo = intent.getStringExtra(MODO) ?: MODO_DESBLOQUEO
        setContentView(R.layout.activity_bloqueo)

        // El teclado atrás no permite escapar del bloqueo.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (modo != MODO_DESBLOQUEO) finish()
            }
        })

        tvTitulo = findViewById(R.id.tvBloqueoTitulo)
        tvPuntos = findViewById(R.id.tvBloqueoPuntos)
        tvError = findViewById(R.id.tvBloqueoError)

        tvTitulo.setText(
            when (modo) {
                MODO_CONFIGURAR -> R.string.pin_crear
                MODO_QUITAR -> R.string.pin_quitar
                else -> R.string.pin_desbloquear
            }
        )

        crearTeclado(findViewById(R.id.llTeclado))
        mostrarPuntos()
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).roundToInt()

    private fun crearTeclado(contenedor: LinearLayout) {
        val filas = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(BORRAR, "0", "")
        )

        filas.forEach { fila ->
            val linea = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    .apply { topMargin = dp(10) }
            }
            fila.forEach { tecla ->
                val margen = dp(6)
                val altoTecla = dp(64)
                linea.addView(
                    crearTecla(tecla),
                    LinearLayout.LayoutParams(0, altoTecla, 1f).apply {
                        marginStart = margen
                        marginEnd = margen
                    }
                )
            }
            contenedor.addView(linea)
        }
    }

    private fun crearTecla(tecla: String): TextView = TextView(this).apply {
        val esBorrar = tecla == BORRAR
        text = if (esBorrar) getString(R.string.pin_borrar) else tecla
        gravity = Gravity.CENTER
        textSize = if (esBorrar) 15f else 24f
        setTextColor(getColor(R.color.texto_principal))
        typeface = ResourcesCompat.getFont(this@BloqueoActivity, R.font.dm_sans_semibold)
        setBackgroundResource(R.drawable.bg_tecla_pin)
        isClickable = true
        isFocusable = true
        setOnClickListener { alPulsar(tecla) }
    }

    private fun alPulsar(tecla: String) {
        when (tecla) {
            BORRAR -> {
                if (pinIngresado.isNotEmpty()) {
                    pinIngresado.deleteCharAt(pinIngresado.length - 1)
                    tvError.text = ""
                    mostrarPuntos()
                }
            }

            "" -> Unit
            else -> {
                if (pinIngresado.length < LARGO_PIN) {
                    pinIngresado.append(tecla)
                    tvError.text = ""
                    mostrarPuntos()
                    if (pinIngresado.length == LARGO_PIN) validar()
                }
            }
        }
    }

    private fun mostrarPuntos() {
        val llenos = pinIngresado.length
        tvPuntos.text = "●".repeat(llenos) + "○".repeat(LARGO_PIN - llenos)
    }

    private fun reiniciar(mensaje: String) {
        pinIngresado = StringBuilder()
        tvError.text = mensaje
        mostrarPuntos()
    }

    private fun validar() {
        when (modo) {
            MODO_CONFIGURAR -> validarConfiguracion()
            MODO_QUITAR -> validarQuitar()
            else -> validarDesbloqueo()
        }
    }

    private fun validarConfiguracion() {
        if (pinNuevo.isEmpty()) {
            pinNuevo = pinIngresado.toString()
            reiniciar(getString(R.string.pin_confirmar))
        } else if (pinNuevo == pinIngresado.toString()) {
            Preferencias.guardarPin(this, pinNuevo)
            Toast.makeText(this, R.string.pin_guardado, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            pinNuevo = ""
            reiniciar(getString(R.string.pin_no_coincide))
        }
    }

    private fun validarQuitar() {
        if (Preferencias.pinCorrecto(this, pinIngresado.toString())) {
            Preferencias.quitarPin(this)
            Toast.makeText(this, R.string.pin_quitado, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            reiniciar(getString(R.string.pin_incorrecto))
        }
    }

    private fun validarDesbloqueo() {
        if (Preferencias.pinCorrecto(this, pinIngresado.toString())) {
            desbloqueado = true
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            reiniciar(getString(R.string.pin_incorrecto))
        }
    }
}