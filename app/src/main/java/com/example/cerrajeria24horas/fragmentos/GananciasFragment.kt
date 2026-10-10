package com.example.cerrajeria24horas.fragmentos

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cerrajeria24horas.R
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.Calculos
import com.example.cerrajeria24horas.util.Fechas
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.GraficoGananciasView
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.Preferencias
import com.example.cerrajeria24horas.util.UI
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GananciasFragment : Fragment(R.layout.fragment_ganancias) {

    private lateinit var raiz: View
    private var cifras = Triple(0.0, 0.0, 0.0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        raiz = view

        view.findViewById<View>(R.id.btnMontos).setOnClickListener {
            if (Interaccion.Montos.ocultos(requireContext())) {
                Interaccion.Montos.revelar()
            } else {
                Preferencias.guardarOcultarMontos(requireContext(), true)
                Interaccion.Montos.reiniciar()
            }
            aplicarMontos()
        }
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser != null) cargarGanancias()
    }

    private fun cargarGanancias() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val pbGanancias = raiz.findViewById<ProgressBar>(R.id.pbGanancias)
        pbGanancias.visibility = View.VISIBLE

        FirestoreAPI.obtenerServicios(uid)
            .addOnSuccessListener { snapshot ->
                val servicios = snapshot.documents
                    .mapNotNull { Servicio.fromSnapshot(it) }
                    .filterNot { it.id.isEmpty() }
                mostrar(servicios)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), UI.mensajeErrorFirestore(requireContext(), e), Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                pbGanancias.visibility = View.GONE
            }
    }

    private fun mostrar(servicios: List<Servicio>) {
        val delDia = Calculos.sumarValores(
            Calculos.serviciosRealizadosEn(servicios, Fechas.inicioDeHoy(), Fechas.inicioDeManana())
        )
        val deLaSemana = Calculos.sumarValores(
            Calculos.serviciosRealizadosEn(servicios, Fechas.inicioDeSemana(), Fechas.inicioDeProximaSemana())
        )
        val delMes = Calculos.sumarValores(
            Calculos.serviciosRealizadosEn(servicios, Fechas.inicioDeMes(), Fechas.inicioDeProximoMes())
        )
        cifras = Triple(delDia, deLaSemana, delMes)
        pintarGraficoSemanal(servicios)
        aplicarMontos()
    }

    private fun pintarGraficoSemanal(servicios: List<Servicio>) {
        val hoy = Fechas.inicioDeHoy()
        val dias = (6 downTo 0).map { atras ->
            Calendar.getInstance().apply {
                timeInMillis = hoy.timeInMillis
                add(Calendar.DAY_OF_MONTH, -atras)
            }
        }
        val valores = dias.map { inicio ->
            val fin = (inicio.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
            Calculos.sumarValores(Calculos.serviciosRealizadosEn(servicios, inicio, fin)).toFloat()
        }
        val formato = SimpleDateFormat("EEE", Locale.forLanguageTag("es-CO"))
        val etiquetas = dias.map { dia ->
            formato.format(dia.time).take(3).replaceFirstChar { it.uppercase() }
        }
        raiz.findViewById<GraficoGananciasView>(R.id.graficoGanancias).datos(valores, etiquetas)
    }

    private fun aplicarMontos() {
        val ocultos = Interaccion.Montos.ocultos(requireContext())
        val visibles = listOf(
            raiz.findViewById<TextView>(R.id.tvGananciaDia),
            raiz.findViewById<TextView>(R.id.tvGananciaSemana),
            raiz.findViewById<TextView>(R.id.tvGananciaMes)
        )
        val valores = listOf(cifras.first, cifras.second, cifras.third)
        visibles.forEachIndexed { indice, texto ->
            texto.text = if (ocultos) getString(R.string.montos_ocultos)
            else Formato.moneda(valores[indice])
        }

        val boton = raiz.findViewById<MaterialButton>(R.id.btnMontos)
        boton.setText(if (ocultos) R.string.mostrar_montos else R.string.ocultar_montos)
        boton.setIconResource(if (ocultos) R.drawable.ic_ojo_oculto else R.drawable.ic_ganancias)

        raiz.findViewById<GraficoGananciasView>(R.id.graficoGanancias).visibility =
            if (ocultos) View.GONE else View.VISIBLE
    }
}
