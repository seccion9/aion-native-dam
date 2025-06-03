package com.example.gestionreservas.models.entity

data class FranjaHorariaReservas(
    val horaInicio: String,
    val horaFin: String,
    var salas: List<SalaConEstado> = emptyList()
)
