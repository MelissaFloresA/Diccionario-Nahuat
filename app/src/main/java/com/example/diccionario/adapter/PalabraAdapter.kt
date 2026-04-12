package com.example.diccionario.adapter

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.diccionario.R
import com.example.diccionario.modelo.Palabra
import com.example.diccionario.ui.DetallePalabraFragment
import com.google.android.material.button.MaterialButton

/**
 * Adaptador para el RecyclerView que muestra la lista de palabras.
 * Maneja la visualización, reproducción de audio, favoritos y navegación al detalle.
 */
class PalabraAdapter(
    private var lista: MutableList<Palabra>,
    private val context: Context,
    private val db: SQLiteDatabase,
    private val mostrarFavorito: Boolean
) : RecyclerView.Adapter<PalabraAdapter.ViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: View = view // Referencia a toda la card para el click listener
        val img: ImageView = view.findViewById(R.id.imgPalabra)
        val txtNahuat: TextView = view.findViewById(R.id.txtNahuat)
        val txtEspanol: TextView = view.findViewById(R.id.txtEspanol)
        val btnAudio: MaterialButton = view.findViewById(R.id.btnAudio)
        val btnFav: ImageButton = view.findViewById(R.id.btnFavorito)
    }


    //Crea nuevas vistas para el RecyclerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_palabra, parent, false)
        return ViewHolder(view)
    }

    //Contador de palabras por categoria
    override fun getItemCount(): Int = lista.size

    //Vincula los datos de una palabra específica con su ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val palabra = lista[position]

        // Configuración de textos con formato de mayuscula
        holder.txtNahuat.text = capitalizarPrimeraLetra(palabra.nahuat)
        holder.txtEspanol.text = capitalizarPrimeraLetra(palabra.espanol)

        // Carga la imagen desde recursos drawable usando el nombre en náhuat
        cargarImagen(holder.img, palabra.nahuat)

        // Configuramos el botón de audio para reproducir audio
        holder.btnAudio.setOnClickListener {
            reproducirAudio(palabra.audio)
        }

        // Control de visibilidad del botón favorito según el fragment
        if (mostrarFavorito) {
            holder.btnFav.visibility = View.VISIBLE
        } else {
            holder.btnFav.visibility = View.GONE
        }

        // Actualiza el ícono del corazón (favorito o no)
        actualizarIconoFavorito(holder, palabra)

        //  listener para marcar/desmarcar favorito
        if (mostrarFavorito) {
            holder.btnFav.setOnClickListener {
                toggleFavorito(palabra, holder, position)
            }
        }

        // Navegación al detalle de la palabra al hacer clic en toda la card
        holder.cardView.setOnClickListener {
            navegarADetalle(palabra)
        }
    }

    //Funcion de capitalización
    private fun capitalizarPrimeraLetra(texto: String): String {
        if (texto.isEmpty()) return texto
        return texto[0].uppercase() + texto.substring(1).lowercase()
    }

    //Funcion para cargar png (formato img_nombre_nahuat)
    @Suppress("DiscouragedApi")
    private fun cargarImagen(imageView: ImageView, nombreNahuat: String) {
        // Construir el nombre de la imagen: img_nahuat
        val nombreImagen = "img_${nombreNahuat.lowercase()}"

        // Obtener el ID del recurso drawable por su nombre
        val idImagen = context.resources.getIdentifier(
            nombreImagen,
            "drawable",
            context.packageName
        )

        if (idImagen != 0) {
            imageView.setImageResource(idImagen)
        } else {
            // Fallback: imagen por defecto si no se encuentra el recurso
            imageView.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    //Función para reproducir audio desde la carpeta raw
    @Suppress("DiscouragedApi")
    private fun reproducirAudio(nombreAudio: String) {
        try {
            mediaPlayer?.release()

            // Obtiene el ID del recurso raw por su nombre
            val idAudio = context.resources.getIdentifier(
                nombreAudio,
                "raw",
                context.packageName
            )

            if (idAudio != 0) {
                // Crea y reproduce el audio
                mediaPlayer = MediaPlayer.create(context, idAudio)
                mediaPlayer?.start()
                // Libera recursos al finalizar
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Actulización de vista de favoritos segun db
    private fun actualizarIconoFavorito(holder: ViewHolder, palabra: Palabra) {
        if (palabra.favorito == 1) {
            holder.btnFav.setImageResource(R.drawable.ic_favorite_filled)
            holder.btnFav.setColorFilter(ContextCompat.getColor(context, R.color.favorito_activo))
        } else {
            holder.btnFav.setImageResource(R.drawable.ic_favorite_border)
            holder.btnFav.setColorFilter(ContextCompat.getColor(context, R.color.text_secundario))
        }
    }

    //Alterna el estado de favorito de una palabra
    private fun toggleFavorito(palabra: Palabra, holder: ViewHolder, position: Int) {
        // Invierte el estado actual (1 -> 0, 0 -> 1)
        val nuevoValor = if (palabra.favorito == 1) 0 else 1
        palabra.favorito = nuevoValor

        // Prepara los valores para actualizar en la base de datos
        val values = ContentValues().apply {
            put("favorito", nuevoValor)
        }

        // Actualiza en la tabla 'palabra'
        db.update(
            "palabra",
            values,
            "id = ?",
            arrayOf(palabra.id.toString())
        )

        // Actualiza la interfaz visual
        actualizarIconoFavorito(holder, palabra)
        notifyItemChanged(position)
    }

    //Navega al fragmento de detalle de la palabra

    private fun navegarADetalle(palabra: Palabra) {
        val detalleFragment = DetallePalabraFragment().apply {
            arguments = Bundle().apply {
                // Datos textuales formateados con capitalización
                putString("palabra_nahuat", capitalizarPrimeraLetra(palabra.nahuat))
                putString("palabra_significado", capitalizarPrimeraLetra(palabra.espanol))
                putString("categoria", capitalizarPrimeraLetra(palabra.categoria))
                // Datos de recursos (imagen con formato img_nahuat)
                putString("imagen", "img_${palabra.nahuat.lowercase()}")
                putString("audio", palabra.audio)
                // Identificador único
                putInt("palabra_id", palabra.id)
                // Estado de aprendizaje
                putInt("aprendida", palabra.aprendida)
            }
        }

        // Realiza la transacción de navegación
        (context as? FragmentActivity)?.supportFragmentManager?.beginTransaction()
            ?.setCustomAnimations(
                R.anim.slide_in,
                R.anim.slide_out,
                R.anim.pop_in,
                R.anim.pop_out
            )
            ?.replace(R.id.fragment_container, detalleFragment)
            ?.addToBackStack(null) // Permite volver atrás con el botón de retroceso
            ?.commit()
    }

    //Libera recursos del MediaPlayer cuando el Recyclerview se destruye
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}