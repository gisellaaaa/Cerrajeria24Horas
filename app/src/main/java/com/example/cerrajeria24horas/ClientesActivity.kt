package com.example.cerrajeria24horas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.adapters.ClientesAdapter
import com.example.cerrajeria24horas.data.Cliente
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.auth.FirebaseAuth

class ClientesActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ClientesAdapter
    private lateinit var rvClientes: RecyclerView
    private lateinit var emptyView: View
    private lateinit var pbClientes: ProgressBar
    private var cargado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            irAlLogin()
            return
        }

        setContentView(R.layout.activity_clientes)
        configurarEncabezado(getString(R.string.titulo_clientes))

        rvClientes = findViewById(R.id.rvClientes)
        emptyView = findViewById(R.id.emptyClientes)
        pbClientes = findViewById(R.id.pbClientes)

        adapter = ClientesAdapter { cliente -> abrirDetalle(cliente.id) }
        rvClientes.layoutManager = LinearLayoutManager(this)
        rvClientes.adapter = adapter

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRegistrarCliente)
            .setOnClickListener {
                abrirActividad(ClienteFormActivity::class.java)
            }

        cargado = true
    }

    override fun onResume() {
        super.onResume()
        if (cargado && auth.currentUser != null) {
            cargarClientes()
        }
    }

    private fun cargarClientes() {
        val uid = auth.currentUser?.uid ?: return
        rvClientes.visibility = View.GONE
        emptyView.visibility = View.GONE
        pbClientes.visibility = View.VISIBLE

        FirestoreAPI.obtenerClientes(uid)
            .addOnSuccessListener { snapshot ->
                val clientes = FirestoreAPI.ordenarClientes(
                    snapshot.documents
                        .mapNotNull { Cliente.fromSnapshot(it) }
                        .filterNot { it.id.isEmpty() }
                )
                emptyView.visibility = if (clientes.isEmpty()) View.VISIBLE else View.GONE
                adapter.setData(clientes)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
                emptyView.visibility = View.VISIBLE
            }
            .addOnCompleteListener {
                pbClientes.visibility = View.GONE
                rvClientes.visibility = View.VISIBLE
            }
    }

    private fun abrirDetalle(clienteId: String) {
        val intent = Intent(this, ClienteDetalleActivity::class.java)
        intent.putExtra(ClienteDetalleActivity.EXTRA_CLIENTE_ID, clienteId)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun irAlLogin() {
        abrirRaiz(LoginActivity::class.java)
    }
}