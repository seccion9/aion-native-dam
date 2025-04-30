package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName

data class Calendario(
    @SerializedName("name")
    val nombre: String,

    @SerializedName("color")
    val color: String?,

    @SerializedName("schedules")
    val horarios: List<Horario>
)
