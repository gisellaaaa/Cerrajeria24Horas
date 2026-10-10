package com.example.cerrajeria24horas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.Preferencias
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var pbLogin: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Si ya existe una sesión activa, entrar directamente al panel principal.
        if (auth.currentUser != null) {
            irAlPanel()
            return
        }

        setContentView(R.layout.activity_login)
        UI.aplicarMarca(findViewById(R.id.tvMarca))

        etCorreo = findViewById(R.id.etLoginCorreo)
        etPassword = findViewById(R.id.etLoginPassword)
        pbLogin = findViewById(R.id.pbLogin)

        findViewById<Button>(R.id.btnLogin).setOnClickListener { iniciarSesion() }
        findViewById<Button>(R.id.btnIrRegistro).setOnClickListener {
            abrirActividad(RegisterActivity::class.java)
        }
        findViewById<TextView>(R.id.tvOlvideContrasena).setOnClickListener {
            recuperarContrasena()
        }
        mostrarCuentasDelDispositivo()
    }

    private fun iniciarSesion() {
        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString()

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.completa_campos, Toast.LENGTH_SHORT).show()
            return
        }
        if (!Formato.esCorreoValido(correo)) {
            Toast.makeText(this, R.string.correo_invalido, Toast.LENGTH_SHORT).show()
            return
        }

        mostrarCarga(true)
        auth.signInWithEmailAndPassword(correo, password)
            .addOnSuccessListener {
                Preferencias.guardarCuenta(this, correo, correo)
                Toast.makeText(this, getString(R.string.bienvenido), Toast.LENGTH_SHORT).show()
                if (Preferencias.tienePin(this) && !BloqueoActivity.desbloqueado) {
                    // La app no se abre hasta digitar el PIN.
                    BloqueoActivity.abrir(this, BloqueoActivity.MODO_DESBLOQUEO)
                }
                irAlPanel()
            }
            .addOnFailureListener { e ->
                mostrarCarga(false)
                Toast.makeText(this, mensajeError(e), Toast.LENGTH_LONG).show()
            }
    }

    /** Envía al correo con el que se creó la cuenta el enlace de recuperación de contraseña. */
    private fun recuperarContrasena() {
        val correo = etCorreo.text.toString().trim()
        if (!Formato.esCorreoValido(correo)) {
            Toast.makeText(this, R.string.correo_invalido, Toast.LENGTH_SHORT).show()
            return
        }

        mostrarCarga(true)
        auth.sendPasswordResetEmail(correo)
            .addOnSuccessListener {
                mostrarCarga(false)
                Toast.makeText(this, R.string.correo_recuperacion_enviado, Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                mostrarCarga(false)
                Toast.makeText(this, R.string.error_enviar_recuperacion, Toast.LENGTH_LONG).show()
            }
    }

    /** Cuentas usadas en este teléfono: al tocarlas se completa el correo para cambiar de cuenta. */
    private fun mostrarCuentasDelDispositivo() {
        val cuentas = Preferencias.cuentas(this)
        val lista = findViewById<LinearLayout>(R.id.llCuentasLista)
        findViewById<TextView>(R.id.tvSinCuentas).visibility =
            if (cuentas.isEmpty()) View.VISIBLE else View.GONE
        if (cuentas.isEmpty()) return

        findViewById<View>(R.id.llCuentasDispositivo).visibility = View.VISIBLE
        lista.removeAllViews()
        cuentas.forEach { cuenta ->
            val fila = layoutInflater.inflate(R.layout.item_cuenta_guardada, lista, false)
            fila.findViewById<TextView>(R.id.icCuentaNombre).text =
                cuenta.nombre.ifBlank { cuenta.correo }
            fila.findViewById<TextView>(R.id.icCuentaCorreo).text = cuenta.correo
            fila.setOnClickListener { usarCuenta(cuenta.correo) }
            lista.addView(fila)
        }
    }

    private fun usarCuenta(correo: String) {
        etCorreo.setText(correo)
        etPassword.setText("")
        etPassword.requestFocus()
    }

    private fun mensajeError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> getString(R.string.no_existe_cuenta)
        is FirebaseAuthInvalidCredentialsException -> getString(R.string.contrasena_incorrecta)
        is FirebaseNetworkException -> getString(R.string.error_red)
        else -> getString(R.string.correo_o_contrasena_incorrectos)
    }

    private fun mostrarCarga(activo: Boolean) {
        pbLogin.visibility = if (activo) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.btnLogin).isEnabled = !activo
    }

    private fun irAlPanel() {
        abrirRaiz(MainActivity::class.java)
    }
}