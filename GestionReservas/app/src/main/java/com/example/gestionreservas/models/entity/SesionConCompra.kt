package com.example.gestionreservas.models.entity

import java.io.Serializable

data class SesionConCompra(
    val sesion: Sesion,
    val compra: Compra?
):Serializable
