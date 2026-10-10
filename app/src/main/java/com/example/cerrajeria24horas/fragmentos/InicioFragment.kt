package com.example.cerrajeria24horas.fragmentos

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.ServicioDetalleActivity
import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.AnilloProgresoView
import com.example.cerrajeria24horas.util.Calculos
import com.example.cerrajeria24horas.util.Fechas
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.Preferencias
import com.example.cerrajeria24horas.util.UI
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var raiz: View
    private var cargado = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        raiz = view
        mostrarResumen(emptyList())
        cargarNombre()
        cargado = true
    }

    override fun onResume() {
        super.onResume()
        if (cargado && auth.currentUser != null) cargarDatos()
    }

    private fun cargarNombre() {
        val usuario = auth.currentUser ?: return
        FirestoreAPI.obtenerUsuario(usuario.uid)
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre").orEmpty()
                raiz.findViewById<TextView>(R.id.tvBienvenido).text = nombre
                usuario.email?.let { correo ->
                    Preferencias.guardarCuenta(requireContext(), correo, nombre.ifBlank { correo })
                }
            }
            .addOnFailureListener {
                raiz.findViewById<TextView>(R.id.tvBienvenido).text = ""
            }
    }

    private fun cargarDatos() {
        val uid = auth.currentUser?.uid ?: return
        FirestoreAPI.obtenerServiciosSegunRol(uid)
            .addOnSuccessListener { snapshot ->
                val servicios = FirestoreAPI.ordenarServicios(
                    snapshot.documents
                        .mapNotNull { Servicio.fromSnapshot(it) }
                        .filterNot { it.id.isEmpty() }
                )
                mostrarResumen(servicios)
                cargarNombre()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), UI.mensajeErrorFirestore(requireContext(), e), Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarResumen(servicios: List<Servicio>) {
        val pendientes = Calculos.contarPorEstado(servicios, EstadoServicio.PENDIENTE)
        val enProceso = Calculos.contarPorEstado(servicios, EstadoServicio.EN_PROCESO)
        val realizados = Calculos.contarPorEstado(servicios, EstadoServicio.REALIZADO)

        pintarMetrica(
            R.id.cardPendientes, pendientes.toString(), R.string.pendientes,
            R.drawable.ic_reloj, R.color.oro, R.color.azul_marino
        )
        pintarMetrica(
            R.id.cardEnProceso, enProceso.toString(), R.string.en_proceso,
            R.drawable.ic_llave_doble, R.color.azul_acento, R.color.white
        )
        pintarMetrica(
            R.id.cardRealizados, realizados.toString(), R.string.realizados,
            R.drawable.ic_cerradura, R.color.estado_realizado, R.color.white
        )

        val gananciasHoy = Calculos.sumarValores(
            Calculos.serviciosRealizadosEn(servicios, Fechas.inicioDeHoy(), Fechas.inicioDeManana())
        )
        raiz.findViewById<TextView>(R.id.tvGananciasHoy).text =
            if (Interaccion.Montos.ocultos(requireContext())) getString(R.string.montos_ocultos)
            else Formato.moneda(gananciasHoy)

        val total = servicios.size
        val fraccion = if (total == 0) 0f else realizados.toFloat() / total
        raiz.findViewById<AnilloProgresoView>(R.id.anilloProgreso).mostrar(fraccion)

        mostrarUltimos(servicios)
    }

    /** Colorea una tarjeta de métrica: ícono, número y título. */
    private fun pintarMetrica(
        idTarjeta: Int,
        valor: String,
        tituloRes: Int,
        iconoRes: Int,
        acentoRes: Int,
        tintIconoRes: Int
    ) {
        val tarjeta = raiz.findViewById<MaterialCardView>(idTarjeta)
        tarjeta.findViewById<FrameLayout>(R.id.fondoMetricaIcono).backgroundTintList =
            ColorStateList.valueOf(requireContext().getColor(acentoRes))
        tarjeta.findViewById<ImageView>(R.id.ivMetricaIcono).apply {
            setImageResource(iconoRes)
            imageTintList = ColorStateList.valueOf(requireContext().getColor(tintIconoRes))
        }
        tarjeta.findViewById<TextView>(R.id.tvMetricaValor).text = valor
        tarjeta.findViewById<TextView>(R.id.tvMetricaTitulo).setText(tituloRes)
    }

    /** Rellena el panel con los últimos servicios para que la pantalla no quede vacía. */
    private fun mostrarUltimos(servicios: List<Servicio>) {
        val contenedor = raiz.findViewById<LinearLayout>(R.id.llUltimosServicios)
        val vacio = raiz.findViewById<View>(R.id.tvPanelSinServicios)
        contenedor.removeAllViews()

        val ultimos = servicios.take(4)
        if (ultimos.isEmpty()) {
            vacio.visibility = View.VISIBLE
            contenedor.visibility = View.GONE
            return
        }

        vacio.visibility = View.GONE
        contenedor.visibility = View.VISIBLE
        val inflador = LayoutInflater.from(requireContext())
        val ocultos = Interaccion.Montos.ocultos(requireContext())

        ultimos.forEach { servicio ->
            val fila = inflador.inflate(R.layout.item_servicio, contenedor, false)
            fila.findViewById<TextView>(R.id.tvServCliente).text =
                servicio.nombreCliente.ifBlank { servicio.clienteId }
            UI.pintarEstado(fila.findViewById(R.id.tvServEstado), servicio.estado)
            fila.findViewById<TextView>(R.id.tvServTipo).text =
                Formato.tipoServicio(servicio.tipoServicio, requireContext())
            fila.findViewById<TextView>(R.id.tvServCiudad).text =
                listOf(servicio.ciudad, servicio.direccion)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
            fila.findViewById<TextView>(R.id.tvServFecha).text =
                servicio.fecha?.let { Formato.fechaCorta(it) }.orEmpty()
            fila.findViewById<TextView>(R.id.tvServValor).text =
                if (ocultos) getString(R.string.montos_ocultos) else Formato.moneda(servicio.valor)
            fila.setOnClickListener { abrirDetalle(servicio.id) }
            contenedor.addView(fila)
        }
    }

    private fun abrirDetalle(servicioId: String) {
        val intent = Intent(requireContext(), ServicioDetalleActivity::class.java)
        intent.putExtra(ServicioDetalleActivity.EXTRA_SERVICIO_ID, servicioId)
        startActivity(intent)
    }
}
