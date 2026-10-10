package com.example.cerrajeria24horas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.cerrajeria24horas.util.FotoPerfil
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.Movimiento
import com.example.cerrajeria24horas.util.Preferencias
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class AjustesActivity : BaseActivity() {

    private val selectorFoto = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val usuario = FirebaseAuth.getInstance().currentUser ?: return@registerForActivityResult
        uri ?: return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
        }
        FotoPerfil.guardar(this, usuario.uid, uri)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.foto_perfil_guardada, Toast.LENGTH_SHORT).show()
                actualizarEstadoFoto()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_foto_perfil, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)
        configurarEncabezado(getString(R.string.titulo_ajustes))

        val swNotificaciones = findViewById<SwitchMaterial>(R.id.swNotificaciones)
        swNotificaciones.isChecked = Preferencias.notificaciones(this)
        swNotificaciones.setOnCheckedChangeListener { _, marcado ->
            Preferencias.guardarNotificaciones(this, marcado)
        }

        val swReducirMovimiento = findViewById<SwitchMaterial>(R.id.swReducirMovimiento)
        swReducirMovimiento.isChecked = Preferencias.reducirMovimiento(this)
        swReducirMovimiento.setOnCheckedChangeListener { _, marcado ->
            Preferencias.guardarReducirMovimiento(this, marcado)
        }

        mostrarSeguridad()
        mostrarPrivacidad()
        prepararSesion()

        findViewById<View>(R.id.btnAjustesFoto).setOnClickListener {
            selectorFoto.launch(arrayOf("image/*"))
        }
        findViewById<View>(R.id.btnAjustesCambiarCuenta).setOnClickListener { cambiarCuenta() }
        findViewById<View>(R.id.btnAjustesCerrar).setOnClickListener {
            mostrarDialogoCerrarSesion {
                FirebaseAuth.getInstance().signOut()
                BloqueoActivity.olvidarDesbloqueo()
                Interaccion.reiniciar()
                Toast.makeText(this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show()
                abrirRaiz(LoginActivity::class.java)
            }
        }

        animarEntrada()
    }

    override fun onResume() {
        super.onResume()
        actualizarEstadoFoto()
        // Los valores del PIN y del cierre automático pueden haber cambiado.
        mostrarSeguridad()
        mostrarPrivacidad()
        marcarSesion()
    }

    /** Aparece cada tarjeta con su desplazamiento escalonado (respeta "Reducir movimiento"). */
    private fun animarEntrada() {
        val tarjetas = listOf(
            R.id.cardCuenta,
            R.id.cardSeguridad,
            R.id.cardSesion,
            R.id.cardPrivacidad,
            R.id.cardDispositivo,
            R.id.btnAjustesCerrar
        ).mapNotNull { findViewById<View>(it) }
        Movimiento.entradaEscalonada(tarjetas)
    }

    /** El botón dice "Agregar" o "Cambiar" según haya foto, y se mantiene al volver a la pantalla. */
    private fun actualizarEstadoFoto() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FotoPerfil.obtener(uid) { base64 ->
            findViewById<MaterialButton>(R.id.btnAjustesFoto)?.text = getString(
                if (base64.isNullOrBlank()) R.string.agregar_foto_perfil
                else R.string.cambiar_foto_perfil
            )
        }
    }

    private fun cambiarCuenta() {
        FirebaseAuth.getInstance().signOut()
        BloqueoActivity.olvidarDesbloqueo()
        Interaccion.reiniciar()
        Toast.makeText(this, R.string.sesion_cerrada_cambio, Toast.LENGTH_SHORT).show()
        abrirRaiz(LoginActivity::class.java)
    }

    // ---------- Seguridad ----------

    private fun mostrarSeguridad() {
        val contenedor = findViewById<LinearLayout>(R.id.llSeguridad)
        contenedor.removeAllViews()

        agregarFila(
            contenedor,
            R.drawable.ic_key,
            R.string.seg_cambiar_contrasena,
            R.string.seg_cambiar_contrasena_descripcion,
            null
        ) { mostrarDialogoContrasena() }

        agregarFila(
            contenedor,
            R.drawable.ic_candado,
            R.string.seg_pin,
            R.string.seg_pin_descripcion,
            getString(
                if (Preferencias.tienePin(this)) R.string.seg_pin_activo
                else R.string.seg_pin_inactivo
            )
        ) { abrirAjustePin() }
    }

    /** Botones de tiempo del cierre automático: se redondean como pastillas independientes. */
    private val botonesSesion = listOf(
        R.id.btnSesionNunca,
        R.id.btnSesion5,
        R.id.btnSesion15,
        R.id.btnSesion30
    )

    private fun prepararSesion() {
        botonesSesion.forEach { id ->
            findViewById<MaterialButton>(id)?.setOnClickListener {
                marcarSesion(id)
                Preferencias.guardarTiempoSesion(this, minutosDeSesion(id))
            }
        }
        marcarSesion()
    }

    private fun marcarSesion() {
        marcarSesion(idDeSesion(Preferencias.tiempoSesionMinutos(this)))
    }

    private fun marcarSesion(id: Int) {
        botonesSesion.forEach { findViewById<MaterialButton>(it)?.isChecked = it == id }
    }

    private fun idDeSesion(minutos: Int): Int = when (minutos) {
        5 -> R.id.btnSesion5
        15 -> R.id.btnSesion15
        30 -> R.id.btnSesion30
        else -> R.id.btnSesionNunca
    }

    private fun minutosDeSesion(id: Int): Int = when (id) {
        R.id.btnSesion5 -> 5
        R.id.btnSesion15 -> 15
        R.id.btnSesion30 -> 30
        else -> 0
    }

    private fun abrirAjustePin() {
        if (!Preferencias.tienePin(this)) {
            BloqueoActivity.abrir(this, BloqueoActivity.MODO_CONFIGURAR)
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.seg_pin)
            .setItems(
                arrayOf(
                    getString(R.string.seg_cambiar_pin),
                    getString(R.string.seg_quitar_pin)
                )
            ) { _, cual ->
                if (cual == 0) {
                    BloqueoActivity.abrir(this, BloqueoActivity.MODO_CONFIGURAR)
                } else {
                    BloqueoActivity.abrir(this, BloqueoActivity.MODO_QUITAR)
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun mostrarDialogoContrasena() {
        val contenido = layoutInflater.inflate(R.layout.dialogo_cambiar_contrasena, null)
        val campoActual = contenido.findViewById<TextInputLayout>(R.id.tilContrasenaActual)
        val campoNueva = contenido.findViewById<TextInputLayout>(R.id.tilContrasenaNueva)
        val campoConfirmacion = contenido.findViewById<TextInputLayout>(R.id.tilContrasenaConfirmacion)
        val etActual = contenido.findViewById<TextInputEditText>(R.id.etContrasenaActual)
        val etNueva = contenido.findViewById<TextInputEditText>(R.id.etContrasenaNueva)
        val etConfirmacion = contenido.findViewById<TextInputEditText>(R.id.etContrasenaConfirmacion)

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_cambiar_contrasena)
            .setView(contenido)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.guardar, null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val actual = etActual.text?.toString().orEmpty()
                val nueva = etNueva.text?.toString().orEmpty()
                val confirmacion = etConfirmacion.text?.toString().orEmpty()

                campoActual.error = null
                campoNueva.error = null
                campoConfirmacion.error = null

                if (actual.isEmpty()) {
                    campoActual.error = getString(R.string.contrasena_actual_requerida)
                } else if (nueva.length < 6) {
                    campoNueva.error = getString(R.string.contrasena_minima)
                } else if (nueva != confirmacion) {
                    campoConfirmacion.error = getString(R.string.contrasena_nueva_no_coincide)
                } else {
                    actualizarContrasena(actual, nueva, dialogo)
                }
            }
        }
        dialogo.show()
    }

    /** Firebase pide la contraseña actual antes de permitir cambiarla. */
    private fun actualizarContrasena(actual: String, nueva: String, dialogo: android.app.Dialog) {
        val usuario = FirebaseAuth.getInstance().currentUser
        val correo = usuario?.email
        if (usuario == null || correo == null) {
            Toast.makeText(this, R.string.error_actualizar_contrasena, Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, actual)
            .addOnSuccessListener {
                usuario.updatePassword(nueva)
                    .addOnSuccessListener {
                        dialogo.dismiss()
                        Toast.makeText(this, R.string.contrasena_actualizada, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, R.string.error_actualizar_contrasena, Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.contrasena_actual_incorrecta, Toast.LENGTH_SHORT).show()
            }
    }

    // ---------- Privacidad ----------

    private fun mostrarPrivacidad() {
        val contenedor = findViewById<LinearLayout>(R.id.llPrivacidad)
        contenedor.removeAllViews()

        agregarFila(
            contenedor,
            R.drawable.ic_ojo_oculto,
            R.string.priv_ocultar_montos,
            R.string.priv_ocultar_montos_descripcion,
            getString(
                if (Preferencias.ocultarMontos(this)) R.string.priv_activado
                else R.string.priv_desactivado
            )
        ) {
            val nuevoValor = !Preferencias.ocultarMontos(this)
            Preferencias.guardarOcultarMontos(this, nuevoValor)
            Interaccion.Montos.reiniciar()
            mostrarPrivacidad()
        }

        agregarFila(
            contenedor,
            R.drawable.ic_basura,
            R.string.priv_borrar_datos,
            R.string.priv_borrar_datos_descripcion,
            null
        ) { confirmarBorrarDatos() }

        agregarFila(
            contenedor,
            R.drawable.ic_documento,
            R.string.priv_politica,
            R.string.priv_politica_descripcion,
            null
        ) { LegalActivity.abrir(this, false) }

        agregarFila(
            contenedor,
            R.drawable.ic_documento,
            R.string.priv_terminos,
            R.string.priv_terminos_descripcion,
            null
        ) { LegalActivity.abrir(this, true) }
    }

    private fun confirmarBorrarDatos() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.priv_borrar_datos)
            .setMessage(R.string.priv_borrar_confirmacion)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.priv_borrar_datos) { _, _ -> borrarDatosLocales() }
            .show()
    }

    private fun borrarDatosLocales() {
        Preferencias.borrarDatosLocales(this)
        Interaccion.reiniciar()
        Interaccion.Montos.reiniciar()
        cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        Toast.makeText(this, R.string.priv_datos_borrados, Toast.LENGTH_SHORT).show()
        recreate()
    }

    // ---------- Fila reutilizable ----------

    private fun agregarFila(
        contenedor: LinearLayout,
        icono: Int,
        titulo: Int,
        descripcion: Int,
        valor: String?,
        accion: () -> Unit
    ) {
        if (contenedor.childCount > 0) {
            contenedor.addView(
                View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(getColor(R.color.borde))
                }
            )
        }

        val fila = layoutInflater.inflate(R.layout.item_ajuste, contenedor, false) as LinearLayout
        fila.findViewById<ImageView>(R.id.iaIcono).setImageResource(icono)
        fila.findViewById<TextView>(R.id.iaTitulo).setText(titulo)
        fila.findViewById<TextView>(R.id.iaDescripcion).setText(descripcion)
        val tvValor = fila.findViewById<TextView>(R.id.iaValor)
        if (valor.isNullOrBlank()) {
            tvValor.visibility = View.GONE
        } else {
            tvValor.text = valor
        }
        fila.setOnClickListener { accion() }
        contenedor.addView(fila)
    }
}