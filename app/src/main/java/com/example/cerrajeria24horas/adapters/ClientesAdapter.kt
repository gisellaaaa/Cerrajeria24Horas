package com.example.cerrajeria24horas.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.Cliente

class ClientesAdapter(
    private val alHacerClick: (Cliente) -> Unit
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    private val elementos = mutableListOf<Cliente>()

    fun setData(lista: List<Cliente>) {
        elementos.clear()
        elementos.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.enlazar(elementos[position])
    }

    override fun getItemCount(): Int = elementos.size

    inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvCliAvatar)
        private val tvNombre: TextView = itemView.findViewById(R.id.tvCliNombre)
        private val tvTelefono: TextView = itemView.findViewById(R.id.tvCliTelefono)
        private val tvCiudad: TextView = itemView.findViewById(R.id.tvCliCiudad)

        fun enlazar(cliente: Cliente) {
            tvNombre.text = cliente.nombreCompleto
            tvTelefono.text = cliente.telefono
            tvCiudad.text = cliente.ciudad
            pintarAvatar(cliente.nombreCompleto)
            itemView.setOnClickListener { alHacerClick(cliente) }
        }

        private fun pintarAvatar(nombre: String) {
            val colores = intArrayOf(
                R.color.azul_marino,
                R.color.azul_acento,
                R.color.azul_marino_claro
            )
            val color = colores[Math.floorMod(nombre.hashCode(), colores.size)]
            tvAvatar.backgroundTintList =
                ColorStateList.valueOf(itemView.context.getColor(color))
            tvAvatar.text = iniciales(nombre)
        }

        private fun iniciales(nombre: String): String {
            val partes = nombre.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            return when {
                partes.isEmpty() -> "?"
                partes.size == 1 -> partes[0].take(1).uppercase()
                else -> (partes[0].take(1) + partes[1].take(1)).uppercase()
            }
        }
    }
}
