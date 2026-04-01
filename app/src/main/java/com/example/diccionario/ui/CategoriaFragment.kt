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

        // Inicializar base de datos
        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        // Configurar UI
        val titulo = view.findViewById<TextView>(R.id.texto_titulo_categoria)
        val botonBack = view.findViewById<ImageButton>(R.id.boton_retroceder)

        titulo.text = categoria

        botonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recycler_palabras)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarPalabras()
    }

    private fun cargarPalabras() {
        val lista = obtenerPalabrasPorCategoria()

        adapter = PalabraAdapter(
            lista,
            requireContext(),
            db,
            false // No mostrar corazón en categoría
        )

        recyclerView.adapter = adapter
    }

    private fun obtenerPalabrasPorCategoria(): MutableList<Palabra> {
        val lista = mutableListOf<Palabra>()

        // CAMBIADO: "diccionario" -> "palabra"
        val cursor = db.rawQuery(
            "SELECT * FROM palabra WHERE categoria = ? ORDER BY nahuat",
            arrayOf(categoria)
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val palabra = Palabra(
                        id = it.getInt(0),
                        espanol = it.getString(1),
                        nahuat = it.getString(2),
                        categoria = it.getString(3),
                        imagen = it.getString(4),
                        audio = it.getString(5),
                        favorito = it.getInt(6)
                    )
                    lista.add(palabra)
                } while (it.moveToNext())
            }
        }

        return lista
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }
}