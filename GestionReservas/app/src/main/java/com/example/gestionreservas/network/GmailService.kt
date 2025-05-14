package com.example.gestionreservas.network

import com.example.gestionreservas.models.entity.GmailMessageDetailResponse
import com.example.gestionreservas.models.entity.GmailMessagesResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GmailService {
    @GET("gmail/v1/users/me/messages")
    suspend fun getMessages(
        @Header("Authorization") authHeader: String,
        @Query("pageToken") pageToken: String? = null
    ): Response<GmailMessagesResponse>

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessageById(
        @Header("Authorization") authHeader: String,
        @Path("id") messageId: String
    ): Response<GmailMessageDetailResponse>
}