package com.example.gestionreservas.models.repository

import com.example.gestionreservas.models.entity.LoginRequest
import com.example.gestionreservas.network.ApiService
import retrofit2.Call

class AuthRepository (private val apiService: ApiService){
    fun login(email: String, password: String): Call<String> {
        val request = LoginRequest(email, password)
        return apiService.login(request)
    }

}