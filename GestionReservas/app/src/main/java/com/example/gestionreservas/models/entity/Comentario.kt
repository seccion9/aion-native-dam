package com.example.gestionreservas.models.entity

import java.io.Serializable

data class Comentario(
    var id:String,
    var fecha:String,
    var descripcion:String,
    var tipo:String,
    var nombreUsuario:String
):Serializable
