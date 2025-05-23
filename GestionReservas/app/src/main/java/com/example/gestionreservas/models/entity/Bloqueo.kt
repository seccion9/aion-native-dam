package com.example.gestionreservas.models.entity

data class Bloqueo(
    val id: String,
    val calendarioId: String,
    val fecha: String,
    val motivo: String? = null
)
