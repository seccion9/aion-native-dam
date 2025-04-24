package com.example.gestionreservas.network

import com.example.gestionreservas.models.entity.Experiencia
import com.example.gestionreservas.models.entity.ExperienciaConHorarios
import com.example.gestionreservas.models.entity.LoginRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    //Conexion con cabecera de la api para que nos devuelva el token del body de la cabecera
    @POST("api/sanctum/token")
    fun login(@Body request: LoginRequest): Call<String>

    @GET("api/getExperiences")
    fun obtenerExperiencias(
        @Header("Authorization") token: String
    ): Call<List<Experiencia>>

    @GET("api/getExperiencesByIdsAndDate")
    fun obtenerReservas(
        @Header("Authorization") token: String,
        @Query("ids[]") ids: List<Int>,
        @Query("date") fecha: String
    ): Call<List<ExperienciaConHorarios>>
}