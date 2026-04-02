package com.example.diccionario.ui

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diccionario.R
import com.example.diccionario.adapter.PalabraAdapter
import com.example.diccionario.db.DBHelper
import com.example.diccionario.modelo.Palabra

class CategoriaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PalabraAdapter
    private lateinit var db: SQLiteDatabase
    private lateinit var textoProgreso: TextView
    private var categoria: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoria = it.getString("categoria", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categoria, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        val titulo = view.findViewById<TextView>(R.id.texto_titulo_categoria)
        val botonBack = view.findViewById<ImageButton>(R.id.boton_retroceder)
        textoProgreso = view.findViewById(R.id.texto_progreso)

        titulo.text = categoria

        botonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        recyclerView = view.findViewById(R.id.recycler_palabras)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarPalabras()
        actualizarProgreso()
    }

    //Muestra palabras
    private fun cargarPalabras() {
        val lista = obtenerPalabrasPorCategoria()

        adapter = PalabraAdapter(
            lista,
            requireContext(),
            db,
            false
        )

        recyclerView.adapter = adapter
    }

    //Consulta SQL para obtener palabras por categoría
    private fun obtenerPalabrasPorCategoria(): MutableList<Palabra> {
        val lista = mutableListOf<Palabra>()

        val resultado = db.rawQuery(
            "SELECT * FROM palabra WHERE categoria = ? ORDER BY nahuat",
            arrayOf(categoria)
        )

        resultado.use {
            if (it.moveToFirst()) {
                do {
                    val palabra = Palabra(
                        it.getInt(0),
                        it.getString(1),
                        it.getString(2),
                        it.getString(3),
                        it.getString(4),
                        it.getString(5),
                        it.getInt(6),
                        it.getInt(7)
                    )
                    lista.add(palabra)
                } while (it.moveToNext())
            }
        }

        return lista
    }

    // Actualiza texto de progreso para calcular progreso
    private fun actualizarProgreso() {
        val totalCursor = db.rawQuery(
            "SELECT COUNT(*) FROM palabra WHERE categoria = ?",
            arrayOf(categoria)
        )

        val aprendidasCursor = db.rawQuery(
            "SELECT COUNT(*) FROM palabra WHERE categoria = ? AND aprendida = 1",
            arrayOf(categoria)
        )

        var total = 0
        var aprendidas = 0

        if (totalCursor.moveToFirst()) {
            total = totalCursor.getInt(0)
        }

        if (aprendidasCursor.moveToFirst()) {
            aprendidas = aprendidasCursor.getInt(0)
        }

        totalCursor.close()
        aprendidasCursor.close()

        // usamos string.xml para acceder a texto
        textoProgreso.text = getString(R.string.progreso_categoria, aprendidas, total)
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized && db.isOpen) {
            actualizarProgreso()
            cargarPalabras() // refresca lista por si cambió estado
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }
}
