package com.example.gestionreservas.models.entity
//Data class para hacer login con nuestra API con usuario y contraseña
data class LoginRequest(
    val email: String,
    val password: String
)
