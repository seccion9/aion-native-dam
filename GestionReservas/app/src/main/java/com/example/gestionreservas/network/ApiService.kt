package com.example.gestionreservas.network

import com.example.gestionreservas.models.entity.Experiencia
import com.example.gestionreservas.models.entity.ExperienciaConHorarios
import com.example.gestionreservas.models.entity.LoginRequest
import com.example.gestionreservas.models.entity.OcupacionCalendarioSemanal
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    //Conexion con cabecera de la api para que nos devuelva el token del body de la cabecera
    @POST("api/sanctum/token")
    suspend fun login(@Body request: LoginRequest): Response<String>

    @GET("api/getExperiences")
    fun obtenerExperiencias(
        @Header("Authorization") token: String
    ): Call<List<Experiencia>>

    @GET("api/getExperiencesByIdsAndDate")
    suspend fun obtenerReservas(
        @Header("Authorization") token: String,
        @Query("ids") ids: List<Int>,
        @Query("date") fecha: String
    ): Response <List <ExperienciaConHorarios>>

    @GET("api/getMonthlyOccupancyByExperienceIdsAndDates")
    fun obtenerCalendarioSemanal(
        @Header("Authorization") token:String,
        @Query("ids[]") ids: List<Int>,
        @Query("date_start") fechaInicio:String,
        @Query("date_end") fechaFin:String
    ):Call<Map<String, OcupacionCalendarioSemanal>>

    @GET("/api/escaperadar/getOccupancyBetweenDates")
    fun obtenerListadoReservas(
        @Header("Authorization")token:String,
        @Query("date_start") fechaInicio:String,
        @Query("date_end") fechaFin:String
    ):Call<String>

    @GET("/api/getPurchaseById")
    fun obtenerDatosCompraId(
        @Header("Authorization")token:String,
        @Query("id") uuid:String
    ):Call<String>
}