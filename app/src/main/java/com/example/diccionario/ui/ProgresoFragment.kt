package com.example.diccionario.ui

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.diccionario.R
import com.example.diccionario.db.DBHelper
import com.example.diccionario.modelo.Palabra
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProgresoFragment : Fragment(R.layout.fragment_progreso) {

    private lateinit var db: SQLiteDatabase
    private lateinit var txtPalabra: TextView
    private lateinit var opciones: List<CardView>
    private lateinit var textos: List<TextView>

    private var listaJuego: MutableList<Palabra> = mutableListOf()
    private var indiceActual = 0
    private var palabraActual: Palabra? = null
    private var estaCargando = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        txtPalabra = view.findViewById(R.id.textoPalabra)

        val botonBack = view.findViewById<ImageButton>(R.id.boton_retroceder)
        botonBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                val bottomNav = requireActivity()
                    .findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav.selectedItemId = R.id.nav_home
            }
        }

        opciones = listOf(
            view.findViewById(R.id.opcion1),
            view.findViewById(R.id.opcion2),
            view.findViewById(R.id.opcion3),
            view.findViewById(R.id.opcion4)
        )

        textos = listOf(
            view.findViewById(R.id.txtOpcion1),
            view.findViewById(R.id.txtOpcion2),
            view.findViewById(R.id.txtOpcion3),
            view.findViewById(R.id.txtOpcion4)
        )

        listaJuego = obtenerListaJuego()
        listaJuego.shuffle()

        cargarPregunta()

        opciones.forEachIndexed { index, card ->
            card.setOnClickListener {
                if (!estaCargando) {
                    verificarRespuesta(textos[index].text.toString(), card)
                }
            }
        }
    }

    // Obtiene lista de palabras priorizando no aprendidas
    private fun obtenerListaJuego(): MutableList<Palabra> {
        val lista = mutableListOf<Palabra>()
        val resultado = db.rawQuery("SELECT * FROM palabra ORDER BY aprendida ASC, nahuat", null)
        resultado.use {
            if (it.moveToFirst()) {
                do {
                    lista.add(
                        Palabra(
                            it.getInt(0), it.getString(1), it.getString(2),
                            it.getString(3), it.getString(4), it.getString(5),
                            it.getInt(6), it.getInt(7)
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return lista
    }

    // Carga nueva pregunta con 4 opciones
    private fun cargarPregunta() {
        if (indiceActual >= listaJuego.size) {
            indiceActual = 0
            listaJuego = obtenerListaJuego()
            listaJuego.shuffle()
        }

        palabraActual = listaJuego[indiceActual]
        txtPalabra.text = palabraActual!!.nahuat

        val opcionesUnicas = mutableListOf<Palabra>()
        opcionesUnicas.add(palabraActual!!)

        val pool = listaJuego.shuffled()
        for (palabra in pool) {
            if (opcionesUnicas.size == 4) break
            if (opcionesUnicas.none { it.espanol == palabra.espanol }) {
                opcionesUnicas.add(palabra)
            }
        }

        opcionesUnicas.shuffle()
        textos.forEachIndexed { index, texto ->
            texto.text = opcionesUnicas[index].espanol
        }

        indiceActual++
        resetBordes()
    }

    // Verifica respuesta y marca como aprendida si es correcta
    private fun verificarRespuesta(respuesta: String, card: CardView) {
        estaCargando = true
        val correcta = palabraActual?.espanol

        if (respuesta == correcta) {
            aplicarBorde(card, R.color.verde)
            if (palabraActual?.aprendida == 0) {
                val values = ContentValues().apply { put("aprendida", 1) }
                db.update("palabra", values, "id = ?", arrayOf(palabraActual?.id.toString()))
            }
            mostrarToastVerdeRapido("+1 ${palabraActual?.nahuat} aprendida")
        } else {
            aplicarBorde(card, R.color.rojo)
        }

        card.postDelayed({
            if (isAdded) {
                cargarPregunta()
                estaCargando = false
            }
        }, 250)
    }

    // Aplica borde de color a la card
    private fun aplicarBorde(card: CardView, color: Int) {
        val drawable = GradientDrawable().apply {
            setColor(ContextCompat.getColor(requireContext(), R.color.white))
            setStroke(8, ContextCompat.getColor(requireContext(), color))
            cornerRadius = 24f
        }
        card.background = drawable
    }

    // Resetea bordes de todas las cards
    private fun resetBordes() {
        opciones.forEach {
            val drawable = GradientDrawable().apply {
                setColor(ContextCompat.getColor(requireContext(), R.color.white))
                cornerRadius = 24f
            }
            it.background = drawable
        }
    }

    // Toast personalizado
    @Suppress("DEPRECATION")
    private fun mostrarToastVerdeRapido(mensaje: String) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(60, 20, 60, 20)
        }

        val background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(requireContext(), R.color.verde))
            cornerRadius = 32f
        }
        layout.background = background

        val textView = TextView(requireContext()).apply {
            this.text = mensaje
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            textSize = 14f
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            gravity = Gravity.CENTER
        }

        layout.addView(textView)

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 80)
        toast.show()

        // Forzar ocultar después de 1 segundo
        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) db.close()
    }
}
