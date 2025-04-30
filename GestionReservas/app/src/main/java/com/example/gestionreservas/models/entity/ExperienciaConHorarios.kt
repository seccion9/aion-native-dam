package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName

data class ExperienciaConHorarios(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val nombre: String,

    @SerializedName("description")
    val descripcion: String?,

    @SerializedName("calendars")
    val calendarios: List<Calendario>?
)
