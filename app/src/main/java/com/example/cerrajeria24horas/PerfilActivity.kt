package com.example.cerrajeria24horas

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.util.FotoPerfil
import com.google.firebase.auth.FirebaseAuth

class PerfilActivity : BaseActivity() {

    private lateinit var pbPerfil: ProgressBar
    private lateinit var ivPerfilFoto: ImageView
    private lateinit var btnCambiarFoto: TextView
    private lateinit var btnEliminarFoto: TextView

    private val selectorFoto = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val usuario = FirebaseAuth.getInstance().currentUser ?: return@registerForActivityResult
        uri ?: return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
        }
        FotoPerfil.guardar(this, usuario.uid, uri)
            .addOnSuccessListener {
                FotoPerfil.mostrar(ivPerfilFoto, uri)
                actualizarEstadoFoto(true)
                Toast.makeText(this, R.string.foto_perfil_guardada, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_foto_perfil, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        configurarEncabezado(getString(R.string.titulo_perfil))

        pbPerfil = findViewById(R.id.pbPerfil)
        ivPerfilFoto = findViewById(R.id.ivPerfilFoto)
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto)
        btnEliminarFoto = findViewById(R.id.btnEliminarFoto)

        cargarFotoPerfil()
        cargarPerfil()

        ivPerfilFoto.setOnClickListener { abrirSelectorFoto() }
        btnCambiarFoto.setOnClickListener { abrirSelectorFoto() }
        btnEliminarFoto.setOnClickListener { eliminarFoto() }
        findViewById<View>(R.id.btnAjustes).setOnClickListener { abrirActividad(AjustesActivity::class.java) }
        findViewById<TextView>(R.id.tvCerrarSesion).setOnClickListener { confirmarCierreSesion() }
    }

    private fun eliminarFoto() {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        FotoPerfil.eliminar(usuario.uid)
            .addOnSuccessListener {
                FotoPerfil.mostrar(ivPerfilFoto, null)
                actualizarEstadoFoto(false)
                Toast.makeText(this, R.string.foto_perfil_eliminada, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_eliminar_foto_perfil, Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarEstadoFoto(conFoto: Boolean) {
        btnCambiarFoto.setText(
            if (conFoto) R.string.cambiar_foto_perfil else R.string.agregar_foto_perfil
        )
        btnEliminarFoto.visibility = if (conFoto) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        cargarFotoPerfil()
    }

    private fun abrirSelectorFoto() {
        selectorFoto.launch(arrayOf("image/*"))
    }

    private fun cargarFotoPerfil() {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        FotoPerfil.obtener(usuario.uid) { base64 ->
            if (!isFinishing && !isDestroyed) {
                FotoPerfil.mostrar(ivPerfilFoto, base64)
                actualizarEstadoFoto(!base64.isNullOrBlank())
            }
        }
    }

    private fun cargarPerfil() {
        val usuario = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            irAlLogin()
            return
        }

        pbPerfil.visibility = View.VISIBLE
        FirestoreAPI.obtenerUsuario(usuario.uid)
            .addOnSuccessListener { doc ->
                findViewById<TextView>(R.id.tvPerfilNombre).text =
                    doc.getString("nombre") ?: usuario.email ?: ""
                findViewById<TextView>(R.id.tvPerfilCorreo).text = usuario.email ?: ""
                val telefono = doc.getString("telefono").orEmpty()
                findViewById<TextView>(R.id.tvPerfilTelefono).text =
                    if (telefono.isNotBlank()) getString(R.string.telefono) + ": " + telefono else ""
                val rol = doc.getString("rol").orEmpty()
                findViewById<TextView>(R.id.tvPerfilRol).text =
                    if (rol.isNotBlank()) getString(R.string.rol) + ": " + rol else ""
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.tvPerfilNombre).text =
                    usuario.email?.substringBefore("@") ?: ""
                findViewById<TextView>(R.id.tvPerfilCorreo).text = usuario.email ?: ""
            }
            .addOnCompleteListener {
                pbPerfil.visibility = View.GONE
            }
    }

    private fun confirmarCierreSesion() {
        mostrarDialogoCerrarSesion {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show()
            irAlLogin()
        }
    }

    private fun irAlLogin() {
        abrirRaiz(LoginActivity::class.java)
    }
}
