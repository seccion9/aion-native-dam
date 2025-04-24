package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName

data class Horario(
    @SerializedName("start_session")
    val horaInicio: String,

    @SerializedName("end_session")
    val horaFin: String,

    @SerializedName("isFree")
    val estaLibre: Boolean = true
)
