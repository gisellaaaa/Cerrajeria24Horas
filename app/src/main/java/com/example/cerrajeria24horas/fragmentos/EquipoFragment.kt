package com.example.cerrajeria24horas.fragmentos

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.adapters.EquipoAdapter
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.RolUsuario
import com.example.cerrajeria24horas.data.Usuario
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.UI
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/** Equipo del taller: solo el administrador lo ve, y desde aquí crea las cuentas de sus cerrajeros. */
class EquipoFragment : Fragment(R.layout.fragment_equipo) {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: EquipoAdapter
    private lateinit var rvEquipo: RecyclerView
    private lateinit var emptyView: View
    private lateinit var pbEquipo: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvEquipo = view.findViewById(R.id.rvEquipo)
        emptyView = view.findViewById(R.id.emptyEquipo)
        pbEquipo = view.findViewById(R.id.pbEquipo)

        adapter = EquipoAdapter()
        rvEquipo.layoutManager = LinearLayoutManager(requireContext())
        rvEquipo.adapter = adapter

        view.findViewById<MaterialButton>(R.id.btnAgregarCerrajero).setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) cargarEquipo()
    }

    private fun tallerId(): String = auth.currentUser?.uid ?: ""

    private fun cargarEquipo() {
        val uid = auth.currentUser?.uid ?: return
        rvEquipo.visibility = View.GONE
        emptyView.visibility = View.GONE
        pbEquipo.visibility = View.VISIBLE

        FirestoreAPI.obtenerUsuariosDelTaller(uid)
            .addOnSuccessListener { snapshot ->
                val miembros = snapshot.documents
                    .mapNotNull { Usuario.fromSnapshot(it) }
                    .sortedWith(compareBy({ it.rol != RolUsuario.ADMIN }, { it.nombre.lowercase() }))
                emptyView.visibility = if (miembros.isEmpty()) View.VISIBLE else View.GONE
                adapter.setData(miembros)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), UI.mensajeErrorFirestore(requireContext(), e), Toast.LENGTH_LONG).show()
                emptyView.visibility = View.VISIBLE
            }
            .addOnCompleteListener {
                pbEquipo.visibility = View.GONE
                rvEquipo.visibility = View.VISIBLE
            }
    }

    private fun mostrarDialogoAgregar() {
        val dialogo = Dialog(requireContext(), R.style.Theme_Cerrajeria24Horas_Dialogo)
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogo.setContentView(R.layout.dialogo_agregar_cerrajero)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etNombre = dialogo.findViewById<EditText>(R.id.etMiembroNombre)
        val etCorreo = dialogo.findViewById<EditText>(R.id.etMiembroCorreo)
        val etContrasena = dialogo.findViewById<EditText>(R.id.etMiembroContrasena)
        val pb = dialogo.findViewById<ProgressBar>(R.id.pbMiembro)
        val btnCrear = dialogo.findViewById<Button>(R.id.btnMiembroCrear)

        dialogo.findViewById<View>(R.id.btnMiembroCancelar).setOnClickListener { dialogo.dismiss() }
        btnCrear.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString()
            when {
                nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() ->
                    Toast.makeText(requireContext(), R.string.completa_campos, Toast.LENGTH_SHORT).show()

                !Formato.esCorreoValido(correo) ->
                    Toast.makeText(requireContext(), R.string.correo_invalido, Toast.LENGTH_SHORT).show()

                contrasena.length < 6 ->
                    Toast.makeText(requireContext(), R.string.contrasena_corta, Toast.LENGTH_SHORT).show()

                else -> {
                    pb.visibility = View.VISIBLE
                    btnCrear.isEnabled = false
                    FirestoreAPI.crearCuentaCerrajero(nombre, correo, contrasena, tallerId())
                        .addOnSuccessListener {
                            dialogo.dismiss()
                            Toast.makeText(requireContext(), R.string.cerrajero_creado, Toast.LENGTH_SHORT).show()
                            cargarEquipo()
                        }
                        .addOnFailureListener { e ->
                            pb.visibility = View.GONE
                            btnCrear.isEnabled = true
                            val mensaje = when (e) {
                                is FirebaseAuthUserCollisionException -> getString(R.string.correo_ya_registrado)
                                is FirebaseAuthWeakPasswordException -> getString(R.string.contrasena_corta)
                                else -> getString(R.string.error_crear_cerrajero)
                            }
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
                        }
                }
            }
        }
        dialogo.show()
    }
}
