package com.example.diccionario.modelo

data class Palabra(
    val id: Int,
    val espanol: String,
    val nahuat: String,
    val categoria: String,
    val imagen: String,
    val audio: String,
    var favorito: Int
)