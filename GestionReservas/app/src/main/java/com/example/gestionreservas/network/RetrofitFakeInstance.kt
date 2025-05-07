package com.example.gestionreservas.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFakeInstance {
    //Cambiar IP segun la IP que tengamos en nuestro ordenador
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // 1️⃣ construimos Retrofit…
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.142:3000/api/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 2️⃣ …y creamos la instancia de la interfaz
    val apiFake: ApiServiceFake by lazy {
        retrofit.create(ApiServiceFake::class.java)
    }

}