package com.example.gestionreservas.models.entity

data class Jugador (
    val id: String,
    val nombre:String,
    val imagen:String,
    val puntuaciones:List<Float>
)