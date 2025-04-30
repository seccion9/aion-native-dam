package com.example.gestionreservas.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFakeInstance {
    //Cambiar IP segun la IP que tengamos en nuestro ordenador
    val BASE_URL = "http://192.168.223.22:3000/"


    val apiFake: ApiServiceFake by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceFake::class.java)
    }
}