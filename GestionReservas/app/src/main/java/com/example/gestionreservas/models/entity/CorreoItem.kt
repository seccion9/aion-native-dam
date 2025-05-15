package com.example.gestionreservas.model

import java.io.Serializable

data class CorreoItem(
    val id: String,
    val asunto: String,
    val remitente: String,
    val cuerpoPreview: String
):Serializable

