package com.example.cerrajeria24horas.adapters

import com.example.cerrajeria24horas.data.EstadoServicio
import com.example.cerrajeria24horas.data.Servicio

/** Filas de la lista de servicios: encabezado de grupo por estado o un servicio. */
sealed interface FilaServicios {
    data class Encabezado(val estado: EstadoServicio, val cantidad: Int) : FilaServicios
    data class Articulo(val servicio: Servicio) : FilaServicios
}
