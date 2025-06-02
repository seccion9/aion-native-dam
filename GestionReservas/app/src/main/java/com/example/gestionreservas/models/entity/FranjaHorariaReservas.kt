package com.example.gestionreservas.models.entity

data class FranjaHorariaReservas(
    val horaInicio: String,
    val horaFin: String,
    var reservas: List<Compra> = emptyList(),
    var bloqueada: Boolean = false
)
