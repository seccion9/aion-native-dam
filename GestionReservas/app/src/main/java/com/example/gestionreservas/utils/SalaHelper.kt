package com.example.gestionreservas.utils

object SalaHelper {
    fun obtenerCalendarioDeExperiencia(idExperience: String): String {
        return when {
            idExperience.endsWith("1") -> "cal1"
            idExperience.endsWith("2") -> "cal2"
            else -> "calDesconocido"
        }
    }
}