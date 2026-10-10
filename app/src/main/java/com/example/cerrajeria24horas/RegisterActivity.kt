package com.example.cerrajeria24horas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.util.Formato
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmar: EditText
    private lateinit var pbRegistro: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_registro)

        configurarEncabezado(getString(R.string.titulo_registro))

        etNombre = findViewById(R.id.etRegNombre)
        etCorreo = findViewById(R.id.etRegCorreo)
        etTelefono = findViewById(R.id.etRegTelefono)
        etPassword = findViewById(R.id.etRegPassword)
        etConfirmar = findViewById(R.id.etRegConfirmar)
        pbRegistro = findViewById(R.id.pbRegistro)

        findViewById<Button>(R.id.btnCrearCuenta).setOnClickListener { crearCuenta() }
    }

    private fun crearCuenta() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmacion = etConfirmar.text.toString()

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || confirmacion.isEmpty()) {
            Toast.makeText(this, R.string.completa_campos, Toast.LENGTH_SHORT).show()
            return
        }
        if (!Formato.esCorreoValido(correo)) {
            Toast.makeText(this, R.string.correo_invalido, Toast.LENGTH_SHORT).show()
            return
        }
        if (telefono.isNotEmpty() && !Formato.esTelefonoValido(telefono)) {
            Toast.makeText(this, R.string.telefono_invalido, Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, R.string.contrasena_corta, Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmacion) {
            Toast.makeText(this, R.string.contrasenas_no_coinciden, Toast.LENGTH_SHORT).show()
            return
        }

        mostrarCarga(true)
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid ?: ""
                guardarUsuario(uid, nombre, correo, telefono)
            }
            .addOnFailureListener { e ->
                mostrarCarga(false)
                Toast.makeText(this, mensajeError(e), Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarUsuario(uid: String, nombre: String, correo: String, telefono: String) {
        FirestoreAPI.guardarUsuario(uid, nombre, correo, telefono)
            .addOnSuccessListener {
                irAlPanel()
            }
            .addOnFailureListener {
                // La cuenta ya existe en Authentication aunque falle el detalle en Firestore.
                irAlPanel()
            }
    }

    private fun mensajeError(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> getString(R.string.contrasena_corta)
        is FirebaseAuthUserCollisionException -> getString(R.string.cuenta_existente)
        is com.google.firebase.FirebaseNetworkException -> getString(R.string.error_red)
        else -> getString(R.string.campos_incorrectos)
    }

    private fun mostrarCarga(activo: Boolean) {
        pbRegistro.visibility = if (activo) android.view.View.VISIBLE else android.view.View.GONE
        findViewById<Button>(R.id.btnCrearCuenta).isEnabled = !activo
    }

    private fun irAlPanel() {
        abrirRaiz(MainActivity::class.java)
    }
}