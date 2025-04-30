package com.example.gestionreservas.models.entity

import java.io.Serializable

data class Compra(
    val id: String,
    val uuid: String,
    val status: String,
    val mailStatus: String,
    val internaPermanent: Boolean,
    val idDiscount: String?,
    val idBono: String?,
    val priceFinal: Double,
    val priceAfterDiscount: Double,
    val priceFractioned: Double,
    val isFractioned: Boolean,
    val name: String,
    val mail: String,
    val dni: String,
    val phone: String,
    val direction: String,
    val language: String,
    val ip: String,
    val comment: String,
    val automaticActions: String,
    val items: List<ItemReserva>,
    val payments: List<Pago>
):Serializable
