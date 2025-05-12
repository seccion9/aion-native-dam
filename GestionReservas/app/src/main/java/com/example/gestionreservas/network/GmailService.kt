package com.example.gestionreservas.network

import com.example.gestionreservas.models.entity.GmailMessagesResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GmailService {
    @GET("gmail/v1/users/me/messages")
    suspend fun getMessages(
        @Header("Authorization") authHeader: String
    ): Response<GmailMessagesResponse>

}