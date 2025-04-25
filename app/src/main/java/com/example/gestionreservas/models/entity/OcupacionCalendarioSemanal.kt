package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName

data class OcupacionCalendarioSemanal(

    @SerializedName("occupied")
    val ocupadas:Int,

    @SerializedName("total")
    val sesiones:Int,

    @SerializedName("blocked")
    val bloqueadas:Int

)
