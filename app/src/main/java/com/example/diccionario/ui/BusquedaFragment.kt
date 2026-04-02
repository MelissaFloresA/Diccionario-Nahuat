package com.example.diccionario.ui

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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

    // Configura las vistas, base de datos y carga inicial de palabras
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar base de datos
        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        // Configurar botón de retroceso para volver al inicio o navegar atrás
        val botonBack = view.findViewById<ImageButton>(R.id.boton_retroceder)
        botonBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                bottomNav.selectedItemId = R.id.nav_home
            }
        }

        // Configurar RecyclerView para mostrar la lista de palabras
        recyclerView = view.findViewById(R.id.recycler_resultados)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Referencias a textos de estado
        textoVacio = view.findViewById(R.id.texto_vacio)
        textoResultados = view.findViewById(R.id.texto_resultados)

        // Configurar SearchView para búsqueda en tiempo real
        searchView = view.findViewById(R.id.search_view)
        configurarSearchView()

        // Cargar las primeras 5 palabras al iniciar el fragmento
        cargarPrimerasPalabras()
    }

    // Configura el listener del SearchView para detectar cambios y búsquedas
    private fun configurarSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Ejecuta búsqueda cuando el usuario presiona el botón de enviar
            override fun onQueryTextSubmit(query: String?): Boolean {
                buscarPalabras(query ?: "")
                return true
            }

            // Ejecuta búsqueda en tiempo real mientras el usuario escribe
            override fun onQueryTextChange(newText: String?): Boolean {
                buscarPalabras(newText ?: "")
                return true
            }
        })
    }

    // Carga las primeras 5 palabras de la base de datos al iniciar
    private fun cargarPrimerasPalabras() {
        val lista = mutableListOf<Palabra>()

        // Consulta SQL para obtener las primeras 5 palabras ordenadas por ID
        val resultado = db.rawQuery(
            "SELECT * FROM palabra ORDER BY id LIMIT 5",
            null
        )

        // Recorre el cursor y convierte cada registro en objeto
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
                        favorito = it.getInt(6),
                        aprendida = it.getInt(7)
                    )
                    lista.add(palabra)
                } while (it.moveToNext())
            }
        }

        // Muestra la lista si hay palabras, o mensaje de vacío si no
        if (lista.isNotEmpty()) {
            textoResultados.visibility = View.VISIBLE
            textoResultados.text = getString(R.string.busqueda_primeras_palabras, lista.size)
            textoVacio.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            adapter = PalabraAdapter(lista, requireContext(), db, true)
            recyclerView.adapter = adapter
        } else {
            textoVacio.visibility = View.VISIBLE
            textoVacio.text = getString(R.string.busqueda_sin_palabras)
            textoResultados.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }
    }

    // Busca palabras en la base de datos según el texto ingresado
    private fun buscarPalabras(busqueda: String) {
        // Si la búsqueda está vacía, muestra las primeras 5 palabras
        if (busqueda.trim().isEmpty()) {
            cargarPrimerasPalabras()
            return
        }

        val lista = mutableListOf<Palabra>()

        // Consulta SQL que busca coincidencias en náhuat o español
        val cursor = db.rawQuery(
            "SELECT * FROM palabra WHERE nahuat LIKE ? OR espanol LIKE ? ORDER BY nahuat",
            arrayOf("%$busqueda%", "%$busqueda%")
        )

        // Convierte los resultados en objetos Palabra
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
                        favorito = it.getInt(6),
                        aprendida = it.getInt(7)
                    )
                    lista.add(palabra)
                } while (it.moveToNext())
            }
        }

        // Muestra resultados o mensaje de "no encontrados"
        if (lista.isEmpty()) {
            textoVacio.visibility = View.VISIBLE
            textoVacio.text = getString(R.string.busqueda_sin_resultados, busqueda)
            textoResultados.visibility = View.GONE
            recyclerView.visibility = View.GONE
        } else {
            textoVacio.visibility = View.GONE
            textoResultados.visibility = View.VISIBLE
            textoResultados.text = getString(R.string.busqueda_resultados_encontrados, lista.size, busqueda)
            recyclerView.visibility = View.VISIBLE

            adapter = PalabraAdapter(lista, requireContext(), db, true)
            recyclerView.adapter = adapter
        }
    }

    // Cierra la conexión a la base de datos cuando el fragmento se destruye
    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }
}