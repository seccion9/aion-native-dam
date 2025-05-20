package com.example.gestionreservas.models.repository

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
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
        val compras = api.getPurchases("Bearer $token")
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
    fun transformarComprasASesiones(
        compras: List<Compra>,
        fechaSeleccionada: LocalDate
    ): List<SesionConCompra> {
        val sesiones = mutableListOf<SesionConCompra>()
        Log.d("SESIONES", "Fecha seleccionada: $fechaSeleccionada")
        compras.forEach { compra ->
            compra.items.forEach { item ->
                val fechaItem = LocalDate.parse(item.start.substring(0, 10))
                Log.d("SESIONES", "Evaluando compra de ${compra.name}")
                Log.d("SESIONES", "Fecha del item: $fechaItem vs seleccionada: $fechaSeleccionada")

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
                    Log.d("SESIONES", "Añadida sesión de ${compra.name} a las $fechaItem")
                } else {
                    Log.d("SESIONES", "No coincide la fecha del item con la seleccionada")
                }
            }
        }
        return sesiones
    }
    suspend fun obtenerCompras(token: String): List<Compra> {
            return api.getPurchases("Bearer $token")

    }
    suspend fun registrarCompra(token: String, compra: Compra): Response<Compra> {
        return api.registrarCompra("Bearer $token", compra)
    }
    suspend fun enviarComentarioACompra(
        token: String,
        listaCompras: List<Compra>,
        lineaSeleccionada: String,
        comentario: String,
        motivo: String
    ): Compra? {
        val partes = lineaSeleccionada.split("|")
        val fecha = partes[0]
        val nombre = partes[1]
        val calendario = partes[2]
        val id = partes[3]

        var compraEncontrada: Compra? = null

        listaCompras.forEach { compra ->
            if (compra.id == id && compra.name == nombre) {
                compra.items.forEach { item ->
                    val horaItem = item.start.substring(11, 16)
                    if (horaItem == fecha && item.idCalendario == calendario) {
                        compraEncontrada = compra
                    }
                }
            }
        }

        compraEncontrada?.let {
            it.comment = "$motivo : $comentario"
            val exito = modificarCompra(token, it)
            return if (exito) it else null
        }

        return null
    }


}