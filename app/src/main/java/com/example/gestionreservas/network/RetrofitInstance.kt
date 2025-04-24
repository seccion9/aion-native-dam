package com.example.gestionreservas.network


import android.content.Context
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    //Instanciamos retrofit y le indicamos la direccion de nuestra API
    private const val BASE_URL = "https://test.gestorempresas.es/"
    val gson = GsonBuilder().setLenient().create()
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}