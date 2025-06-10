package com.example.gestionreservas.network

import com.example.gestionreservas.models.entity.Bloqueo
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ExperienciaCompleta
import com.example.gestionreservas.models.entity.LoginRequest
import com.example.gestionreservas.models.entity.LoginResponse
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.entity.OcupacionCalendarioSemanal
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.PagoCajaChica
import com.example.gestionreservas.models.entity.PagoReserva
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    /**
     * Llamadas a endpoint caja chica
     */

    //Obtener los pagos del dia de la caja chica
    @GET("paymentsCajaChicaDia/{fecha}")
    suspend fun getPagosCajaDia(
        @Header("Authorization") token: String,
        @Path("fecha") fecha: String
    ): Response<List<PagoCaja>>

    //Obtener los pagos totales de la caja chica
    @GET("paymentsCajaChicaDia")
    suspend fun getPagosCaja(
        @Header("Authorization") token:String
    ):Response<List<PagoCajaChica>>

    //Obtener los pagos totales de la caja chica
    @GET("paymentsCajaChicaDia")
    suspend fun getPagosCajaV2(
        @Header("Authorization") token:String
    ):Response<List<PagoCaja>>

    //Guardar pagos manuales caja chica
    @POST("paymentsCajaChica")
    suspend fun registrarPagoCajaChica(
        @Header("Authorization") token:String,
        @Body pagoCaja: PagoCaja
    ):Response<PagoCaja>

    //Editar pago de caja
    @PATCH("paymentsCajaChica/{id}")
    suspend fun editarPagoCajaChica(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body pagoCaja: PagoCaja
    ): Response<PagoCaja>

    //Eliminar pago de la api
    @DELETE("paymentsCajaChica/{id}")
    suspend fun eliminarPago(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>




    //Registrar compra en la API
    @POST("purchases")
    suspend fun registrarCompra(
        @Header("Authorization") token:String,
        @Body compra:Compra
    ):Response<Compra>

    /**
     * Llamadas a endpoints de comentarios
     */

    //Registra un comentario
    @POST("comentarios")
    suspend fun registrarComentario(
        @Header("Authorization") token:String,
        @Body comentario: Comentario
    ):Response<Comentario>
    //Obtiene todos los comentarios
    @GET("comentarios")
    suspend fun obtenerComentarios(
        @Header("Authorization") token: String
    ): Response<List<Comentario>>

    //Borra un comentario
    @DELETE("comentarios/{id}")
    suspend fun eliminarComentario(
        @Header("Authorization") token:String,
        @Path("id")id:String
    ):Response<Unit>

    //Edita comentarios
    @PATCH("comentarios/{id}")
    suspend fun editarComentario(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body comentario: Comentario
    ): Response<Unit>

    /**
     * Llamadas a endpoints bloqueos
     */
    //Obtener bloqueos de API.
    @GET("bloqueos")
    suspend fun obtenerBloqueos(
        @Header("Authorization") token:String,
    ):Response<List<Bloqueo>>

    //Crea bloqueo de días en la API
    @POST("bloqueos")
    suspend fun crearBloqueo(
        @Header("Authorization") token:String,
        @Body bloqueo: Bloqueo
    ):Response<Bloqueo>

    //Obtener bloqueos por fecha
    @GET("bloqueos")
    suspend fun obtenerBloqueosPorFecha(
        @Header("Authorization") token: String,
        @Query("fecha") fecha: String
    ): Response<List<Bloqueo>>




    //Borra compras de la API
    @DELETE("purchases/{id}")
    suspend fun eliminarCompra(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    //Obtiene las experiencias de la API
    @GET("getExperiencesByIds")
    suspend fun obtenerExperiencias(
        @Header("Authorization") token:String
    ):Response<List<ExperienciaCompleta>>
}