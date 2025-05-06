package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName

data class Ocupacion(
    @SerializedName("experience_id") val experienciaId: String,
    @SerializedName("calendar_id")   val calendarioId: String,
    val date: String,
    val start: String,
    val end: String,
    @SerializedName("id_purchase")   val idCompra: String
)
