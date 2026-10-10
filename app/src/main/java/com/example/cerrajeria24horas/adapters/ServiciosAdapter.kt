package com.example.cerrajeria24horas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.UI

class ServiciosAdapter(
    private val alHacerClick: (Servicio) -> Unit
) : ListAdapter<FilaServicios, RecyclerView.ViewHolder>(DIFERENCIAS) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is FilaServicios.Encabezado -> TIPO_ENCABEZADO
        is FilaServicios.Articulo -> TIPO_ARTICULO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflador = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_ENCABEZADO) {
            EncabezadoViewHolder(inflador.inflate(R.layout.item_encabezado_grupo, parent, false))
        } else {
            ServicioViewHolder(inflador.inflate(R.layout.item_servicio, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val fila = getItem(position)) {
            is FilaServicios.Encabezado -> (holder as EncabezadoViewHolder).enlazar(fila)
            is FilaServicios.Articulo -> (holder as ServicioViewHolder).enlazar(fila.servicio)
        }
    }

    private class EncabezadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tvGrupoTitulo)
        private val cantidad: TextView = itemView.findViewById(R.id.tvGrupoCantidad)

        fun enlazar(fila: FilaServicios.Encabezado) {
            titulo.text = fila.estado.etiqueta
            cantidad.text = fila.cantidad.toString()
        }
    }

    inner class ServicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCliente: TextView = itemView.findViewById(R.id.tvServCliente)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvServEstado)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvServTipo)
        private val tvCiudad: TextView = itemView.findViewById(R.id.tvServCiudad)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvServFecha)
        private val tvValor: TextView = itemView.findViewById(R.id.tvServValor)

        fun enlazar(servicio: Servicio) {
            tvCliente.text = servicio.nombreCliente.ifBlank { servicio.clienteId }
            UI.pintarEstado(tvEstado, servicio.estado)
            tvTipo.text = Formato.tipoServicio(servicio.tipoServicio, itemView.context)
            tvCiudad.text = listOf(servicio.ciudad, servicio.direccion)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            tvFecha.text = servicio.fecha?.let { Formato.fechaCorta(it) }
            tvValor.text = if (Interaccion.Montos.ocultos(itemView.context)) {
                itemView.context.getString(R.string.montos_ocultos)
            } else {
                Formato.moneda(servicio.valor)
            }
            itemView.setOnClickListener { alHacerClick(servicio) }
        }
    }

    companion object {
        private const val TIPO_ENCABEZADO = 0
        private const val TIPO_ARTICULO = 1

        private val DIFERENCIAS = object : DiffUtil.ItemCallback<FilaServicios>() {
            override fun areItemsTheSame(viejo: FilaServicios, nuevo: FilaServicios): Boolean =
                clave(viejo) == clave(nuevo)

            override fun areContentsTheSame(viejo: FilaServicios, nuevo: FilaServicios): Boolean =
                viejo == nuevo

            private fun clave(fila: FilaServicios): String = when (fila) {
                is FilaServicios.Encabezado -> "enc_${fila.estado.valor}"
                is FilaServicios.Articulo -> fila.servicio.id
            }
        }
    }
}
