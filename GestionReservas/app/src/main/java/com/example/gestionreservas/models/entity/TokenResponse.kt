package com.example.gestionreservas.models.entity

data class TokenResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String?,
    val scope: String,
    val token_type: String
)
