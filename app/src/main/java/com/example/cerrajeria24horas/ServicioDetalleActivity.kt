package com.example.cerrajeria24horas

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Timestamp
import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.RolUsuario
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.data.Usuario
import com.example.cerrajeria24horas.util.FacturaPdf
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.auth.FirebaseAuth

class ServicioDetalleActivity : BaseActivity() {

    companion object {
        const val EXTRA_SERVICIO_ID = "servicioId"
    }

    private lateinit var spEstado: Spinner
    private lateinit var tvEstado: TextView
    private lateinit var tvValor: TextView
    private lateinit var pbServicioDetalle: ProgressBar
    private lateinit var spAsignado: Spinner
    private lateinit var cardAsignacion: View
    private lateinit var btnAsignar: Button
    private var servicioActual: Servicio? = null
    private var esAdmin = false
    private var cerrajeros: List<Usuario> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_servicio_detalle)
        configurarEncabezado(getString(R.string.titulo_detalle_servicio))

        spEstado = findViewById(R.id.spEstado)
        tvEstado = findViewById(R.id.tvSEstado)
        tvValor = findViewById(R.id.tvSValor)
        pbServicioDetalle = findViewById(R.id.pbServicioDetalle)
        spAsignado = findViewById(R.id.spAsignado)
        cardAsignacion = findViewById(R.id.cardAsignacion)
        btnAsignar = findViewById(R.id.btnAsignar)

        spEstado.adapter = ArrayAdapter(
            this,
            R.layout.item_selector,
            EstadoServicio.values().map { it.etiqueta }
        ).apply {
            setDropDownViewResource(R.layout.item_selector_opcion)
        }

        findViewById<Button>(R.id.btnCambiarEstado).setOnClickListener { cambiarEstado() }
        findViewById<Button>(R.id.btnFactura).setOnClickListener { descargarFactura() }
        btnAsignar.setOnClickListener { asignar() }

        val servicioId = intent.getStringExtra(EXTRA_SERVICIO_ID).orEmpty()
        if (servicioId.isEmpty()) {
            Toast.makeText(this, R.string.sin_datos, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cargarRol()
        cargarServicio(servicioId)
    }

    private fun cargarServicio(servicioId: String) {
        pbServicioDetalle.visibility = View.VISIBLE

        FirestoreAPI.obtenerServicio(servicioId)
            .addOnSuccessListener { snapshot ->
                val servicio = Servicio.fromSnapshot(snapshot)
                if (servicio.id.isEmpty()) {
                    Toast.makeText(this, R.string.sin_datos, Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }
                servicioActual = servicio
                mostrar(servicio)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnCompleteListener {
                pbServicioDetalle.visibility = View.GONE
            }
    }

    private fun mostrar(servicio: Servicio) {
        findViewById<TextView>(R.id.tvSCliente).text = servicio.nombreCliente.ifBlank { servicio.clienteId }
        tvEstado?.let { UI.pintarEstado(it, servicio.estado) }
        findViewById<TextView>(R.id.tvSTipo).text =
            Formato.tipoServicio(servicio.tipoServicio, this)

        val tvDescripcion = findViewById<TextView>(R.id.tvSDescripcion)
        if (servicio.descripcion.isNotBlank()) {
            tvDescripcion.text = servicio.descripcion
            tvDescripcion.visibility = View.VISIBLE
        } else {
            tvDescripcion.visibility = View.GONE
        }

        findViewById<TextView>(R.id.tvSCiudad).text = getString(R.string.ciudad) + ": " +
            servicio.ciudad
        findViewById<TextView>(R.id.tvSDireccion).text = getString(R.string.direccion) + ": " +
            servicio.direccion
        findViewById<TextView>(R.id.tvSFecha).text = getString(R.string.fecha) + ": " +
            servicio.fecha?.let { Formato.fecha(it) + " · " + Formato.hora(it) }
        tvValor.text = if (Interaccion.Montos.ocultos(this)) {
            getString(R.string.montos_ocultos)
        } else {
            Formato.moneda(servicio.valor)
        }

        val indice = EstadoServicio.values().indexOf(servicio.estado)
        spEstado.setSelection(indice)
        pintarLineaTiempo(servicio)
        aplicarAsignacion()
    }

    private fun cargarRol() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreAPI.obtenerUsuario(uid)
            .addOnSuccessListener { doc ->
                esAdmin = doc.getString("rol") != RolUsuario.CERRAJERO.valor
                if (esAdmin) cargarCerrajeros(uid) else cardAsignacion.visibility = View.GONE
            }
    }

    private fun cargarCerrajeros(tallerId: String) {
        FirestoreAPI.obtenerUsuariosDelTaller(tallerId)
            .addOnSuccessListener { snapshot ->
                cerrajeros = snapshot.documents
                    .mapNotNull { Usuario.fromSnapshot(it) }
                    .filter { it.rol == RolUsuario.CERRAJERO }
                    .sortedBy { it.nombre.lowercase() }
                aplicarAsignacion()
            }
    }

    private fun aplicarAsignacion() {
        val servicio = servicioActual ?: return
        if (!esAdmin) return
        val opciones = mutableListOf(getString(R.string.sin_asignar))
        opciones += cerrajeros.map { it.nombre.ifBlank { it.correo } }
        spAsignado.adapter = ArrayAdapter(this, R.layout.item_selector, opciones).apply {
            setDropDownViewResource(R.layout.item_selector_opcion)
        }
        val indice = cerrajeros.indexOfFirst { it.uid == servicio.asignadoA }
        spAsignado.setSelection(if (indice >= 0) indice + 1 else 0)
        cardAsignacion.visibility = View.VISIBLE
    }

    private fun asignar() {
        val servicio = servicioActual ?: return
        val posicion = spAsignado.selectedItemPosition
        val uidAsignar = if (posicion <= 0) "" else cerrajeros[posicion - 1].uid
        btnAsignar.isEnabled = false
        FirestoreAPI.asignarServicio(servicio.id, uidAsignar)
            .addOnSuccessListener {
                servicioActual = servicio.copy(asignadoA = uidAsignar)
                Toast.makeText(this, R.string.servicio_asignado, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener { btnAsignar.isEnabled = true }
    }

    /** Línea de tiempo construida con las fechas disponibles del servicio. */
    private fun pintarLineaTiempo(servicio: Servicio) {
        val contenedor = findViewById<LinearLayout>(R.id.lineaTiempo)
        contenedor.removeAllViews()
        val inflador = LayoutInflater.from(this)

        fun agregar(tituloRes: Int, fecha: Timestamp?) {
            if (fecha == null) return
            val fila = inflador.inflate(R.layout.item_linea_tiempo, contenedor, false)
            fila.findViewById<TextView>(R.id.tlTitulo).setText(tituloRes)
            fila.findViewById<TextView>(R.id.tlFecha).text =
                Formato.fecha(fecha) + " · " + Formato.hora(fecha)
            contenedor.addView(fila)
        }

        agregar(R.string.tl_creado, servicio.fechaCreacion)
        agregar(R.string.tl_programado, servicio.fecha)
        if (servicio.estado == EstadoServicio.REALIZADO) {
            agregar(R.string.tl_finalizado, servicio.fechaFinalizacion)
        }

        contenedor.getChildAt(contenedor.childCount - 1)
            ?.findViewById<View>(R.id.tlLinea)?.visibility = View.INVISIBLE
    }

    private fun cambiarEstado() {
        val servicio = servicioActual ?: return
        val nuevoEstado = EstadoServicio.values()[spEstado.selectedItemPosition]
        if (nuevoEstado == servicio.estado) {
            Toast.makeText(this, R.string.estado_actualizado, Toast.LENGTH_SHORT).show()
            return
        }

        findViewById<Button>(R.id.btnCambiarEstado).isEnabled = false
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        FirestoreAPI.actualizarEstadoServicio(servicio.id, nuevoEstado)
            .addOnSuccessListener {
                FirestoreAPI.registrarHistorialEstado(
                    servicioId = servicio.id,
                    estadoAnterior = servicio.estado.valor,
                    estadoNuevo = nuevoEstado.valor,
                    usuarioId = uid
                ).addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        UI.mensajeErrorFirestore(this, e),
                        Toast.LENGTH_LONG
                    ).show()
                }
                servicioActual = servicio.copy(estado = nuevoEstado)
                UI.pintarEstado(tvEstado, nuevoEstado)
                Toast.makeText(this, R.string.estado_actualizado, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
                spEstado.setSelection(EstadoServicio.values().indexOf(servicio.estado))
            }
            .addOnCompleteListener {
                findViewById<Button>(R.id.btnCambiarEstado).isEnabled = true
            }
    }

    /** Genera la factura en PDF y abre el menú para guardarla o compartirla con el cliente. */
    private fun descargarFactura() {
        val servicio = servicioActual ?: return
        val archivo = FacturaPdf.generar(this, servicio)
        if (archivo == null) {
            Toast.makeText(this, R.string.factura_error, Toast.LENGTH_LONG).show()
            return
        }

        val enviar = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, archivo)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.factura_titulo))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(enviar, getString(R.string.compartir_factura)))
            Toast.makeText(this, R.string.factura_lista, Toast.LENGTH_SHORT).show()
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.factura_sin_app, Toast.LENGTH_LONG).show()
        }
    }
}