package com.example.diccionario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class DetallePalabraFragment : Fragment() {

    private var isFavorite = false
    private lateinit var botonFavorito: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_palabra, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recibir datos del bundle
        arguments?.let { bundle ->
            val palabra = bundle.getString("palabra_nahuat", "Ichpikatl") ?: "Ichpikatl"
            val significado = bundle.getString("palabra_significado", "Niña") ?: "Niña"
            val categoria = bundle.getString("categoria", "Gente") ?: "Gente"

            view.findViewById<TextView>(R.id.texto_palabra_nahuat).text = palabra
            view.findViewById<TextView>(R.id.texto_significado).text = significado
            view.findViewById<TextView>(R.id.texto_categoria).text = categoria
        }

        // Botón de retroceder
        view.findViewById<View>(R.id.boton_retroceder).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Botón de audio
        view.findViewById<View>(R.id.boton_audio).setOnClickListener {
            // Reproducir audio
        }

        // Botón de favorito con cambio de color
        botonFavorito = view.findViewById(R.id.boton_favorito)
        botonFavorito.setOnClickListener {
            isFavorite = !isFavorite
            actualizarEstadoFavorito()
        }

        // Estado inicial
        isFavorite = false
        actualizarEstadoFavorito()
    }

    private fun actualizarEstadoFavorito() {
        if (isFavorite) {
            // Estado favorito: corazón rojo con fondo rosado
            botonFavorito.setIconResource(R.drawable.ic_favorite_filled)
            botonFavorito.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.favorito_activo)
            botonFavorito.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.favorito_fondo))
            botonFavorito.strokeWidth = 0
        } else {
            // Estado no favorito: corazón gris con fondo blanco
            botonFavorito.setIconResource(R.drawable.ic_favorite_border)
            botonFavorito.iconTint = ContextCompat.getColorStateList(requireContext(), R.color.favorito_borde)
            botonFavorito.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            botonFavorito.strokeWidth = 0
        }
    }
}