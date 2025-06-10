package com.example.gestionreservas.models.entity

import com.example.gestionreservas.models.enums.EstadoSala

data class SalaConEstado(
    val idSala: String,
    var estado: EstadoSala,
    var reserva: Compra? = null
)
