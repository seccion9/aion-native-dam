package com.example.gestionreservas.network

import com.example.gestionreservas.models.entity.Compra
import retrofit2.http.GET

interface ApiServiceFake {
    @GET("purchases")
    suspend fun getPurchases(): List<Compra>
}