package com.example.gestionreservas.models.entity

data class Bloqueo(
    val id: String,
    val salas: List<String>,
    val tipo: String,
    val inicio: String,
    val fin: String,
    val motivo: String? = null
)
