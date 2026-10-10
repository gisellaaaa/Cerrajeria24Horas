package com.example.cerrajeria24horas.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cerrajeria24horas.R

/**
 * Servicios que se van mostrando, uno por página, en el carrusel de la bienvenida.
 * Una sola familia visual: icono en círculo, título y descripción.
 */
class BienvenidaServiciosAdapter :
    RecyclerView.Adapter<BienvenidaServiciosAdapter.ServicioVH>() {

    data class Servicio(val icono: Int, val acento: Int, val titulo: Int, val descripcion: Int)

    private val servicios = listOf(
        Servicio(
            R.drawable.ic_llave_doble,
            R.color.superficie_servicios,
            R.string.servicio_carrusel_1_titulo,
            R.string.servicio_carrusel_1_desc
        ),
        Servicio(
            R.drawable.ic_auto,
            R.color.superficie_ganancias,
            R.string.servicio_carrusel_2_titulo,
            R.string.servicio_carrusel_2_desc
        ),
        Servicio(
            R.drawable.ic_cerradura,
            R.color.superficie_inicio,
            R.string.servicio_carrusel_3_titulo,
            R.string.servicio_carrusel_3_desc
        ),
        Servicio(
            R.drawable.ic_key,
            R.color.superficie_clientes,
            R.string.servicio_carrusel_4_titulo,
            R.string.servicio_carrusel_4_desc
        )
    )

    val total: Int get() = servicios.size

    class ServicioVH(view: View) : RecyclerView.ViewHolder(view) {
        val circulo: View = view.findViewById(R.id.carCirculo)
        val icono: ImageView = view.findViewById(R.id.carIcono)
        val titulo: TextView = view.findViewById(R.id.carTitulo)
        val descripcion: TextView = view.findViewById(R.id.carDescripcion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioVH {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bienvenida_servicio, parent, false)
        return ServicioVH(vista)
    }

    override fun getItemCount(): Int = servicios.size

    override fun onBindViewHolder(holder: ServicioVH, position: Int) {
        val servicio = servicios[position]
        val contexto = holder.itemView.context
        holder.icono.setImageResource(servicio.icono)
        holder.titulo.setText(servicio.titulo)
        holder.descripcion.setText(servicio.descripcion)
        val acento = ContextCompat.getColor(contexto, servicio.acento)
        holder.circulo.backgroundTintList = ColorStateList.valueOf(acento)
    }
}
