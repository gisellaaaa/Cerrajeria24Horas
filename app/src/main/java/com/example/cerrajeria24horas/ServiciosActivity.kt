package com.example.cerrajeria24horas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.adapters.ServiciosAdapter
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.auth.FirebaseAuth

class ServiciosActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ServiciosAdapter
    private lateinit var rvServicios: RecyclerView
    private lateinit var emptyView: View
    private lateinit var pbServicios: ProgressBar
    private var cargado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            irAlLogin()
            return
        }

        setContentView(R.layout.activity_servicios)
        configurarEncabezado(getString(R.string.titulo_servicios))

        rvServicios = findViewById(R.id.rvServicios)
        emptyView = findViewById(R.id.emptyServicios)
        pbServicios = findViewById(R.id.pbServicios)

        adapter = ServiciosAdapter { servicio -> abrirDetalle(servicio.id) }
        rvServicios.layoutManager = LinearLayoutManager(this)
        rvServicios.adapter = adapter

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRegistrarServicio)
            .setOnClickListener {
                abrirActividad(ServicioFormActivity::class.java)
            }

        cargado = true
    }

    override fun onResume() {
        super.onResume()
        if (cargado && auth.currentUser != null) {
            cargarServicios()
        }
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
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
                emptyView.visibility = View.VISIBLE
            }
            .addOnCompleteListener {
                pbServicios.visibility = View.GONE
                rvServicios.visibility = View.VISIBLE
            }
    }

    private fun abrirDetalle(servicioId: String) {
        val intent = Intent(this, ServicioDetalleActivity::class.java)
        intent.putExtra(ServicioDetalleActivity.EXTRA_SERVICIO_ID, servicioId)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun irAlLogin() {
        abrirRaiz(LoginActivity::class.java)
    }
}