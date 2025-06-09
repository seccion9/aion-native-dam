package com.example.gestionreservas.models.repository

import android.util.Log
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.network.ApiServiceFake

class ComentariosRepository(private val api: ApiServiceFake) {

    suspend fun obtenerComentariosApi(token: String): List<Comentario> {
        return try {
            val response = api.obtenerComentarios("Bearer $token")
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("ComentariosRepository", "Error HTTP ${response.code()} al obtener comentarios")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ComentariosRepository", "Excepción al obtener comentarios: ${e.message}")
            emptyList()
        }
    }

}