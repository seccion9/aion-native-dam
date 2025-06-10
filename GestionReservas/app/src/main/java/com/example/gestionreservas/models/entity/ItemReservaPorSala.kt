package com.example.gestionreservas.models.entity

import com.example.gestionreservas.models.enums.EstadoSala

data class ItemReservaPorSala(
    val estado: EstadoSala,
    val compra: Compra? = null,
    val cantidadSalas: Int = 0
)
