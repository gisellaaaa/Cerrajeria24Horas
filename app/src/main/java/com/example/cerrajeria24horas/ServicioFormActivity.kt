package com.example.cerrajeria24horas

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.cerrajeria24horas.data.Cliente
import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.data.TiposServicio
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.FormatoPrecioWatcher
import com.example.cerrajeria24horas.util.Precio
import com.example.cerrajeria24horas.util.UI
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ServicioFormActivity : BaseActivity() {

    companion object {
        const val EXTRA_CLIENTE_ID = "clienteId"
        const val EXTRA_NOMBRE_CLIENTE = "nombreCliente"
    }

    private lateinit var spCliente: Spinner
    private lateinit var spTipo: Spinner
    private lateinit var etDescripcion: EditText
    private lateinit var etCiudad: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHora: EditText
    private lateinit var etValor: EditText

    private val clientes = mutableListOf<Cliente>()
    private var clienteSeleccionadoId = ""
    private var fechaSeleccionada: Calendar = Calendar.getInstance()
    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var inicializado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_servicio_form)
        configurarEncabezado(getString(R.string.subir_servicio))

        spCliente = findViewById(R.id.spCliente)
        spTipo = findViewById(R.id.spTipo)
        etDescripcion = findViewById(R.id.etDescripcion)
        etCiudad = findViewById(R.id.etCiudad)
        etDireccion = findViewById(R.id.etDireccion)
        etFecha = findViewById(R.id.etFecha)
        etHora = findViewById(R.id.etHora)
        etValor = findViewById(R.id.etValor)
        configurarFormatoValor()

        spTipo.adapter = ArrayAdapter(
            this,
            R.layout.item_selector,
            resources.getStringArray(R.array.tipos_servicio_etiquetas)
        ).apply {
            setDropDownViewResource(R.layout.item_selector_opcion)
        }

        etFecha.setText(formatoFecha.format(fechaSeleccionada.time))
        etHora.setText(Formato.horaCorta(fechaSeleccionada))

        etFecha.setOnClickListener { elegirFecha() }
        etHora.setOnClickListener { elegirHora() }

        spCliente.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position in clientes.indices) {
                    val cliente = clientes[position]
                    clienteSeleccionadoId = cliente.clienteId
                    if (inicializado) {
                        if (etCiudad.text.isEmpty()) etCiudad.setText(cliente.ciudad)
                        if (etDireccion.text.isEmpty()) etDireccion.setText(cliente.direccion)
                    }
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGuardarServicio)
            .setOnClickListener { guardar() }

        cargarClientes()
    }

    private fun configurarFormatoValor() {
        etValor.addTextChangedListener(FormatoPrecioWatcher(etValor))
    }

    private fun cargarClientes() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreAPI.obtenerClientes(uid)
            .addOnSuccessListener { snapshot ->
                clientes.clear()
                snapshot.documents
                    .mapNotNull { Cliente.fromSnapshot(it) }
                    .filterNot { it.id.isEmpty() }
                    .forEach { clientes.add(it) }

                val nombres = clientes.map { it.nombreCompleto }
                    .ifEmpty { listOf(getString(R.string.sin_clientes)) }
                spCliente.adapter = AdaptadorClientes(
                    this,
                    nombres,
                    if (clientes.isEmpty()) 0 else -1
                ).apply {
                    // La lista desplegable no debe llevar la flecha del selector.
                    setDropDownViewResource(R.layout.item_selector_opcion)
                }

                val extraId = intent.getStringExtra(EXTRA_CLIENTE_ID).orEmpty()
                val extraNombre = intent.getStringExtra(EXTRA_NOMBRE_CLIENTE).orEmpty()
                val indice = if (extraId.isNotEmpty()) {
                    clientes.indexOfFirst { it.clienteId == extraId }
                } else if (extraNombre.isNotEmpty()) {
                    clientes.indexOfFirst { it.nombreCompleto == extraNombre }
                } else {
                    -1
                }
                inicializado = true
                if (indice >= 0) spCliente.setSelection(indice)
                else if (clientes.isNotEmpty()) spCliente.setSelection(0)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
            }
    }

    private fun elegirFecha() {
        val selector = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.elegir_fecha)
            .setSelection(utcMillisDe(fechaSeleccionada))
            .build()
        selector.addOnPositiveButtonClickListener { milis ->
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = milis
            fechaSeleccionada.set(
                utc.get(Calendar.YEAR),
                utc.get(Calendar.MONTH),
                utc.get(Calendar.DAY_OF_MONTH)
            )
            etFecha.setText(formatoFecha.format(fechaSeleccionada.time))
        }
        selector.show(supportFragmentManager, "selector_fecha")
    }

    private fun elegirHora() {
        val selector = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(fechaSeleccionada.get(Calendar.HOUR_OF_DAY))
            .setMinute(fechaSeleccionada.get(Calendar.MINUTE))
            .build()
        selector.addOnPositiveButtonClickListener {
            fechaSeleccionada.set(Calendar.HOUR_OF_DAY, selector.hour)
            fechaSeleccionada.set(Calendar.MINUTE, selector.minute)
            etHora.setText(Formato.horaCorta(fechaSeleccionada))
        }
        selector.show(supportFragmentManager, "selector_hora")
    }

    private fun utcMillisDe(fecha: Calendar): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        utc.set(Calendar.YEAR, fecha.get(Calendar.YEAR))
        utc.set(Calendar.MONTH, fecha.get(Calendar.MONTH))
        utc.set(Calendar.DAY_OF_MONTH, fecha.get(Calendar.DAY_OF_MONTH))
        return utc.timeInMillis
    }

    private fun guardar() {
        if (clienteSeleccionadoId.isBlank()) {
            Toast.makeText(this, R.string.selecciona_un_cliente, Toast.LENGTH_SHORT).show()
            return
        }
        val ciudad = etCiudad.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()
        if (ciudad.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, R.string.cliente_obligatorio, Toast.LENGTH_SHORT).show()
            return
        }

        val textoValor = etValor.text.toString().trim()
        val valor = Precio.aDouble(textoValor)
        if (valor <= 0) {
            Toast.makeText(this, R.string.valor_invalido, Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, R.string.error_red, Toast.LENGTH_LONG).show()
            return
        }

        val posicionTipo = spTipo.selectedItemPosition
        val tipo = if (posicionTipo in TiposServicio.valores.indices) {
            TiposServicio.valores[posicionTipo]
        } else {
            TiposServicio.valores.first()
        }
        val nombreCliente =
            clientes.firstOrNull { it.clienteId == clienteSeleccionadoId }?.nombreCompleto
                ?: ""

        val servicio = Servicio(
            clienteId = clienteSeleccionadoId,
            nombreCliente = nombreCliente,
            tipoServicio = tipo,
            descripcion = etDescripcion.text.toString().trim(),
            ciudad = ciudad,
            direccion = direccion,
            fecha = Timestamp(fechaSeleccionada.time),
            valor = valor,
            estado = EstadoServicio.PENDIENTE,
            cerrajeroId = uid
        )

        mostrarCarga(true)
        FirestoreAPI.guardarServicio(servicio)
            .addOnSuccessListener {
                mostrarCarga(false)
                Toast.makeText(this, R.string.servicio_guardado, Toast.LENGTH_LONG).show()
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
            .addOnFailureListener { e ->
                mostrarCarga(false)
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
            }
    }

    private inner class AdaptadorClientes(
        context: Context,
        items: List<String>,
        private val indiceMarcado: Int
    ) : ArrayAdapter<String>(context, R.layout.item_selector, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vista = super.getView(position, convertView, parent)
            aplicarColor(vista, position)
            return vista
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vista = super.getDropDownView(position, convertView, parent)
            aplicarColor(vista, position)
            return vista
        }

        private fun aplicarColor(vista: View?, position: Int) {
            (vista as? TextView)?.setTextColor(
                context.getColor(
                    if (position == indiceMarcado) R.color.texto_secundario
                    else R.color.texto_principal
                )
            )
        }
    }

    private fun mostrarCarga(activo: Boolean) {
        findViewById<View>(R.id.pbServicioForm).visibility =
            if (activo) View.VISIBLE else View.GONE
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGuardarServicio)
            .isEnabled = !activo
    }
}