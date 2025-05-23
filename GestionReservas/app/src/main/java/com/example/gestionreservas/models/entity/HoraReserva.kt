package com.example.gestionreservas.models.entity

import java.io.Serializable

data class HoraReserva(
    val horaInicio: String,
    val horaFin: String,
    var sala1Libre: Boolean?,
    var sala2Libre: Boolean?,
    var sala1Bloqueada: Boolean? = false,
    var sala2Bloqueada: Boolean? = false
):Serializable
