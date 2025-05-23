package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName

data class MonthlyOccupancyResponse(
    @SerializedName("monthlyOccupancy")
    val monthlyOccupancy: List<Map<String, OcupacionCalendarioSemanal>>? = null
)
