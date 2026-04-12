package com.example.diccionario.ui

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.diccionario.R
import com.example.diccionario.db.DBHelper
import com.example.diccionario.modelo.Palabra
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

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

        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        cargarConteosCategorias(view)
        cargarPalabraDelDia(view)

        configurarCategoriaClick(view, R.id.card_categoria_familia, "Familia")
        configurarCategoriaClick(view, R.id.card_categoria_vestimenta, "Vestimenta")
        configurarCategoriaClick(view, R.id.card_categoria_escuela, "Escuela")
        configurarCategoriaClick(view, R.id.card_categoria_personas, "Personas")
        configurarCategoriaClick(view, R.id.card_categoria_alimentos, "Alimentos")
        configurarCategoriaClick(view, R.id.card_categoria_animales, "Animales")

        view.findViewById<CardView>(R.id.card_palabra_dia).setOnClickListener {
            if (palabraDelDiaId != -1) {
                navegarADetallePalabra()
            }
        }

        view.findViewById<MaterialButton>(R.id.boton_audio_palabra).setOnClickListener {
            obtenerNombreAudio()
        }
    }

    private fun cargarConteosCategorias(view: View) {
        val categorias = listOf(
            "Familia", "Vestimenta", "Escuela", "Personas", "Alimentos", "Animales"
        )

        categorias.forEach { categoria ->
            val conteo = obtenerConteo(categoria)

            val idTexto = when (categoria) {
                "Familia" -> R.id.texto_conteo_categoria_familia
                "Vestimenta" -> R.id.texto_conteo_categoria_vestimenta
                "Escuela" -> R.id.texto_conteo_categoria_escuela
                "Personas" -> R.id.texto_conteo_categoria_personas
                "Alimentos" -> R.id.texto_conteo_categoria_alimentos
                "Animales" -> R.id.texto_conteo_categoria_animales
                else -> null
            }

            idTexto?.let {
                view.findViewById<TextView>(it).text = getString(R.string.conteo_palabras, conteo)
            }
        }
    }

    private fun obtenerConteo(categoria: String): Int {
        var total = 0
        val resultado = db.rawQuery(
            "SELECT COUNT(*) FROM palabra WHERE categoria = ?",
            arrayOf(categoria)
        )
        resultado.use {
            if (it.moveToFirst()) {
                total = it.getInt(0)
            }
        }
        return total
    }

    private fun cargarPalabraDelDia(view: View) {
        val palabra = obtenerPalabraDelDia()
        palabra?.let {
            palabraDelDiaId = it.id
            view.findViewById<TextView>(R.id.texto_palabra_nahuat).text = capitalizar(it.nahuat)
            view.findViewById<TextView>(R.id.texto_significado_palabra).text = capitalizar(it.espanol)
        }
    }

    // Obtiene la palabra aleatoria del día que cambia cada 24 horas
    private fun obtenerPalabraDelDia(): Palabra? {
        // Obtener preferencias guardadas del usuario
        val prefs = requireContext().getSharedPreferences("palabra_dia", 0)

        // Leer fecha y ID de la palabra guardada anteriormente
        val fechaGuardada = prefs.getString("fecha", "")
        val idGuardado = prefs.getInt("id", -1)

        // Obtener fecha actual formateada (ej: 20250402)
        val fechaActual = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        var palabra: Palabra? = null

        // Verificar si ya existe una palabra para hoy (misma fecha y ID válido)
        if (fechaActual == fechaGuardada && idGuardado != -1) {
            // Si ya hay palabra para hoy, recuperarla de la base de datos por su ID
            val resultado = db.rawQuery(
                "SELECT * FROM palabra WHERE id = ?",
                arrayOf(idGuardado.toString())
            )
            resultado.use {
                if (it.moveToFirst()) {
                    palabra = mapearPalabra(it)
                }
            }
        } else {
            // Si es un nuevo día o es la primera vez, buscar una palabra aleatoria
            val resultado = db.rawQuery(
                "SELECT * FROM palabra ORDER BY RANDOM() LIMIT 1",
                null
            )
            resultado.use {
                if (it.moveToFirst()) {
                    val p = mapearPalabra(it)
                    palabra = p
                    // Guardar la nueva palabra y fecha actual en preferencias
                    prefs.edit {
                        putString("fecha", fechaActual)
                        putInt("id", p.id)
                    }
                }
            }
        }
        return palabra
    }

    private fun obtenerNombreAudio() {
        if (palabraDelDiaId == -1) return
        val resultado = db.rawQuery(
            "SELECT audio FROM palabra WHERE id = ?",
            arrayOf(palabraDelDiaId.toString())
        )
        resultado.use {
            if (it.moveToFirst()) {
                reproducirAudio(it.getString(0))
            }
        }
    }

    @SuppressLint("DiscouragedApi") //metodo admitido para este tipo de solución al proyecto
    private fun reproducirAudio(nombre: String) {
        val idAudio = resources.getIdentifier(nombre, "raw", requireContext().packageName)
        if (idAudio != 0) {
            val mp = android.media.MediaPlayer.create(requireContext(), idAudio)
            mp.start()
            mp.setOnCompletionListener { it.release() }
        }
    }

    private fun navegarADetallePalabra() {
        val resultado = db.rawQuery(
            "SELECT * FROM palabra WHERE id = ?",
            arrayOf(palabraDelDiaId.toString())
        )
        resultado.use {
            if (it.moveToFirst()) {
                val palabra = mapearPalabra(it)
                val fragment = DetallePalabraFragment().apply {
                    arguments = Bundle().apply {
                        putString("palabra_nahuat", capitalizar(palabra.nahuat))
                        putString("palabra_significado", capitalizar(palabra.espanol))
                        putString("categoria", capitalizar(palabra.categoria))
                        putString("imagen", palabra.imagen)
                        putString("audio", palabra.audio)
                        putInt("palabra_id", palabra.id)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.slide_out,
                        R.anim.pop_in,
                        R.anim.pop_out
                    )
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun mapearPalabra(resultado: android.database.Cursor): Palabra {
        return Palabra(
            id = resultado.getInt(0),
            espanol = resultado.getString(1),
            nahuat = resultado.getString(2),
            categoria = resultado.getString(3),
            imagen = resultado.getString(4),
            audio = resultado.getString(5),
            favorito = resultado.getInt(6),
            aprendida = resultado.getInt(7)
        )
    }

    private fun capitalizar(texto: String): String {
        return texto.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    private fun configurarCategoriaClick(view: View, cardId: Int, categoria: String) {
        view.findViewById<CardView>(cardId).setOnClickListener {
            val fragment = CategoriaFragment().apply {
                arguments = Bundle().apply { putString("categoria", categoria) }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.pop_in,
                    R.anim.pop_out
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::db.isInitialized && db.isOpen) db.close()
    }
}