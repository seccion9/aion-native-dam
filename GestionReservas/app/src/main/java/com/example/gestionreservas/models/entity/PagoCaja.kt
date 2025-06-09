package com.example.gestionreservas.models.entity

import java.io.Serializable

data class PagoCaja (
    var id: String? = null,
    var fecha:String,
    var concepto:String,
    var cantidad:String,
    var tipo:String,
    var parcial:String?
):Serializable