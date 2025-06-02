package com.example.gestionreservas.models.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gestionreservas.models.entity.Bloqueo
import com.example.gestionreservas.models.entity.DiaSemana
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.entity.OcupacionCalendarioSemanal
import com.example.gestionreservas.network.ApiServiceFake
import com.example.gestionreservas.network.RetrofitFakeInstance
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarioRepository(private val api: ApiServiceFake) {

    /*
     Llama a nuestra instancia de retrofit para obtener de la API las ocupaciones de hoy
     y las filtra por fecha exacta.
     */
    suspend fun obtenerOcupacionesDelDia(token: String, fechaStr: String): List<Ocupacion> {
        val lista = api.obtenerReservasDia(token, fechaStr, fechaStr)
        return lista.filter { it.date == fechaStr }
    }
    suspend fun bloquearFechas(token: String, bloqueos: List<Bloqueo>): Boolean{
        return try {
            bloqueos.forEach { bloqueo ->
                val response = api.crearBloqueo("Bearer $token", bloqueo)
                if (!response.isSuccessful) {
                    Log.e("CalendarioRepo", "Error al bloquear ${bloqueo.fecha} → ${response.code()}")
                }
            }
            true
        } catch (e: Exception) {
            Log.e("CalendarioRepo", "Excepción bloqueando fechas: ${e.message}")
            false
        }
    }

    suspend fun obtenerBloqueos(token:String): List<Bloqueo>? {
        return try {
            val resp = api.obtenerBloqueos(token)
            if (resp.isSuccessful) {
                resp.body() ?: emptyList()
            } else {
                Log.e("OBTENER_BLOQUEOS", "Error HTTP: ${resp.code()} – ${resp.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("OBTENER_BLOQUEOS", "Excepción: ${e.message}")
            emptyList()
        }
    }


}
