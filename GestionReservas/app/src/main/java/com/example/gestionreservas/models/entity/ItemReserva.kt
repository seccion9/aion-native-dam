package com.example.gestionreservas.models.entity

import java.io.Serializable

data class ItemReserva(
    var id: String,
    var idExperience: String,
    var idCalendario: String,
    var idBusinessUnit: String?,
    var status: String,
    var internaPermanent: Boolean,
    var start: String,
    var end: String,
    var duration: Int,
    var peopleNumber: Int,
    var priceOriginal: Double,
    var priceTotal: Double,
    var priceFractioned: Double,
    var discountAmount: Int,
    var fields: List<Field>
):Serializable
