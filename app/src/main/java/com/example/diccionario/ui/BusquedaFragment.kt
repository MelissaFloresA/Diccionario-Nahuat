package com.example.diccionario.ui

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diccionario.R
import com.example.diccionario.adapter.PalabraAdapter
import com.example.diccionario.db.DBHelper
import com.example.diccionario.modelo.Palabra

class BusquedaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PalabraAdapter
    private lateinit var db: SQLiteDatabase
    private lateinit var searchView: SearchView
    private lateinit var textoVacio: TextView
    private lateinit var textoResultados: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_busqueda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar base de datos
        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        // Configurar UI
        val botonBack = view.findViewById<View>(R.id.boton_retroceder)
        botonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Configurar vistas
        recyclerView = view.findViewById(R.id.recycler_resultados)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        textoVacio = view.findViewById(R.id.texto_vacio)
        textoResultados = view.findViewById(R.id.texto_resultados)

        // Configurar SearchView
        searchView = view.findViewById(R.id.search_view)
        configurarSearchView()

        // Cargar las primeras 5 palabras al iniciar
        cargarPrimerasPalabras()
    }

    private fun configurarSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                buscarPalabras(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                buscarPalabras(newText ?: "")
                return true
            }
        })

    }

    private fun cargarPrimerasPalabras() {
        val lista = mutableListOf<Palabra>()

        // solo mostrar 5 palabras
        val resultado = db.rawQuery(
            "SELECT * FROM palabra ORDER BY id LIMIT 5",
            null
        )

        //Objeto con los resultados de bd
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

        if (lista.isNotEmpty()) {
            textoResultados.visibility = View.VISIBLE
            textoResultados.text = "Mostrando las primeras ${lista.size} palabras"
            textoVacio.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            adapter = PalabraAdapter(lista, requireContext(), db, true)
            recyclerView.adapter = adapter
        } else {
            textoVacio.visibility = View.VISIBLE
            textoVacio.text = "No hay palabras disponibles"
            textoResultados.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }
    }

    private fun buscarPalabras(busqueda: String) {
        if (busqueda.trim().isEmpty()) {
            // Si la búsqueda está vacía, mostrar las primeras 5 palabras
            cargarPrimerasPalabras()
            return
        }

        val lista = mutableListOf<Palabra>()

        //Busqueda por nahuat o por español
        val cursor = db.rawQuery(
            "SELECT * FROM palabra WHERE nahuat LIKE ? OR espanol LIKE ? ORDER BY nahuat",
            arrayOf("%$busqueda%", "%$busqueda%")
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

        if (lista.isEmpty()) {
            textoVacio.visibility = View.VISIBLE
            textoVacio.text = "No se encontraron resultados para \"$busqueda\""
            textoResultados.visibility = View.GONE
            recyclerView.visibility = View.GONE
        } else {
            textoVacio.visibility = View.GONE
            textoResultados.visibility = View.VISIBLE
            textoResultados.text = "Se encontraron ${lista.size} resultados para \"$busqueda\""
            recyclerView.visibility = View.VISIBLE

            adapter = PalabraAdapter(lista, requireContext(), db, true)
            recyclerView.adapter = adapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }
}