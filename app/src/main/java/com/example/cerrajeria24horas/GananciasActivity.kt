package com.example.cerrajeria24horas

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.cerrajeria24horas.data.FirestoreAPI
import com.example.cerrajeria24horas.data.Servicio
import com.example.cerrajeria24horas.util.Calculos
import com.example.cerrajeria24horas.util.Fechas
import com.example.cerrajeria24horas.util.Formato
import com.example.cerrajeria24horas.util.Interaccion
import com.example.cerrajeria24horas.util.Preferencias
import com.example.cerrajeria24horas.util.UI
import com.google.firebase.auth.FirebaseAuth

class GananciasActivity : BaseActivity() {

    private var cargado = false
    private var cifras = Triple(0.0, 0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ganancias)
        configurarEncabezado(getString(R.string.titulo_ganancias))

        findViewById<View>(R.id.btnMontos).setOnClickListener {
            if (Interaccion.Montos.ocultos(this)) {
                Interaccion.Montos.revelar()
            } else {
                Preferencias.guardarOcultarMontos(this, true)
                Interaccion.Montos.reiniciar()
            }
            aplicarMontos()
        }

        cargado = true
    }

    override fun onResume() {
        super.onResume()
        if (cargado) {
            cargarGanancias()
        }
    }

    private fun cargarGanancias() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val pbGanancias = findViewById<ProgressBar>(R.id.pbGanancias)
        pbGanancias.visibility = View.VISIBLE

        FirestoreAPI.obtenerServicios(uid)
            .addOnSuccessListener { snapshot ->
                val servicios = snapshot.documents
                    .mapNotNull { Servicio.fromSnapshot(it) }
                    .filterNot { it.id.isEmpty() }
                mostrar(servicios)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, UI.mensajeErrorFirestore(this, e), Toast.LENGTH_LONG).show()
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
        aplicarMontos()
    }

    /** Con "Ocultar montos" las cifras se muestran como puntos hasta que se revelan. */
    private fun aplicarMontos() {
        val ocultos = Interaccion.Montos.ocultos(this)
        val visibles = listOf(
            findViewById<TextView>(R.id.tvGananciaDia),
            findViewById<TextView>(R.id.tvGananciaSemana),
            findViewById<TextView>(R.id.tvGananciaMes)
        )
        val valores = listOf(cifras.first, cifras.second, cifras.third)
        visibles.forEachIndexed { indice, texto ->
            texto.text = if (ocultos) getString(R.string.montos_ocultos) else Formato.moneda(valores[indice])
        }

        val boton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnMontos)
        boton.setText(if (ocultos) R.string.mostrar_montos else R.string.ocultar_montos)
        boton.setIconResource(if (ocultos) R.drawable.ic_ojo_oculto else R.drawable.ic_ganancias)
    }
}