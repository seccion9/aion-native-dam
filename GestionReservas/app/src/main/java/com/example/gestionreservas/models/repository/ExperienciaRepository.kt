package com.example.gestionreservas.models.repository

import android.util.Log
import com.example.gestionreservas.models.entity.ExperienciaCompleta
import com.example.gestionreservas.network.ApiServiceFake


class ExperienciaRepository(private val api: ApiServiceFake) {
    suspend fun obtenerExperienciasApi(token: String): List<ExperienciaCompleta>? {
        return try {
            val response = api.obtenerExperiencias(token)
            if (response.isSuccessful) {
                Log.e("RESPUESTA API EXP","${response.body()}")
                response.body()

            } else {
                Log.e("API_EXPERIENCIAS", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_EXPERIENCIAS", "Excepción: ${e.message}")
            null
        }
    }
}
