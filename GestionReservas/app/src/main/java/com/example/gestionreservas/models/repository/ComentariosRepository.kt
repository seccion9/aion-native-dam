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
    suspend fun eliminarComentario(token: String, comentario: Comentario): Boolean {
        return try {
            val response = api.eliminarComentario("Bearer $token", comentario.id)
            response.isSuccessful

        } catch (e: Exception) {

            Log.e("ComentariosRepository", "Excepción al borrar comentario: ${e.message}")
            false
        }
    }
    suspend fun editarComentario(token: String, comentario: Comentario): Boolean {
        return try {
            val response = api.editarComentario("Bearer $token", comentario.id, comentario)
            if (response.isSuccessful) {
                Log.d("ComentariosRepository", "Comentario editado correctamente")
                true
            } else {
                Log.e("ComentariosRepository", "Error al editar: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("ComentariosRepository", "Excepción al editar comentario: ${e.message}")
            false
        }
    }


}