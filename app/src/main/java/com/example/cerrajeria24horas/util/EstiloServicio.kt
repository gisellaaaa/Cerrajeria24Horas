package com.example.cerrajeria24horas.util

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.example.cerrajeria24horas.R

/** Forma + color de ficha + icono de cada tipo de servicio, definidos en un solo lugar. */
data class EstiloTipo(
    @DrawableRes val forma: Int,
    @ColorRes val fondo: Int,
    @DrawableRes val icono: Int
)

object EstiloServicio {

    private const val APERTURA_DE_PUERTA = "APERTURA_DE_PUERTA"
    private const val CAMBIO_DE_CERRADURA = "CAMBIO_DE_CERRADURA"
    private const val INSTALACION_DE_CERRADURA = "INSTALACION_DE_CERRADURA"
    private const val REPARACION_DE_CERRADURA = "REPARACION_DE_CERRADURA"
    private const val DUPLICADO_DE_LLAVES = "DUPLICADO_DE_LLAVES"
    private const val APERTURA_DE_VEHICULO = "APERTURA_DE_VEHICULO"

    fun para(tipo: String?): EstiloTipo = when (tipo?.trim()?.uppercase()) {
        APERTURA_DE_PUERTA ->
            EstiloTipo(R.drawable.forma_sello, R.color.oro, R.drawable.ic_casa)
        CAMBIO_DE_CERRADURA ->
            EstiloTipo(R.drawable.forma_cuadrado, R.color.tinte_azul, R.drawable.ic_cerradura)
        INSTALACION_DE_CERRADURA ->
            EstiloTipo(R.drawable.forma_circulo, R.color.tinte_lavanda, R.drawable.ic_cerradura)
        REPARACION_DE_CERRADURA ->
            EstiloTipo(R.drawable.forma_cuadrado, R.color.durazno, R.drawable.ic_llave_doble)
        DUPLICADO_DE_LLAVES ->
            EstiloTipo(R.drawable.forma_sello, R.color.tinte_azul, R.drawable.ic_llave_doble)
        APERTURA_DE_VEHICULO ->
            EstiloTipo(R.drawable.forma_circulo, R.color.oro, R.drawable.ic_auto)
        else ->
            EstiloTipo(R.drawable.forma_circulo, R.color.fondo_suave, R.drawable.ic_key)
    }
}
