package com.example.gestionreservas.models.entity

import android.os.Parcelable
import java.io.Serializable

data class Sesion(
    val hora: String,
    val calendario: String,
    val nombre: String,
    val participantes: Int,
    val totalPagado: Double,
    val estado: String,
    val idiomas: String
):Serializable
