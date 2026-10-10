package com.example.cerrajeria24horas

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.cerrajeria24horas.data.Cliente
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.auth.FirebaseAuth

class ClienteFormActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_form)
        configurarEncabezado(getString(R.string.subir_cliente))

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGuardarCliente)
            .setOnClickListener { guardar() }
    }

    private fun guardar() {
        val nombre = campo(R.id.etCNombre).trim()
        val telefono = campo(R.id.etCTelefono).trim()
        val correo = campo(R.id.etCCorreo).trim()
        val ciudad = campo(R.id.etCCiudad).trim()
        val direccion = campo(R.id.etCDireccion).trim()
        val notas = campo(R.id.etCNotas).trim()

        if (nombre.isEmpty() || telefono.isEmpty() || ciudad.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, R.string.cliente_obligatorio, Toast.LENGTH_SHORT).show()
            return
        }
        if (!Formato.esTelefonoValido(telefono)) {
            Toast.makeText(this, R.string.telefono_invalido, Toast.LENGTH_SHORT).show()
            return
        }
        if (correo.isNotEmpty() && !Formato.esCorreoValido(correo)) {
            Toast.makeText(this, R.string.correo_invalido, Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, R.string.error_red, Toast.LENGTH_LONG).show()
            return
        }

        val cliente = Cliente(
            nombreCompleto = nombre,
            telefono = telefono,
            correo = correo,
            ciudad = ciudad,
            direccion = direccion,
            notas = notas,
            registradoPor = uid
        )

        mostrarCarga(true)
        FirestoreAPI.guardarCliente(cliente)
            .addOnSuccessListener {
                mostrarCarga(false)
                Toast.makeText(this, R.string.cliente_guardado, Toast.LENGTH_LONG).show()
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            .addOnFailureListener { e ->
                mostrarCarga(false)
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
            }
    }

    private fun campo(id: Int): String =
        findViewById<EditText>(id).text.toString()

    private fun mostrarCarga(activo: Boolean) {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGuardarCliente)
            .isEnabled = !activo
        findViewById<View>(R.id.pbClienteForm).visibility =
            if (activo) View.VISIBLE else View.GONE
    }
}