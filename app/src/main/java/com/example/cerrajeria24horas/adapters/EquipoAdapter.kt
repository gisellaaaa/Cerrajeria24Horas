package com.example.cerrajeria24horas.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.RolUsuario
import com.example.cerrajeria24horas.data.Usuario

/** Lista de miembros del taller: administrador y cerrajeros, con su rol. */
class EquipoAdapter : RecyclerView.Adapter<EquipoAdapter.MiembroViewHolder>() {

    private val elementos = mutableListOf<Usuario>()

    fun setData(lista: List<Usuario>) {
        elementos.clear()
        elementos.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiembroViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_miembro, parent, false)
        return MiembroViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MiembroViewHolder, position: Int) {
        holder.enlazar(elementos[position])
    }

    override fun getItemCount(): Int = elementos.size

    class MiembroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvMiembroAvatar)
        private val tvNombre: TextView = itemView.findViewById(R.id.tvMiembroNombre)
        private val tvCorreo: TextView = itemView.findViewById(R.id.tvMiembroCorreo)
        private val tvRol: TextView = itemView.findViewById(R.id.tvMiembroRol)

        fun enlazar(usuario: Usuario) {
            val contexto = itemView.context
            tvNombre.text = usuario.nombre.ifBlank { usuario.correo }
            tvCorreo.text = usuario.correo
            tvAvatar.text = iniciales(usuario.nombre.ifBlank { usuario.correo })
            tvAvatar.backgroundTintList = ColorStateList.valueOf(
                contexto.getColor(
                    if (usuario.rol == RolUsuario.ADMIN) R.color.azul_marino
                    else R.color.azul_acento
                )
            )
            tvRol.setText(
                if (usuario.rol == RolUsuario.ADMIN) R.string.rol_administrador
                else R.string.rol_cerrajero
            )
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
