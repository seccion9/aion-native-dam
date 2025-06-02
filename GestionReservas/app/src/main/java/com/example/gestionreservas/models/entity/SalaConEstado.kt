package com.example.gestionreservas.models.entity

data class SalaConEstado(
    val idSala: String,
    var estado: EstadoSala,
    var reserva: Compra? = null
)
