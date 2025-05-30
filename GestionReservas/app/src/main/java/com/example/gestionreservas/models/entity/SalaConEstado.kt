package com.example.gestionreservas.models.entity

data class SalaConEstado(
    val idSala: String,
    val estado: EstadoSala,
    val reservas: List<Compra>?=null
)
