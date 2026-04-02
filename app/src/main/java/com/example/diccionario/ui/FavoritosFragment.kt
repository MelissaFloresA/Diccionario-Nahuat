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

class FavoritosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PalabraAdapter
    private lateinit var db: SQLiteDatabase
    private lateinit var textoVacio: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favoritos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar base de datos
        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        // Configurar UI
        val botonBack = view.findViewById<ImageButton>(R.id.boton_retroceder)
        textoVacio = view.findViewById(R.id.texto_vacio)

        botonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recycler_favoritos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarFavoritos()
    }

    private fun cargarFavoritos() {
        val lista = obtenerPalabrasFavoritas()

        if (lista.isEmpty()) {
            recyclerView.visibility = View.GONE
            textoVacio.visibility = View.VISIBLE
            textoVacio.text = "No tienes palabras favoritas aún.\n¡Agrega algunas desde la búsqueda o categorías!"
        } else {
            recyclerView.visibility = View.VISIBLE
            textoVacio.visibility = View.GONE

            adapter = PalabraAdapter(
                lista,
                requireContext(),
                db,
                true // Mostrar corazón para poder quitar de favoritos
            )
            recyclerView.adapter = adapter
        }
    }

    private fun obtenerPalabrasFavoritas(): MutableList<Palabra> {
        val lista = mutableListOf<Palabra>()
        
        val resultado = db.rawQuery(
            "SELECT * FROM palabra WHERE favorito = 1 ORDER BY nahuat",
            null
        )

        resultado.use {
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

    override fun onResume() {
        super.onResume()
        // Recargar favoritos cuando se vuelve al fragment
        if (::db.isInitialized && db.isOpen) {
            cargarFavoritos()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }
}