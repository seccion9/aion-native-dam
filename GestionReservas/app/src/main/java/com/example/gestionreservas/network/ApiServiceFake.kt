package com.example.gestionreservas.network

import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.LoginRequest
import com.example.gestionreservas.models.entity.LoginResponse
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.entity.OcupacionCalendarioSemanal
import com.example.gestionreservas.models.entity.PagoCajaChica
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServiceFake {
    //Nos autenticamos con email y contraseña y nos devuelve el token para usar en los endpoints
    @POST("sanctum/token")
    suspend fun autenticacion(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    //Obtiene las compras totales y devuelve una lista de ellas
    @GET("purchases")
    suspend fun getPurchases(
        @Header("Authorization") token: String
    ): List<Compra>
    @GET("purchases")
    suspend fun getPurchasesV2(
        @Header("Authorization") token: String
    ): Response<List<Compra>>

    //Obtiene la ocupacion por ids de experiencias y fechas y devuelve un mapa del dia y los datos de ese dia
    @GET("getMonthlyOccupancyByExperienceIdsAndDates")
    suspend fun getMonthlyOccupancy(
        @Header("Authorization") token: String,
        @Query("ids[]") ids: List<Int>,
        @Query("date_start") fechaInicio: String,
        @Query("date_end") fechaFin: String
    ): Map<String, OcupacionCalendarioSemanal>

    //Recupera la ocupacion entre fechas y devuelve una lista de ocupacion
    @GET("escaperadar/getOccupancyBetweenDates")
    suspend fun obtenerReservasDia(
        @Header("Authorization") token:String,
        @Query("date_start") fechaInicio:String,
        @Query("date_end") fechaFin:String
    ):List<Ocupacion>

    //Obtiene compra y devuelve la compra con las modificaciones
    @PATCH("purchases/{id}")
    suspend fun patchCompra(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body compra: Compra
    ): Response<Compra>

    //Obtener los pagos del dia de la caja chica
    @GET("paymentsCajaChicaDia/{fecha}")
    suspend fun getPagosCajaDia(
        @Header("Authorization") token: String,
        @Path("fecha") fecha: String
    ): Response<List<PagoCajaChica>>
}