package com.example.cerrajeria24horas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.UI

class ServiciosAdapter(
    private val alHacerClick: (Servicio) -> Unit
) : RecyclerView.Adapter<ServiciosAdapter.ServicioViewHolder>() {

    private val elementos = mutableListOf<Servicio>()

    fun setData(lista: List<Servicio>) {
        elementos.clear()
        elementos.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servicio, parent, false)
        return ServicioViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        holder.enlazar(elementos[position])
    }

    override fun getItemCount(): Int = elementos.size

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
}