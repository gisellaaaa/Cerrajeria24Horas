package com.example.cerrajeria24horas

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.Interfaz
import com.example.cerrajeria24horas.util.Preferencias
import com.google.firebase.auth.FirebaseAuth

abstract class BaseActivity : AppCompatActivity() {

    init {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                animar(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Interfaz.configurarBarras(this)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        val contenido = findViewById<ViewGroup>(android.R.id.content)
        val raiz = contenido.getChildAt(0) ?: return
        val anchoMaximo = when (layoutResID) {
            R.layout.activity_login -> R.dimen.ancho_maximo_acceso
            R.layout.activity_registro,
            R.layout.activity_cliente_form,
            R.layout.activity_servicio_form -> R.dimen.ancho_maximo_formulario
            else -> R.dimen.ancho_maximo_contenido
        }
        Interfaz.ajustarContenido(raiz, anchoMaximo) { altoTeclado ->
            alCambiarTeclado(altoTeclado > 0)
        }
    }

    /** Aviso cuando el teclado en pantalla aparece o desaparece, para encoger la cabecera. */
    protected open fun alCambiarTeclado(tecladoVisible: Boolean) {}

    override fun onResume() {
        super.onResume()
        if (revisarSesionEnSeguridad()) {
            revisarInactividad()
        } else {
            Interaccion.marcar()
        }
    }

    /** Solo las pantallas con sesión iniciada aplican el cierre automático. */
    protected open fun revisarSesionEnSeguridad() = false

    private fun revisarInactividad() {
        val limite = Preferencias.tiempoSesionMinutos(this)
        if (limite <= 0 || Interaccion.minutosInactivos() < limite) {
            Interaccion.marcar()
            return
        }
        FirebaseAuth.getInstance().signOut()
        Interaccion.reiniciar()
        Toast.makeText(this, R.string.sesion_cerrada_automatica, Toast.LENGTH_SHORT).show()
        abrirRaiz(LoginActivity::class.java)
    }

    protected fun configurarEncabezado(titulo: String) {
        findViewById<TextView>(R.id.encTitle)?.text = titulo
        findViewById<ImageButton>(R.id.encBack)?.setOnClickListener { volverAtras() }
    }

    private fun volverAtras() {
        finish()
        animar(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    protected fun abrirActividad(actividad: Class<*>) {
        startActivity(Intent(this, actividad))
        animar(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    protected fun abrirRaiz(actividad: Class<*>) {
        val intent = Intent(this, actividad)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        animar(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    /** Con "Reducir movimiento" activado se usan fundidos suaves en lugar de desplazamientos. */
    private fun animar(entrada: Int, salida: Int) {
        if (Preferencias.reducirMovimiento(this)) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            overridePendingTransition(entrada, salida)
        }
    }

    protected fun mostrarDialogoCerrarSesion(accionCerrar: () -> Unit) {
        val dialogo = Dialog(this, R.style.Theme_Cerrajeria24Horas_Dialogo)
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo.setContentView(R.layout.dialogo_cerrar_sesion)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.findViewById<TextView>(R.id.dlgTitulo)?.text = getString(R.string.cerrar_sesion_confirmar)
        dialogo.findViewById<View>(R.id.btnDlgCancelar)?.setOnClickListener { dialogo.dismiss() }
        dialogo.findViewById<View>(R.id.btnDlgConfirmar)?.setOnClickListener {
            dialogo.dismiss()
            accionCerrar()
        }
        dialogo.show()

        // Se mide la ventana disponible, no el tamaño total del teléfono.
        val areaVisible = Rect()
        window.decorView.getWindowVisibleDisplayFrame(areaVisible)
        val margen = resources.getDimensionPixelSize(R.dimen.margen_dialogo)
        val anchoDisponible = areaVisible.width().takeIf { it > 0 }
            ?: resources.displayMetrics.widthPixels
        val altoDisponible = areaVisible.height().takeIf { it > 0 }
            ?: resources.displayMetrics.heightPixels
        val ancho = minOf(
            resources.getDimensionPixelSize(R.dimen.ancho_maximo_dialogo),
            (anchoDisponible - margen * 2).coerceAtLeast(1)
        )
        val altoMaximo = (altoDisponible - margen * 2).coerceAtLeast(1)
        val contenido = dialogo.findViewById<View>(R.id.dlgContenido)
        contenido.measure(
            View.MeasureSpec.makeMeasureSpec(ancho, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(altoMaximo, View.MeasureSpec.AT_MOST)
        )
        // El contenido puede desplazarse si se usa letra grande o poca altura.
        dialogo.window?.setLayout(ancho, contenido.measuredHeight)
    }
}
