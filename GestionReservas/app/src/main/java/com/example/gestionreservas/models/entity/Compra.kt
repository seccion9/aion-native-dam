package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Compra(
    var id: String,
    var uuid: String,
    var status: String,
    var mailStatus: String,
    var internaPermanent: Boolean,
    var idDiscount: String?,
    var idBono: String?,
    var priceFinal: Double,
    var priceAfterDiscount: Double,
    var priceFractioned: Double,
    var isFractioned: Boolean,
    var name: String,
    var mail: String,
    var dni: String,
    var phone: String,
    var direction: String,
    var language: String,
    var ip: String,
    var comment: String,
    var automaticActions: String,
    var items: List<ItemReserva>,
    var payments: List<Pago>,
    @SerializedName("resumenItems")
    var resumenItems: List<ResumenItem>?=null
):Serializable
