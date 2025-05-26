package com.example.gestionreservas.TestApiServer

import com.example.gestionreservas.models.entity.LoginRequest
import com.example.gestionreservas.network.ApiServiceFake
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServiceFakeTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiServiceFake

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/api/")) // muy importante el path
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceFake::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun login_success() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"token":"fake-token-abc123"}""")
        mockWebServer.enqueue(mockResponse)

        val response = apiService.autenticacion(LoginRequest("test@correo.com", "1234"))

        assertTrue(response.isSuccessful)
        assertEquals("fake-token-abc123", response.body()?.token)
    }

    @Test
    fun login_failure() = runBlocking {
        val mockResponse = MockResponse()
            .setResponseCode(401)
            .setBody("""{"error":"Unauthorized"}""")
        mockWebServer.enqueue(mockResponse)

        val response = apiService.autenticacion(LoginRequest("bad@correo.com", "wrongpass"))

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())
    }
}
