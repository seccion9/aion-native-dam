package com.example.gestionreservas.models.repository

import com.example.gestionreservas.models.entity.Monitor
import com.example.gestionreservas.models.entity.SesionMailing
import com.example.gestionreservas.network.ApiServiceFake

class MailingRepository(private val api: ApiServiceFake) {
    /**
     * Obtiene los monitores de la API para mostrarlos en el spinner de mailing
     */
    suspend fun obtenerMonitores(token:String):List<Monitor>{
        val response=api.obtenerMonitores("Bearer $token")
        if(response.isSuccessful){
            return response.body() ?: emptyList()
        }else{
            throw Exception("Error al obtener monitores: ${response.code()}")
        }
    }

    /**
     * Registra una sesión en la API enviando puntuaciones,fotos,email...El backend deberá mandar
     * al email registrado toda la info de la sesión con sus fotos y puntuaciones
     */
    suspend fun registrarSesion(token: String, sesion: SesionMailing): Boolean {
        val response = api.registrarSesionMailing("Bearer $token", sesion)
        return if (response.isSuccessful) {
            true
        } else {
            throw Exception("Error al registrar sesión: ${response.code()} - ${response.message()}")
        }
    }

}