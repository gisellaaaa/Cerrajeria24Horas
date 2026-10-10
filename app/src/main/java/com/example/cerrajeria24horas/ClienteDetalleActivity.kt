package com.example.cerrajeria24horas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.cerrajeria24horas.data.Cliente
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.UI

class ClienteDetalleActivity : BaseActivity() {

    companion object {
        const val EXTRA_CLIENTE_ID = "clienteId"
    }

    private lateinit var pbClienteDetalle: ProgressBar
    private var clienteActual: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente_detalle)
        configurarEncabezado(getString(R.string.titulo_detalle_cliente))

        pbClienteDetalle = findViewById(R.id.pbClienteDetalle)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRegistrarServicioDeCliente)
            .setOnClickListener { irARegistrarServicio() }

        val clienteId = intent.getStringExtra(EXTRA_CLIENTE_ID).orEmpty()
        if (clienteId.isEmpty()) {
            Toast.makeText(this, R.string.sin_datos, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cargarCliente(clienteId)
    }

    private fun cargarCliente(clienteId: String) {
        pbClienteDetalle.visibility = View.VISIBLE

        FirestoreAPI.obtenerCliente(clienteId)
            .addOnSuccessListener { snapshot ->
                val cliente = Cliente.fromSnapshot(snapshot)
                if (cliente.id.isEmpty()) {
                    Toast.makeText(this, R.string.cliente_no_encontrado, Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }
                clienteActual = cliente
                mostrar(cliente)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnCompleteListener {
                pbClienteDetalle.visibility = View.GONE
            }
    }

    private fun mostrar(cliente: Cliente) {
        findViewById<TextView>(R.id.tvCNombre).text = cliente.nombreCompleto
        findViewById<TextView>(R.id.tvCTelefono).text =
            getString(R.string.telefono) + ": " + cliente.telefono
        findViewById<TextView>(R.id.tvCCorreo).text =
            getString(R.string.correo) + ": " +
                cliente.correo.ifBlank { getString(R.string.sin_correo) }
        findViewById<TextView>(R.id.tvCCiudad).text =
            getString(R.string.ciudad) + ": " + cliente.ciudad
        findViewById<TextView>(R.id.tvCDireccion).text =
            getString(R.string.direccion) + ": " + cliente.direccion

        val tvNotas = findViewById<TextView>(R.id.tvCNotas)
        if (cliente.notas.isNotBlank()) {
            tvNotas.text = getString(R.string.notas) + ": " + cliente.notas
            tvNotas.visibility = View.VISIBLE
        } else {
            tvNotas.text = getString(R.string.sin_datos_adicionales)
            tvNotas.visibility = View.VISIBLE
        }
    }

    private fun irARegistrarServicio() {
        val cliente = clienteActual ?: return
        val intent = Intent(this, ServicioFormActivity::class.java)
        intent.putExtra(ServicioFormActivity.EXTRA_CLIENTE_ID, cliente.clienteId)
        intent.putExtra(ServicioFormActivity.EXTRA_NOMBRE_CLIENTE, cliente.nombreCompleto)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}