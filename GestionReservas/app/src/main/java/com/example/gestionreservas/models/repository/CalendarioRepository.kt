package com.example.gestionreservas.models.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gestionreservas.models.entity.DiaSemana
import com.example.gestionreservas.models.entity.HoraReserva
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.entity.OcupacionCalendarioSemanal
import com.example.gestionreservas.network.ApiServiceFake
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
        return api.getMonthlyOccupancy(token, ids, fechaInicio, fechaFin)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun transformarMapaADiasSemana(
        mapa: Map<String, OcupacionCalendarioSemanal>?,
        fechaLunes: LocalDate
    ): ArrayList<DiaSemana> {
        val lista = arrayListOf<DiaSemana>()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        for (i in 0..6) {
            val fecha = fechaLunes.plusDays(i.toLong())
            val clave = fecha.format(formatter)
            val datos = mapa?.get(clave)
            val nombreDia = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))

            val reservas = datos?.ocupadas?.toString() ?: "0"
            val sesiones = datos?.sesiones?.toString() ?: "0"

            lista.add(DiaSemana(fecha, reservas, sesiones, nombreDia))
        }

        return lista
    }


}
