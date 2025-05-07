package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResumenItem(
    val id: String,
    val id_experience: String,
    val id_calendario: String,
    val start: String,
    val end: String,
    val people_number: Int,
    val price_original: Double,
    val price_fractioned: Double
):Serializable
