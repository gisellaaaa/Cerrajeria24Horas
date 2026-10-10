package com.example.cerrajeria24horas

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import com.example.cerrajeria24horas.fragmentos.ClientesFragment
import com.example.cerrajeria24horas.fragmentos.EquipoFragment
import com.example.cerrajeria24horas.fragmentos.GananciasFragment
import com.example.cerrajeria24horas.fragmentos.InicioFragment
import com.example.cerrajeria24horas.fragmentos.ServiciosFragment
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.RolUsuario
import com.example.cerrajeria24horas.util.BarraNavegacionFlotante
import com.example.cerrajeria24horas.util.BarraNavegacionFlotante.Destino
import com.example.cerrajeria24horas.util.Preferencias
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var barra: BarraNavegacionFlotante

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            irAlLogin()
            return
        }

        setContentView(R.layout.activity_main)
        UI.aplicarMarca(findViewById(R.id.tvMarca))
        barra = findViewById(R.id.barraNavegacion)

        findViewById<View>(R.id.btnIrPerfil).setOnClickListener { abrirActividad(PerfilActivity::class.java) }

        barra.alSeleccionar = { destino -> cambiarFragmento(destino) }
        barra.alAccionCentral = { abrirActividad(ServicioFormActivity::class.java) }

        aplicarRol()

        if (savedInstanceState == null) {
            cambiarFragmento(Destino.INICIO)
        } else {
            barra.mostrar(destinoDesdeTag())
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (barra.destinoActual != Destino.INICIO) {
                    barra.mostrar(Destino.INICIO)
                    cambiarFragmento(Destino.INICIO)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun revisarSesionEnSeguridad() = true

    override fun onResume() {
        super.onResume()
        if (auth.currentUser == null || isFinishing) return
        if (Preferencias.tienePin(this) && !BloqueoActivity.desbloqueado) {
            BloqueoActivity.abrir(this, BloqueoActivity.MODO_DESBLOQUEO)
        }
    }

    /** Ajusta lo que ve cada rol: el cerrajero no gestiona clientes, equipo ni crea servicios. */
    private fun aplicarRol() {
        val uid = auth.currentUser?.uid ?: return
        FirestoreAPI.obtenerUsuario(uid).addOnSuccessListener { doc ->
            if (doc.getString("rol") == RolUsuario.CERRAJERO.valor) {
                barra.mostrarItem(Destino.CLIENTES, false)
                barra.mostrarItem(Destino.EQUIPO, false)
                barra.mostrarAccionCentral(false)
            }
        }
    }

    private fun cambiarFragmento(destino: Destino) {
        val fragmento = when (destino) {
            Destino.INICIO -> InicioFragment()
            Destino.SERVICIOS -> ServiciosFragment()
            Destino.CLIENTES -> ClientesFragment()
            Destino.GANANCIAS -> GananciasFragment()
            Destino.EQUIPO -> EquipoFragment()
        }
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.contenedorFragmentos, fragmento, destino.name)
        }
    }

    private fun destinoDesdeTag(): Destino {
        val tag = supportFragmentManager.findFragmentById(R.id.contenedorFragmentos)?.tag
        return Destino.values().firstOrNull { it.name == tag } ?: Destino.INICIO
    }

    private fun irAlLogin() {
        abrirRaiz(LoginActivity::class.java)
    }
}
