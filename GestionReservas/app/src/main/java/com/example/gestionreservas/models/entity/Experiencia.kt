package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName

data class Experiencia(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val nombre: String,

    @SerializedName("description")
    val descripcion: String
)
