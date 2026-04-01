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

class PalabraAdapter(
    private var lista: MutableList<Palabra>,
    private val context: Context,
    private val db: SQLiteDatabase,
    private val mostrarFavorito: Boolean
) : RecyclerView.Adapter<PalabraAdapter.ViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: View = view // La card completa
        val img: ImageView = view.findViewById(R.id.imgPalabra)
        val txtNahuat: TextView = view.findViewById(R.id.txtNahuat)
        val txtEspanol: TextView = view.findViewById(R.id.txtEspanol)
        val btnAudio: MaterialButton = view.findViewById(R.id.btnAudio)
        val btnFav: ImageButton = view.findViewById(R.id.btnFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_palabra, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val palabra = lista[position]

        // Configurar textos con primera letra mayúscula
        holder.txtNahuat.text = capitalizarPrimeraLetra(palabra.nahuat)
        holder.txtEspanol.text = capitalizarPrimeraLetra(palabra.espanol)

        // Configurar imagen
        cargarImagen(holder.img, palabra.imagen)

        // Configurar botón de audio
        holder.btnAudio.setOnClickListener {
            reproducirAudio(palabra.audio)
        }

        // Configurar visibilidad del botón favorito
        if (mostrarFavorito) {
            holder.btnFav.visibility = View.VISIBLE
        } else {
            holder.btnFav.visibility = View.GONE
        }

        // Configurar estado del favorito
        actualizarIconoFavorito(holder, palabra)

        // Configurar click del favorito
        if (mostrarFavorito) {
            holder.btnFav.setOnClickListener {
                toggleFavorito(palabra, holder, position)
            }
        }

        // Configurar clic en toda la card para ir al detalle
        holder.cardView.setOnClickListener {
            navegarADetalle(palabra)
        }
    }

    // ✅ Función para capitalizar la primera letra (versión corregida)
    private fun capitalizarPrimeraLetra(texto: String): String {
        if (texto.isEmpty()) return texto
        return texto.substring(0, 1).uppercase() + texto.substring(1).lowercase()
    }

    private fun cargarImagen(imageView: ImageView, nombreImagen: String) {
        val idImagen = context.resources.getIdentifier(
            nombreImagen,
            "drawable",
            context.packageName
        )

        if (idImagen != 0) {
            imageView.setImageResource(idImagen)
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    private fun reproducirAudio(nombreAudio: String) {
        try {
            mediaPlayer?.release()

            val idAudio = context.resources.getIdentifier(
                nombreAudio,
                "raw",
                context.packageName
            )

            if (idAudio != 0) {
                mediaPlayer = MediaPlayer.create(context, idAudio)
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

    private fun actualizarIconoFavorito(holder: ViewHolder, palabra: Palabra) {
        if (palabra.favorito == 1) {
            holder.btnFav.setImageResource(R.drawable.ic_favorite_filled)
            holder.btnFav.setColorFilter(ContextCompat.getColor(context, R.color.favorito_activo))
        } else {
            holder.btnFav.setImageResource(R.drawable.ic_favorite_border)
            holder.btnFav.setColorFilter(ContextCompat.getColor(context, R.color.text_secundario))
        }
    }

    private fun toggleFavorito(palabra: Palabra, holder: ViewHolder, position: Int) {
        val nuevoValor = if (palabra.favorito == 1) 0 else 1
        palabra.favorito = nuevoValor

        val values = ContentValues().apply {
            put("favorito", nuevoValor)
        }

        db.update(
            "palabra",
            values,
            "id = ?",
            arrayOf(palabra.id.toString())
        )

        actualizarIconoFavorito(holder, palabra)
        notifyItemChanged(position)
    }

    private fun navegarADetalle(palabra: Palabra) {
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

        // Obtener la actividad y navegar
        (context as? FragmentActivity)?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, detalleFragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    fun actualizarLista(nuevaLista: MutableList<Palabra>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}