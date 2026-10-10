package com.example.cerrajeria24horas

import android.content.Intent
import android.os.Bundle
import android.widget.TextView

/**
 * Muestra la política de privacidad o los términos, ambos escritos en el propio
 * proyecto para poder leerlos sin conexión ni consulta a Firestore.
 */
class LegalActivity : BaseActivity() {

    companion object {
        private const val TERMINOS = "terminos"

        fun abrir(actividad: android.app.Activity, terminos: Boolean) {
            actividad.startActivity(
                Intent(actividad, LegalActivity::class.java)
                    .putExtra(TERMINOS, terminos)
            )
            actividad.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal)

        val sonTerminos = intent.getBooleanExtra(TERMINOS, false)
        val titulo = if (sonTerminos) R.string.priv_terminos else R.string.priv_politica
        val texto = if (sonTerminos) R.string.legal_terminos else R.string.legal_politica

        configurarEncabezado(getString(titulo))
        findViewById<TextView>(R.id.tvLegalTitulo).text = getString(titulo)
        findViewById<TextView>(R.id.tvLegalTexto).text = getString(texto)
    }
}