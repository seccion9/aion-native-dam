package com.example.gestionreservas.models.entity

data class ItemReserva(
    val id: String,
    val idExperience: String,
    val idCalendario: String,
    val idBusinessUnit: String?,
    val status: String,
    val internaPermanent: Boolean,
    val start: String,
    val end: String,
    val duration: Int,
    val peopleNumber: Int,
    val priceOriginal: Double,
    val priceTotal: Double,
    val priceFractioned: Double,
    val discountAmount: Int,
    val fields: List<Any>
)
