package com.example.diccionario.ui

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.diccionario.R
import com.example.diccionario.db.DBHelper
import com.example.diccionario.modelo.Palabra

class ProgresoFragment : Fragment(R.layout.fragment_progreso) {

    private lateinit var db: SQLiteDatabase

    private lateinit var txtPalabra: TextView
    private lateinit var opciones: List<CardView>
    private lateinit var textos: List<TextView>

    private var listaJuego: MutableList<Palabra> = mutableListOf()
    private var indiceActual = 0
    private var palabraActual: Palabra? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        txtPalabra = view.findViewById(R.id.textoPalabra)

        val botonBack = view.findViewById<ImageButton>(R.id.boton_retroceder)
        botonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
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
                verificarRespuesta(textos[index].text.toString(), card)
            }
        }
    }

    private fun obtenerListaJuego(): MutableList<Palabra> {
        val lista = mutableListOf<Palabra>()

        val cursor = db.rawQuery(
            "SELECT * FROM palabra ORDER BY aprendida ASC, nahuat",
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    lista.add(
                        Palabra(
                            it.getInt(0),
                            it.getString(1),
                            it.getString(2),
                            it.getString(3),
                            it.getString(4),
                            it.getString(5),
                            it.getInt(6),
                            it.getInt(7)
                        )
                    )
                } while (it.moveToNext())
            }
        }

        return lista
    }

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

        for (p in listaJuego.shuffled()) {
            if (opcionesUnicas.size == 4) break

            val existe = opcionesUnicas.any { it.espanol == p.espanol }

            if (!existe) {
                opcionesUnicas.add(p)
            }
        }

        opcionesUnicas.shuffle()

        textos.forEachIndexed { i, txt ->
            txt.text = opcionesUnicas[i].espanol
        }

        indiceActual++

        resetBordes()
    }

    private fun verificarRespuesta(respuesta: String, card: CardView) {
        val correcta = palabraActual?.espanol

        if (respuesta == correcta) {

            aplicarBorde(card, R.color.verde)

            val palabra = palabraActual

            if (palabra != null && palabra.aprendida == 0) {
                val values = ContentValues()
                values.put("aprendida", 1)

                db.update("palabra", values, "id = ?", arrayOf(palabra.id.toString()))
            }

            // SIEMPRE muestra toast
            mostrarToast("${palabra?.nahuat} aprendida")

        } else {
            aplicarBorde(card, R.color.rojo)
        }

        card.postDelayed({
            resetBordes()
            cargarPregunta()
        }, 800)
    }

    private fun aplicarBorde(card: CardView, color: Int) {
        val drawable = GradientDrawable()
        drawable.setColor(ContextCompat.getColor(requireContext(), R.color.white))
        drawable.setStroke(6, ContextCompat.getColor(requireContext(), color))
        drawable.cornerRadius = 24f
        card.background = drawable
    }

    private fun resetBordes() {
        opciones.forEach {
            val drawable = GradientDrawable()
            drawable.setColor(ContextCompat.getColor(requireContext(), R.color.white))
            drawable.cornerRadius = 24f
            it.background = drawable
        }
    }

    private fun mostrarToast(mensaje: String) {

        val toast = Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT)

        val textView = TextView(requireContext())
        textView.text = mensaje
        textView.setPadding(40, 20, 40, 20)
        textView.textSize = 16f
        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        val fondo = GradientDrawable()
        fondo.setColor(0xFF323232.toInt())
        fondo.cornerRadius = 24f

        textView.background = fondo

        toast.view = textView
        toast.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }
}