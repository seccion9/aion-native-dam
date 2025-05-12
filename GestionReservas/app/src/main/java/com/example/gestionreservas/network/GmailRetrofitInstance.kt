package com.example.gestionreservas.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GmailRetrofitInstance {
    val api: GmailService by lazy {
        Retrofit.Builder()
            .baseUrl("https://gmail.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GmailService::class.java)
    }
}