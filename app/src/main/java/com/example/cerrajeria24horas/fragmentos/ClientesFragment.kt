package com.example.cerrajeria24horas.fragmentos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.ClienteDetalleActivity
import com.example.cerrajeria24horas.ClienteFormActivity
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.adapters.ClientesAdapter
import com.example.cerrajeria24horas.data.Cliente
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.util.UI
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ClientesFragment : Fragment(R.layout.fragment_clientes) {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var raiz: View
    private lateinit var adapter: ClientesAdapter
    private lateinit var rvClientes: RecyclerView
    private lateinit var emptyView: View
    private lateinit var pbClientes: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        raiz = view

        rvClientes = view.findViewById(R.id.rvClientes)
        emptyView = view.findViewById(R.id.emptyClientes)
        pbClientes = view.findViewById(R.id.pbClientes)

        adapter = ClientesAdapter { cliente -> abrirDetalle(cliente.id) }
        rvClientes.layoutManager = LinearLayoutManager(requireContext())
        rvClientes.adapter = adapter

        view.findViewById<MaterialButton>(R.id.btnRegistrarCliente).setOnClickListener {
            abrirFormulario()
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) cargarClientes()
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
                Toast.makeText(requireContext(), UI.mensajeErrorFirestore(requireContext(), e), Toast.LENGTH_LONG).show()
                emptyView.visibility = View.VISIBLE
            }
            .addOnCompleteListener {
                pbClientes.visibility = View.GONE
                rvClientes.visibility = View.VISIBLE
            }
    }

    private fun abrirFormulario() {
        startActivity(Intent(requireContext(), ClienteFormActivity::class.java))
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun abrirDetalle(clienteId: String) {
        val intent = Intent(requireContext(), ClienteDetalleActivity::class.java)
        intent.putExtra(ClienteDetalleActivity.EXTRA_CLIENTE_ID, clienteId)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
