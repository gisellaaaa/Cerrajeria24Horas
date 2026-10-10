package com.example.cerrajeria24horas.fragmentos

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cerrajeria24horas.MainActivity
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.ServicioDetalleActivity
import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.RolUsuario
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.AnilloProgresoView
import com.example.cerrajeria24horas.util.BarraNavegacionFlotante.Destino
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
        configurarAccesosRapidos()
        mostrarResumen(emptyList())
        cargarNombre()
        cargado = true
    }

    override fun onResume() {
        super.onResume()
        if (cargado && auth.currentUser != null) cargarDatos()
    }

    private fun configurarAccesosRapidos() {
        raiz.findViewById<View>(R.id.btnAccesoServicios).setOnClickListener {
            (activity as? MainActivity)?.abrirDestino(Destino.SERVICIOS)
        }
        raiz.findViewById<View>(R.id.btnAccesoClientes).setOnClickListener {
            (activity as? MainActivity)?.abrirDestino(Destino.CLIENTES)
        }
        raiz.findViewById<View>(R.id.btnAccesoGanancias).setOnClickListener {
            (activity as? MainActivity)?.abrirDestino(Destino.GANANCIAS)
        }
    }

    private fun cargarNombre() {
        val usuario = auth.currentUser ?: return
        FirestoreAPI.obtenerUsuario(usuario.uid)
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre").orEmpty()
                raiz.findViewById<TextView>(R.id.tvBienvenido).text = nombre
                if (doc.getString("rol") == RolUsuario.CERRAJERO.valor) {
                    raiz.findViewById<View>(R.id.btnAccesoClientes).visibility = View.GONE
                }
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
        val titulos = listOf(
            R.id.cardPendientes to R.string.pendientes,
            R.id.cardEnProceso to R.string.en_proceso,
            R.id.cardRealizados to R.string.realizados
        )
        titulos.forEach { (idTarjeta, idTexto) ->
            raiz.findViewById<MaterialCardView>(idTarjeta)
                .findViewById<TextView>(R.id.tvStatTitulo).setText(idTexto)
        }

        val valores = mapOf(
            R.id.cardPendientes to Calculos.contarPorEstado(servicios, EstadoServicio.PENDIENTE).toString(),
            R.id.cardEnProceso to Calculos.contarPorEstado(servicios, EstadoServicio.EN_PROCESO).toString(),
            R.id.cardRealizados to Calculos.contarPorEstado(servicios, EstadoServicio.REALIZADO).toString()
        )
        valores.forEach { (idTarjeta, valor) ->
            raiz.findViewById<MaterialCardView>(idTarjeta)
                .findViewById<TextView>(R.id.tvStatValor).text = valor
        }

        val acentos = listOf(
            Triple(R.id.cardPendientes, R.color.oro, R.color.azul_marino),
            Triple(R.id.cardEnProceso, R.color.azul_acento, R.color.azul_acento),
            Triple(R.id.cardRealizados, R.color.estado_realizado, R.color.estado_realizado)
        )
        acentos.forEach { (idTarjeta, idAcento, idCifra) ->
            val tarjeta = raiz.findViewById<MaterialCardView>(idTarjeta)
            tarjeta.findViewById<View>(R.id.vAcento).backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(idAcento))
            tarjeta.findViewById<TextView>(R.id.tvStatValor)
                .setTextColor(requireContext().getColor(idCifra))
        }

        val gananciasHoy = Calculos.sumarValores(
            Calculos.serviciosRealizadosEn(servicios, Fechas.inicioDeHoy(), Fechas.inicioDeManana())
        )
        raiz.findViewById<TextView>(R.id.tvGananciasHoy).text =
            if (Interaccion.Montos.ocultos(requireContext())) getString(R.string.montos_ocultos)
            else Formato.moneda(gananciasHoy)

        val total = servicios.size
        val realizados = Calculos.contarPorEstado(servicios, EstadoServicio.REALIZADO)
        val fraccion = if (total == 0) 0f else realizados.toFloat() / total
        raiz.findViewById<AnilloProgresoView>(R.id.anilloProgreso).mostrar(fraccion)

        mostrarUltimos(servicios)
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
