package com.example.gestionreservas.models.entity

data class ItemReservaPorSala(
    val estado: EstadoSala,
    val compra: Compra? = null,
    val cantidadSalas: Int = 0
)
