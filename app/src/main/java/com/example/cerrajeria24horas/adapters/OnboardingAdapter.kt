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

/** Páginas de la bienvenida inicial. Una sola familia visual: icono en círculo, título y texto. */
class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.PaginaVH>() {

    data class Pagina(val icono: Int, val acento: Int, val titulo: Int, val descripcion: Int)

    private val paginas = listOf(
        Pagina(
            R.drawable.ic_inicio,
            R.color.superficie_inicio,
            R.string.onboarding_titulo_1,
            R.string.onboarding_desc_1
        ),
        Pagina(
            R.drawable.ic_cerradura,
            R.color.superficie_servicios,
            R.string.onboarding_titulo_2,
            R.string.onboarding_desc_2
        ),
        Pagina(
            R.drawable.ic_grafico,
            R.color.superficie_ganancias,
            R.string.onboarding_titulo_3,
            R.string.onboarding_desc_3
        )
    )

    val total: Int get() = paginas.size

    class PaginaVH(view: View) : RecyclerView.ViewHolder(view) {
        val circulo: View = view.findViewById(R.id.onbCirculo)
        val icono: ImageView = view.findViewById(R.id.onbIcono)
        val titulo: TextView = view.findViewById(R.id.onbTitulo)
        val descripcion: TextView = view.findViewById(R.id.onbDescripcion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaginaVH {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return PaginaVH(vista)
    }

    override fun getItemCount(): Int = paginas.size

    override fun onBindViewHolder(holder: PaginaVH, position: Int) {
        val pagina = paginas[position]
        val contexto = holder.itemView.context
        holder.icono.setImageResource(pagina.icono)
        holder.titulo.setText(pagina.titulo)
        holder.descripcion.setText(pagina.descripcion)
        val acento = ContextCompat.getColor(contexto, pagina.acento)
        holder.circulo.backgroundTintList = ColorStateList.valueOf(acento)
    }
}
