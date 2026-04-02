package com.example.diccionario.ui

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.diccionario.R
import com.example.diccionario.db.DBHelper
import com.google.android.material.button.MaterialButton

class DetallePalabraFragment : Fragment() {

    private var palabraId: Int = -1
    private var isFavorite = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var db: SQLiteDatabase
    private lateinit var botonFavorito: MaterialButton
    private lateinit var botonAudio: MaterialButton
    private lateinit var imagenPrincipal: ImageView
    private lateinit var textoPalabraNahuat: TextView
    private lateinit var textoSignificado: TextView
    private lateinit var textoCategoria: TextView

    // Variables para almacenar los datos recibidos
    private var palabraNahuat: String = ""
    private var palabraSignificado: String = ""
    private var palabraCategoria: String = ""
    private var palabraImagen: String = ""
    private var palabraAudio: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_palabra, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar DB
        val helper = DBHelper(requireContext())
        db = helper.abrirBase()

        // Inicializar vistas
        imagenPrincipal = view.findViewById(R.id.imagen_principal)
        textoPalabraNahuat = view.findViewById(R.id.texto_palabra_nahuat)
        textoSignificado = view.findViewById(R.id.texto_significado)
        textoCategoria = view.findViewById(R.id.texto_categoria)
        botonFavorito = view.findViewById(R.id.boton_favorito)
        botonAudio = view.findViewById(R.id.boton_audio)

        // Botón retroceder
        view.findViewById<View>(R.id.boton_retroceder).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Cargar datos
        cargarDatos()

        // Configurar botón de audio
        botonAudio.setOnClickListener {
            reproducirAudio()
        }

        // Configurar botón de favorito
        botonFavorito.setOnClickListener {
            toggleFavorito()
        }
    }

    private fun cargarDatos() {
        arguments?.let { bundle ->
            palabraNahuat = bundle.getString("palabra_nahuat", "")
            palabraSignificado = bundle.getString("palabra_significado", "")
            palabraCategoria = bundle.getString("categoria", "")
            palabraImagen = bundle.getString("imagen", "")
            palabraAudio = bundle.getString("audio", "")
            palabraId = bundle.getInt("palabra_id", -1)

            // Mostrar datos en las vistas
            textoPalabraNahuat.text = palabraNahuat
            textoSignificado.text = palabraSignificado
            textoCategoria.text = palabraCategoria

            // Cargar imagen
            if (palabraImagen.isNotEmpty()) {
                val idImagen = resources.getIdentifier(palabraImagen, "drawable", requireContext().packageName)
                if (idImagen != 0) {
                    imagenPrincipal.setImageResource(idImagen)
                }
            }

            // Si no tenemos el ID, buscarlo en la base de datos
            if (palabraId == -1 && palabraNahuat.isNotEmpty()) {
                buscarEnDatabase(palabraNahuat)
            } else {
                // Si tenemos el ID, buscar el estado favorito
                buscarEstadoFavoritoPorId()
            }
        }
    }

    private fun buscarEnDatabase(palabraNahuat: String) {
        val resultado = db.rawQuery(
            "SELECT id, favorito, audio, imagen FROM palabra WHERE nahuat = ?",
            arrayOf(palabraNahuat)
        )

        resultado.use {
            if (it.moveToFirst()) {
                palabraId = it.getInt(0)
                isFavorite = it.getInt(1) == 1

                // Si no se pasó audio, usar el de la DB
                if (palabraAudio.isEmpty()) {
                    palabraAudio = it.getString(2)
                }

                // Si no se pasó imagen, usar el de la DB
                if (palabraImagen.isEmpty()) {
                    palabraImagen = it.getString(3)
                    val idImagen = resources.getIdentifier(palabraImagen, "drawable", requireContext().packageName)
                    if (idImagen != 0) {
                        imagenPrincipal.setImageResource(idImagen)
                    }
                }
            }
        }

        actualizarEstadoFavorito()
    }

    private fun buscarEstadoFavoritoPorId() {
        if (palabraId != -1) {
            val cursor = db.rawQuery(
                "SELECT favorito FROM palabra WHERE id = ?",
                arrayOf(palabraId.toString())
            )

            cursor.use {
                if (it.moveToFirst()) {
                    isFavorite = it.getInt(0) == 1
                }
            }
        }

        actualizarEstadoFavorito()
    }

    private fun reproducirAudio() {
        if (palabraAudio.isEmpty()) return

        try {
            mediaPlayer?.release()

            val idAudio = resources.getIdentifier(
                palabraAudio,
                "raw",
                requireContext().packageName
            )

            if (idAudio != 0) {
                mediaPlayer = MediaPlayer.create(requireContext(), idAudio)
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleFavorito() {
        isFavorite = !isFavorite

        val values = ContentValues().apply {
            put("favorito", if (isFavorite) 1 else 0)
        }

        db.update(
            "palabra",
            values,
            "id = ?",
            arrayOf(palabraId.toString())
        )

        actualizarEstadoFavorito()
    }

    private fun actualizarEstadoFavorito() {
        if (isFavorite) {
            botonFavorito.setIconResource(R.drawable.ic_favorite_filled)
            botonFavorito.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.favorito_activo)
            botonFavorito.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.favorito_fondo))
        } else {
            botonFavorito.setIconResource(R.drawable.ic_favorite_border)
            botonFavorito.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.text_secundario)
            botonFavorito.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
    }
}