package com.example.cerrajeria24horas

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.Calculos
import com.example.cerrajeria24horas.util.Fechas
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.Preferencias
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var pbDashboard: ProgressBar
    private var cargado = false
    private var ultimoResumen: List<Servicio> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val usuario = auth.currentUser
        if (usuario == null) {
            irAlLogin()
            return
        }

        setContentView(R.layout.activity_main)

        pbDashboard = findViewById(R.id.pbDashboard)

        // Valores por defecto para que el resumen nunca se vea vacío.
        mostrarResumen(emptyList())
        cargarNombre(usuario.uid)

        findViewById<android.widget.ImageButton>(R.id.btnIrPerfil).setOnClickListener {
            abrir(PerfilActivity::class.java)
        }
        findViewById<View>(R.id.btnIrPerfilTexto).setOnClickListener { abrir(PerfilActivity::class.java) }
        findViewById<View>(R.id.btnIrClientes).setOnClickListener { abrir(ClientesActivity::class.java) }
        findViewById<View>(R.id.btnIrServicios).setOnClickListener { abrir(ServiciosActivity::class.java) }
        findViewById<View>(R.id.btnIrGanancias).setOnClickListener { abrir(GananciasActivity::class.java) }
        findViewById<TextView>(R.id.btnCerrarSesion).setOnClickListener { confirmarCierreSesion() }

        cargado = true
    }

    override fun revisarSesionEnSeguridad() = true

    override fun onResume() {
        super.onResume()
        if (auth.currentUser == null || isFinishing) return
        if (Preferencias.tienePin(this) && !BloqueoActivity.desbloqueado) {
            BloqueoActivity.abrir(this, BloqueoActivity.MODO_DESBLOQUEO)
            return
        }
        if (cargado) {
            cargarDatos()
        }
    }

    private fun cargarNombre(uid: String) {
        FirestoreAPI.obtenerUsuario(uid)
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre").orEmpty()
                findViewById<TextView>(R.id.tvBienvenido).text = nombre
                // El nombre real reemplaza al correo en la lista de cuentas del dispositivo.
                auth.currentUser?.email?.let { correo ->
                    Preferencias.guardarCuenta(this, correo, nombre.ifBlank { correo })
                }
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.tvBienvenido).text = ""
            }
    }

    private fun cargarDatos() {
        val uid = auth.currentUser?.uid ?: return
        pbDashboard.visibility = ProgressBar.VISIBLE

        FirestoreAPI.obtenerServicios(uid)
            .addOnSuccessListener { snapshot ->
                val servicios = FirestoreAPI.ordenarServicios(
                    snapshot.documents
                        .mapNotNull { Servicio.fromSnapshot(it) }
                        .filterNot { it.id.isEmpty() }
                )

                mostrarResumen(servicios)
                cargarNombre(uid)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                pbDashboard.visibility = ProgressBar.GONE
            }
    }

    private fun mostrarResumen(servicios: List<Servicio>) {
        ultimoResumen = servicios
        val titulo = listOf(
            R.id.cardPendientes to R.string.pendientes,
            R.id.cardEnProceso to R.string.en_proceso,
            R.id.cardRealizados to R.string.realizados
        )
        titulo.forEach { (idTarjeta, idTexto) ->
            findViewById<com.google.android.material.card.MaterialCardView>(idTarjeta)
                .findViewById<TextView>(R.id.tvStatTitulo).setText(idTexto)
        }

        val valores = mapOf(
            R.id.cardPendientes to Calculos.contarPorEstado(servicios, EstadoServicio.PENDIENTE).toString(),
            R.id.cardEnProceso to Calculos.contarPorEstado(servicios, EstadoServicio.EN_PROCESO).toString(),
            R.id.cardRealizados to Calculos.contarPorEstado(servicios, EstadoServicio.REALIZADO).toString()
        )
        valores.forEach { (idTarjeta, valor) ->
            findViewById<com.google.android.material.card.MaterialCardView>(idTarjeta)
                .findViewById<TextView>(R.id.tvStatValor).text = valor
        }

        // Acento y cifra de cada tarjeta según el estado para una lectura de un vistazo.
        val acentos = listOf(
            Triple(R.id.cardPendientes, R.color.oro, R.color.azul_marino),
            Triple(R.id.cardEnProceso, R.color.azul_acento, R.color.azul_acento),
            Triple(R.id.cardRealizados, R.color.estado_realizado, R.color.estado_realizado)
        )
        acentos.forEach { (idTarjeta, idAcento, idCifra) ->
            val tarjeta = findViewById<com.google.android.material.card.MaterialCardView>(idTarjeta)
            tarjeta.findViewById<View>(R.id.vAcento).backgroundTintList =
                ColorStateList.valueOf(getColor(idAcento))
            tarjeta.findViewById<TextView>(R.id.tvStatValor).setTextColor(getColor(idCifra))
        }

        val gananciasHoy = Calculos.sumarValores(
            Calculos.serviciosRealizadosEn(
                servicios,
                Fechas.inicioDeHoy(),
                Fechas.inicioDeManana()
            )
        )
        findViewById<TextView>(R.id.tvGananciasHoy).text =
            if (Interaccion.Montos.ocultos(this)) getString(R.string.montos_ocultos)
            else Formato.moneda(gananciasHoy)
    }

    private fun confirmarCierreSesion() {
        mostrarDialogoCerrarSesion {
            auth.signOut()
            BloqueoActivity.olvidarDesbloqueo()
            Interaccion.reiniciar()
            Toast.makeText(this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show()
            irAlLogin()
        }
    }

    private fun abrir(actividad: Class<*>) {
        abrirActividad(actividad)
    }

    private fun irAlLogin() {
        abrirRaiz(LoginActivity::class.java)
    }
}