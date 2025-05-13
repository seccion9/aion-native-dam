package com.example.gestionreservas.models.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.network.ApiServiceFake
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class CompraRepository(private val api: ApiServiceFake) {
    suspend fun modificarCompra(token: String, compra: Compra): Boolean {
        val response = api.patchCompra(token, compra.id, compra)
        if (response.isSuccessful) {
            return true
        } else {
            throw Exception("Error modificando compra: ${response.code()} - ${response.message()}")
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerSesionesDelDia(token: String, fecha: LocalDate): List<SesionConCompra> {
        val compras = api.getPurchases(token)
        return transformarComprasASesiones(compras, fecha)
    }

    /*Esta funcion nos devolvera una lista de todas las sesiones de la semana que se mostraran en
    nuestro recyclerview
    */
    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerSesionesDeSemana(compras: List<Compra>, fechaActual: LocalDate): List<SesionConCompra> {
        val sesiones = mutableListOf<SesionConCompra>()
        val lunes = fechaActual.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val domingo = fechaActual.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        for (compra in compras) {
            for (item in compra.items) {
                val fechaItem = LocalDate.parse(item.start.substring(0, 10))
                if (!fechaItem.isBefore(lunes) && !fechaItem.isAfter(domingo)) {
                    val sesion = Sesion(
                        hora = item.start.substring(11, 16),
                        calendario = item.idCalendario,
                        nombre = compra.name,
                        participantes = item.peopleNumber,
                        totalPagado = item.priceTotal,
                        estado = compra.status,
                        idiomas = compra.language
                    )
                    sesiones.add(SesionConCompra(sesion, compra))
                }
            }
        }
        return sesiones
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun transformarComprasASesiones(
        compras: List<Compra>,
        fechaSeleccionada: LocalDate
    ): List<SesionConCompra> {
        val sesiones = mutableListOf<SesionConCompra>()
        compras.forEach { compra ->
            compra.items.forEach { item ->
                val fechaItem = LocalDate.parse(item.start.substring(0, 10))
                if (fechaItem == fechaSeleccionada) {
                    val sesion = Sesion(
                        hora = item.start.substring(11, 16),
                        calendario = item.idCalendario,
                        nombre = compra.name,
                        participantes = item.peopleNumber,
                        totalPagado = item.priceTotal,
                        estado = compra.status,
                        idiomas = compra.language
                    )
                    sesiones.add(SesionConCompra(sesion, compra))
                }
            }
        }
        return sesiones
    }
    suspend fun obtenerCompras(token: String): List<Compra> {
        return api.getPurchases(token)
    }
    suspend fun registrarCompra(token: String, compra: Compra): Response<Compra> {
        return api.registrarCompra("Bearer $token", compra)
    }
}