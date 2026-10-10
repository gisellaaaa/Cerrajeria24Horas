package com.example.cerrajeria24horas.fragmentos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.ServicioDetalleActivity
import com.example.cerrajeria24horas.ServicioFormActivity
import com.example.cerrajeria24horas.adapters.ServiciosAdapter
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.UI
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ServiciosFragment : Fragment(R.layout.fragment_servicios) {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var raiz: View
    private lateinit var adapter: ServiciosAdapter
    private lateinit var rvServicios: RecyclerView
    private lateinit var emptyView: View
    private lateinit var pbServicios: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        raiz = view

        rvServicios = view.findViewById(R.id.rvServicios)
        emptyView = view.findViewById(R.id.emptyServicios)
        pbServicios = view.findViewById(R.id.pbServicios)

        adapter = ServiciosAdapter { servicio -> abrirDetalle(servicio.id) }
        rvServicios.layoutManager = LinearLayoutManager(requireContext())
        rvServicios.adapter = adapter

        view.findViewById<MaterialButton>(R.id.btnRegistrarServicio).setOnClickListener {
            abrirFormulario()
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) cargarServicios()
    }

    private fun cargarServicios() {
        val uid = auth.currentUser?.uid ?: return
        rvServicios.visibility = View.GONE
        emptyView.visibility = View.GONE
        pbServicios.visibility = View.VISIBLE

        FirestoreAPI.obtenerServicios(uid)
            .addOnSuccessListener { snapshot ->
                val servicios = FirestoreAPI.ordenarServicios(
                    snapshot.documents
                        .mapNotNull { Servicio.fromSnapshot(it) }
                        .filterNot { it.id.isEmpty() }
                )
                emptyView.visibility = if (servicios.isEmpty()) View.VISIBLE else View.GONE
                adapter.setData(servicios)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), UI.mensajeErrorFirestore(requireContext(), e), Toast.LENGTH_LONG).show()
                emptyView.visibility = View.VISIBLE
            }
            .addOnCompleteListener {
                pbServicios.visibility = View.GONE
                rvServicios.visibility = View.VISIBLE
            }
    }

    private fun abrirFormulario() {
        startActivity(Intent(requireContext(), ServicioFormActivity::class.java))
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun abrirDetalle(servicioId: String) {
        val intent = Intent(requireContext(), ServicioDetalleActivity::class.java)
        intent.putExtra(ServicioDetalleActivity.EXTRA_SERVICIO_ID, servicioId)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
