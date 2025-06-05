package com.example.gestionreservas.models.entity

import java.io.Serializable

data class Field(
    val id: String,
    val title: String,
    var name: String,
    val value: String,
    val amount: Double
):Serializable
