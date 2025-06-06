package com.example.gestionreservas.models.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.network.ApiServiceFake
import com.example.gestionreservas.network.RetrofitFakeInstance
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class CompraRepository(private val api: ApiServiceFake) {

    /**
     * Modifica los datos de una compra con el token y la compra haciendo un patch en la API.
     */
    suspend fun modificarCompra(token: String, compra: Compra): Boolean {
        val response = api.patchCompra(token, compra.id, compra)
        if (response.isSuccessful) {
            return true
        } else {
            throw Exception("Error modificando compra: ${response.code()} - ${response.message()}")
        }
    }
    suspend fun obtenerComentariosUsuario(token: String): List<Comentario>? {
        return try {
            val response = api.obtenerComentarios(token)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("API_COMENTARIOS", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_COMENTARIOS", "Excepción: ${e.message}")
            null
        }
    }

    suspend fun registrarComentarioAPI(token:String,comentario: Comentario):Boolean{
        Log.e("TOKEN_COMENTARIO", "Token usado: $token")

        val response=api.registrarComentario(token,comentario)
        if(response.isSuccessful){
            return true
        }else {
            throw Exception("Error registrando comentario: ${response.code()} - ${response.message()}")
        }
    }
    /**
     * Obtiene las compras del dia(sesiones) a través del token de shared preferences y el dia seleccionado.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerSesionesDelDia(token: String, fecha: LocalDate): List<SesionConCompra> {
        val compras = api.getPurchases("Bearer $token")

        return transformarComprasASesiones(compras, fecha)
    }


    /**
     * Llama a la api para eliminar compra de esta con el token y el id de compra,devolvemos true o falso
     * dependiendo de si es exitosa o no.
     */
    suspend fun eliminarCompra(token:String, idCompra:String): Boolean {
        val response = api.eliminarCompra(token, idCompra)
        Log.d("DEBUG_BORRADO", "token:$token")
        return if (response.isSuccessful) {
            true
        } else {
            Log.e("BORRAR COMPRA", "Error al borrar compra ${response.message()}")
            false
        }
    }


    /**Esta funcion nos devolvera una lista de todas las sesiones de la semana que se mostraran en
    *nuestro recyclerview
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


    /**
     * Transforma las compras en sesiones para mostrarlas en la app recorriendo la compra y sus items,
     * con ello creamos un objeto sesión y lo añadimos a la lista,despues devolvemos esta lista.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun transformarComprasASesiones(compras: List<Compra>, fechaSeleccionada: LocalDate): List<SesionConCompra> {
        val sesiones = mutableListOf<SesionConCompra>()
        Log.d("SESIONES", "Fecha seleccionada: $fechaSeleccionada")

        compras.forEach { compra ->
            compra.items
                .filter { item ->
                    val fechaItem = LocalDate.parse(item.start.substring(0, 10))
                    val confirmada = item.status.equals("confirmada", ignoreCase = true)
                    val fechaCoincide = fechaItem == fechaSeleccionada
                    confirmada && fechaCoincide
                }
                .forEach { item ->
                    val sesion = Sesion(
                        hora = item.start.substring(11, 16),
                        calendario = item.idCalendario,
                        nombre = compra.name,
                        participantes = item.peopleNumber,
                        totalPagado = item.priceTotal,
                        estado = item.status,
                        idiomas = compra.language
                    )
                    sesiones.add(SesionConCompra(sesion, compra))
                    Log.d("SESIONES", "Añadida sesión confirmada de ${compra.name} a las ${item.start}")
                }
        }

        return sesiones
    }

    fun transformarComprasASesionesTodas(compras: List<Compra>): List<SesionConCompra> {
        val sesiones = mutableListOf<SesionConCompra>()

        compras.forEach { compra ->
            compra.items.forEach { item ->
                val sesion = Sesion(
                    hora = item.start.substring(11, 16),
                    calendario = item.idCalendario,
                    nombre = compra.name,
                    participantes = item.peopleNumber,
                    totalPagado = item.priceTotal,
                    estado = item.status,
                    idiomas = compra.language
                )
                sesiones.add(SesionConCompra(sesion, compra))
                Log.d("SESIONES", "Añadida sesión ${item.status} de ${compra.name} a las ${item.start}")
            }
        }

        return sesiones
    }


    /**
     * Obtiene todas las compras de la API con el token guardado en shared preferences.
     */
    suspend fun obtenerCompras(token: String): List<Compra> {
            return api.getPurchases("Bearer $token")

    }


    /**
     * Registra en la API un objeto compra con el token guardado en shared preferences y devuelve
     * la respuesta.
     */
    suspend fun registrarCompra(token: String, compra: Compra): Response<Compra> {
        return api.registrarCompra("Bearer $token", compra)
    }


}