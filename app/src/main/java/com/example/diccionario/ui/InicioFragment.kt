package com.example.diccionario.ui

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.diccionario.R
import com.example.diccionario.db.DBHelper
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class InicioFragment : Fragment() {

    private lateinit var db: SQLiteDatabase
    private var palabraDelDiaId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar base de datos
        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        // Cargar conteos reales de las categorías
        cargarConteosCategorias(view)

        // Cargar palabra aleatoria del día
        cargarPalabraDelDia(view)

        // Configurar clics de categorías
        configurarCategoriaClick(view, R.id.card_categoria_familia, "Familia")
        configurarCategoriaClick(view, R.id.card_categoria_vestimenta, "Vestimenta")
        configurarCategoriaClick(view, R.id.card_categoria_escuela, "Escuela")
        configurarCategoriaClick(view, R.id.card_categoria_personas, "Personas")
        configurarCategoriaClick(view, R.id.card_categoria_alimentos, "Alimentos")
        configurarCategoriaClick(view, R.id.card_categoria_animales, "Animales")

        // Configurar clic de palabra del día
        view.findViewById<CardView>(R.id.card_palabra_dia).setOnClickListener {
            if (palabraDelDiaId != -1) {
                navegarADetallePalabraDelDia()
            }
        }

        // Configurar botón de audio de la palabra del día
        val botonAudio = view.findViewById<MaterialButton>(R.id.boton_audio_palabra)
        botonAudio.setOnClickListener {
            reproducirAudioPalabraDelDia()
        }
    }

    private fun cargarConteosCategorias(view: View) {
        // Obtener conteos de cada categoría
        val categorias = listOf("Familia", "Vestimenta", "Escuela", "Personas", "Alimentos", "Animales")

        categorias.forEach { categoria ->
            val conteo = obtenerConteoPorCategoria(categoria)
            when (categoria) {
                "Familia" -> {
                    val textoConteo = view.findViewById<TextView>(R.id.texto_conteo_categoria_familia)
                    textoConteo.text = "${conteo} palabras"
                }
                "Vestimenta" -> {
                    val textoConteo = view.findViewById<TextView>(R.id.texto_conteo_categoria_vestimenta)
                    textoConteo.text = "${conteo} palabras"
                }
                "Escuela" -> {
                    val textoConteo = view.findViewById<TextView>(R.id.texto_conteo_categoria_escuela)
                    textoConteo.text = "${conteo} palabras"
                }
                "Personas" -> {
                    val textoConteo = view.findViewById<TextView>(R.id.texto_conteo_categoria_personas)
                    textoConteo.text = "${conteo} palabras"
                }
                "Alimentos" -> {
                    val textoConteo = view.findViewById<TextView>(R.id.texto_conteo_categoria_alimentos)
                    textoConteo.text = "${conteo} palabras"
                }
                "Animales" -> {
                    val textoConteo = view.findViewById<TextView>(R.id.texto_conteo_categoria_animales)
                    textoConteo.text = "${conteo} palabras"
                }
            }
        }
    }

    private fun obtenerConteoPorCategoria(categoria: String): Int {
        var conteo = 0
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM palabra WHERE categoria = ?",
            arrayOf(categoria)
        )

        cursor.use {
            if (it.moveToFirst()) {
                conteo = it.getInt(0)
            }
        }

        return conteo
    }

    private fun cargarPalabraDelDia(view: View) {
        // Obtener la palabra aleatoria basada en la fecha actual
        val palabra = obtenerPalabraAleatoriaPorFecha()

        if (palabra != null) {
            palabraDelDiaId = palabra.id

            val textoPalabra = view.findViewById<TextView>(R.id.texto_palabra_nahuat)
            val textoSignificado = view.findViewById<TextView>(R.id.texto_significado_palabra)

            textoPalabra.text = capitalizarPrimeraLetra(palabra.nahuat)
            textoSignificado.text = capitalizarPrimeraLetra(palabra.espanol)
        }
    }

    private fun obtenerPalabraAleatoriaPorFecha(): com.example.diccionario.modelo.Palabra? {
        // Obtener el día del año para tener un número que cambia cada 24 horas
        val calendar = Calendar.getInstance()
        val diaDelAnio = calendar.get(Calendar.DAY_OF_YEAR)

        // Obtener el total de palabras
        var totalPalabras = 0
        val cursorCount = db.rawQuery("SELECT COUNT(*) FROM palabra", null)
        cursorCount.use {
            if (it.moveToFirst()) {
                totalPalabras = it.getInt(0)
            }
        }

        if (totalPalabras == 0) return null

        // Calcular el índice aleatorio basado en el día del año
        val indice = (diaDelAnio % totalPalabras) + 1

        // Obtener la palabra en ese índice
        var palabra: com.example.diccionario.modelo.Palabra? = null
        val cursor = db.rawQuery(
            "SELECT * FROM palabra WHERE id = ?",
            arrayOf(indice.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                palabra = com.example.diccionario.modelo.Palabra(
                    id = it.getInt(0),
                    espanol = it.getString(1),
                    nahuat = it.getString(2),
                    categoria = it.getString(3),
                    imagen = it.getString(4),
                    audio = it.getString(5),
                    favorito = it.getInt(6)
                )
            }
        }

        // Si por alguna razón no existe la palabra con ese ID, obtener una aleatoria
        if (palabra == null && totalPalabras > 0) {
            val cursorRandom = db.rawQuery(
                "SELECT * FROM palabra ORDER BY RANDOM() LIMIT 1",
                null
            )
            cursorRandom.use {
                if (it.moveToFirst()) {
                    palabra = com.example.diccionario.modelo.Palabra(
                        id = it.getInt(0),
                        espanol = it.getString(1),
                        nahuat = it.getString(2),
                        categoria = it.getString(3),
                        imagen = it.getString(4),
                        audio = it.getString(5),
                        favorito = it.getInt(6)
                    )
                }
            }
        }

        return palabra
    }

    private fun reproducirAudioPalabraDelDia() {
        if (palabraDelDiaId == -1) return

        val cursor = db.rawQuery(
            "SELECT audio FROM palabra WHERE id = ?",
            arrayOf(palabraDelDiaId.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                val audioName = it.getString(0)
                reproducirAudio(audioName)
            }
        }
    }

    private fun reproducirAudio(nombreAudio: String) {
        try {
            val idAudio = resources.getIdentifier(
                nombreAudio,
                "raw",
                requireContext().packageName
            )

            if (idAudio != 0) {
                val mediaPlayer = android.media.MediaPlayer.create(requireContext(), idAudio)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener {
                    it.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun navegarADetallePalabraDelDia() {
        if (palabraDelDiaId == -1) return

        val cursor = db.rawQuery(
            "SELECT * FROM palabra WHERE id = ?",
            arrayOf(palabraDelDiaId.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                val palabra = com.example.diccionario.modelo.Palabra(
                    id = it.getInt(0),
                    espanol = it.getString(1),
                    nahuat = it.getString(2),
                    categoria = it.getString(3),
                    imagen = it.getString(4),
                    audio = it.getString(5),
                    favorito = it.getInt(6)
                )

                val detalleFragment = DetallePalabraFragment().apply {
                    arguments = Bundle().apply {
                        putString("palabra_nahuat", capitalizarPrimeraLetra(palabra.nahuat))
                        putString("palabra_significado", capitalizarPrimeraLetra(palabra.espanol))
                        putString("categoria", capitalizarPrimeraLetra(palabra.categoria))
                        putString("imagen", palabra.imagen)
                        putString("audio", palabra.audio)
                        putInt("palabra_id", palabra.id)
                    }
                }

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detalleFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun capitalizarPrimeraLetra(texto: String): String {
        if (texto.isEmpty()) return texto
        return texto.substring(0, 1).uppercase() + texto.substring(1).lowercase()
    }

    private fun configurarCategoriaClick(view: View, cardId: Int, categoriaNombre: String) {
        view.findViewById<CardView>(cardId).setOnClickListener {
            val categoriaFragment = CategoriaFragment().apply {
                arguments = Bundle().apply {
                    putString("categoria", categoriaNombre)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, categoriaFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }
}