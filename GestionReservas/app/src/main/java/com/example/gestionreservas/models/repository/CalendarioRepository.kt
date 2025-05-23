package com.example.gestionreservas.models.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gestionreservas.models.entity.Bloqueo
import com.example.gestionreservas.models.entity.DiaSemana
import com.example.gestionreservas.models.entity.HoraReserva
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
    /*
     Transforma una lista de ocupaciones en una lista de HoraReserva.
     Se agrupan las reservas por horas y en cada hora se ve si hay reserva en cada sala (cal1-2),
     si la hay se marca como `false` y si no `true`.
     Se transforma a HoraReserva y se ordenan las horas.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun transformarOcupacionesAHoraReserva(ocupaciones: List<Ocupacion>): List<HoraReserva> {
        return ocupaciones
            .groupBy { it.start.substring(0, 5) to it.end.substring(0, 5) }
            .map { (horas, lista) ->
                val (ini, fin) = horas

                val hayCal1 = lista.any { it.calendarioId == "cal1" }
                val hayCal2 = lista.any { it.calendarioId == "cal2" }

                HoraReserva(
                    horaInicio = ini,
                    horaFin = fin,
                    sala1Libre = !hayCal1,
                    sala2Libre = !hayCal2
                )
            }
            .sortedBy { it.horaInicio }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerOcupacionSemanalFake(
        token: String,
        ids: List<Int>,
        fechaInicio: String,
        fechaFin: String
    ): Map<String, OcupacionCalendarioSemanal> {
        val resp = api.getMonthlyOccupancy(token, ids, fechaInicio, fechaFin)
        Log.e("OCUPACION",resp.toString())
        // Si el backend siempre envía un único mapa:
        val mapa = resp

        Log.d("REPO_OCUPACION", "Claves recibidas: ${mapa.keys}")   // <- ahora sí verás fechas
        return mapa
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun transformarMapaADiasSemana(
        mapa: Map<String, OcupacionCalendarioSemanal>?,
        fechaLunes: LocalDate
    ): ArrayList<DiaSemana> {

        val lista   = arrayListOf<DiaSemana>()
        val isoFmt  = DateTimeFormatter.ISO_LOCAL_DATE          // 2025-05-19
        val slashFmt= DateTimeFormatter.ofPattern("dd/MM/yyyy") // 19/05/2025

        for (i in 0..6) {
            val fecha = fechaLunes.plusDays(i.toLong())

            // ① buscamos con ISO
            var datos = mapa?.get(fecha.format(isoFmt))

            // ② si no hay, probamos con dd/MM/yyyy
            if (datos == null) datos = mapa?.get(fecha.format(slashFmt))

            val nombreDia = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            val reservas  = datos?.ocupadas?.toString() ?: "0"
            val sesiones  = datos?.sesiones?.toString() ?: "0"

            lista.add(DiaSemana(fecha, reservas, sesiones, nombreDia))
        }
        return lista
    }



}
