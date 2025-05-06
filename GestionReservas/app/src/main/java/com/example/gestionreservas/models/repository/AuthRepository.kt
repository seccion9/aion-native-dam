package com.example.gestionreservas.models.repository

import com.example.gestionreservas.models.entity.LoginRequest
import com.example.gestionreservas.network.ApiService
import retrofit2.Call
import retrofit2.Response

class AuthRepository (private val apiService: ApiService){
    suspend fun login(email: String, password: String): Response<String> {
        val request = LoginRequest(email, password)
        return apiService.login(request)
    }

}