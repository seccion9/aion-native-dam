package com.example.gestionreservas.models.repository

import android.os.Build
import android.util.Log
import android.widget.Toast
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

    suspend fun bloquearFechas(token: String, bloqueo: Bloqueo): Boolean {
        return try {
            val response = api.crearBloqueo(token, bloqueo)
            if (response.isSuccessful) {
                Log.d("BLOQUEAR_FECHA", "Bloqueo creado con éxito: ${response.body()}")
                true
            } else {
                Log.e("BLOQUEAR_FECHA", "Error al crear bloqueo: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("BLOQUEAR_FECHA", "Excepción: ${e.message}")
            false
        }
    }

    suspend fun obtenerBloqueos(token: String): List<Bloqueo>? {
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
