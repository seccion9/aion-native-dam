package com.example.gestionreservas.models.entity

import java.util.UUID

data class SesionMailing(
    val id: String = UUID.randomUUID().toString(),
    val fecha: String,
    val monitor: String,
    val puntuacion: Float,
    val email: String,
    val jugadores: List<Jugador>,
    val fotosBase64: List<String>
)
