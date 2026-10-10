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
import com.example.cerrajeria24horas.adapters.FilaServicios
import com.example.cerrajeria24horas.adapters.ServiciosAdapter
import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.RolUsuario
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.UI
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth

class ServiciosFragment : Fragment(R.layout.fragment_servicios) {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ServiciosAdapter
    private lateinit var rvServicios: RecyclerView
    private lateinit var emptyView: View
    private lateinit var pbServicios: ProgressBar

    private var servicios: List<Servicio> = emptyList()
    private var filtro: EstadoServicio? = null
    private var cargado = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvServicios = view.findViewById(R.id.rvServicios)
        emptyView = view.findViewById(R.id.emptyServicios)
        pbServicios = view.findViewById(R.id.pbServicios)

        adapter = ServiciosAdapter { servicio -> abrirDetalle(servicio.id) }
        rvServicios.layoutManager = LinearLayoutManager(requireContext())
        rvServicios.adapter = adapter

        view.findViewById<ChipGroup>(R.id.chipsServicios).setOnCheckedStateChangeListener { _, marcados ->
            filtro = when (marcados.firstOrNull()) {
                R.id.chipPendientes -> EstadoServicio.PENDIENTE
                R.id.chipEnProceso -> EstadoServicio.EN_PROCESO
                R.id.chipRealizados -> EstadoServicio.REALIZADO
                else -> null
            }
            aplicar()
        }

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

        FirestoreAPI.obtenerUsuario(uid)
            .addOnSuccessListener { doc ->
                if (view == null) return@addOnSuccessListener
                val esCerrajero = doc.getString("rol") == RolUsuario.CERRAJERO.valor
                view?.findViewById<MaterialButton>(R.id.btnRegistrarServicio)?.visibility =
                    if (esCerrajero) View.GONE else View.VISIBLE
                val consulta = if (esCerrajero) {
                    FirestoreAPI.obtenerServiciosAsignados(uid)
                } else {
                    FirestoreAPI.obtenerServicios(uid)
                }
                consulta
                    .addOnSuccessListener { snapshot ->
                        if (view == null) return@addOnSuccessListener
                        servicios = FirestoreAPI.ordenarServicios(
                            snapshot.documents
                                .mapNotNull { Servicio.fromSnapshot(it) }
                                .filterNot { it.id.isEmpty() }
                        )
                        cargado = true
                        aplicar()
                    }
                    .addOnFailureListener { e ->
                        val contexto = context ?: return@addOnFailureListener
                        Toast.makeText(contexto, UI.mensajeErrorFirestore(contexto, e), Toast.LENGTH_LONG).show()
                        emptyView.visibility = View.VISIBLE
                    }
                    .addOnCompleteListener {
                        if (view == null) return@addOnCompleteListener
                        pbServicios.visibility = View.GONE
                        rvServicios.visibility = View.VISIBLE
                    }
            }
            .addOnFailureListener { e ->
                val contexto = context ?: return@addOnFailureListener
                Toast.makeText(contexto, UI.mensajeErrorFirestore(contexto, e), Toast.LENGTH_LONG).show()
                pbServicios.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
    }

    private fun aplicar() {
        if (!cargado) return
        val filtrados = servicios.filter { filtro == null || it.estado == filtro }
        emptyView.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
        adapter.submitList(agrupar(filtrados))
    }

    private fun agrupar(lista: List<Servicio>): List<FilaServicios> {
        val filas = mutableListOf<FilaServicios>()
        EstadoServicio.values().forEach { estado ->
            val delEstado = lista.filter { it.estado == estado }
            if (delEstado.isNotEmpty()) {
                filas.add(FilaServicios.Encabezado(estado, delEstado.size))
                delEstado.forEach { filas.add(FilaServicios.Articulo(it)) }
            }
        }
        return filas
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
