package com.example.gestionreservas.models.entity

data class PagoReserva(
    var id:String,
    var fecha:String,
    var concepto:String,
    var cantidad:Double,
    var tipo:String,
    val reserva: Compra? = null,
    var metodo:String?,
    var estado:String?
)
