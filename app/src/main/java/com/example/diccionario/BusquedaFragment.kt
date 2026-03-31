package com.example.diccionario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView

class BusquedaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_busqueda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el clic en la card de resultado
        val cardResultado = view.findViewById<CardView>(R.id.card_resultado)

        cardResultado.setOnClickListener {
            // Obtener los datos de la card
            val palabra = view.findViewById<TextView>(R.id.texto_palabra).text.toString()
            val significado = view.findViewById<TextView>(R.id.texto_significado).text.toString()

            // Crear el fragment de detalle con los datos
            val detalleFragment = DetallePalabraFragment()
            val bundle = Bundle().apply {
                putString("palabra_nahuat", palabra)
                putString("palabra_significado", significado)
                // Opcional: también puedes pasar la categoría, imagen, etc.
                putString("categoria", "Alimentos")
            }
            detalleFragment.arguments = bundle

            // Navegar al fragment de detalle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detalleFragment)
                .addToBackStack(null) // Permite volver atrás con el botón de retroceso
                .commit()
        }

        // Configurar el botón de audio (opcional)
        val botonAudio = view.findViewById<View>(R.id.boton_audio)
        botonAudio.setOnClickListener {
            // Aquí iría la lógica para reproducir audio
            // Por ahora solo mostramos un mensaje o puedes dejarlo vacío
        }

        // Configurar el botón de retroceder del header
        val botonRetroceder = view.findViewById<View>(R.id.boton_retroceder)
        botonRetroceder.setOnClickListener {
            // Volver al fragment anterior
            parentFragmentManager.popBackStack()
        }
    }
}