package com.example.gestionreservas.models.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExperienciaCompleta (
    val id: Int,
    val minPlayers: Int,
    val maxPlayers: Int,
    val duration: Int,
    val TTC: Int,
    val phone: String?,
    val mail: String?,
    val groupType: String,
    val type: String,
    val name: String,
    val image: String,
    val description: String,
    val prices: List<Any>,
    val calendars: CalendarsWrapper
) : Serializable

data class CalendarsWrapper(
    @SerializedName("0")
    val sala0: CalendarData? = null,
    @SerializedName("1")
    val sala1: CalendarData? = null,
    val fields: List<FieldData> = emptyList()
)

data class CalendarData(
    val name: String,
    val icon: String,
    val color: String,
    val schedules_by_dow: List<ScheduleByDay>,
    val schedules_by_date: List<Any>
)

data class ScheduleByDay(
    val day_of_week_iso: Int,
    val schedules: List<HorarioSesion>
)

data class HorarioSesion(
    val start_session: String,
    val end_session: String
)

data class FieldData(
    val title: String,
    val name: String,
    val value: String
)
