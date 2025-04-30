package com.example.gestionreservas.models.entity

import java.io.Serializable

data class HoraReserva(
    val horaInicio: String,
    val horaFin: String,
    val sala1Libre: Boolean?,
    val sala2Libre: Boolean?
):Serializable
