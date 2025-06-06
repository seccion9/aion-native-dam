package com.example.gestionreservas.models.entity

import java.io.Serializable

data class Pago(
    var id: String,
    var amount: Double,
    var method: String,
    var tipo:String?,
    var estado:String?
):Serializable
