package com.example.gestionreservas.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.gestionreservas.network.GmailRetrofitInstance
import com.example.gestionreservas.models.entity.GmailMessagesResponse
import retrofit2.Response
import kotlin.io.encoding.ExperimentalEncodingApi
import android.util.Base64


object MailingRepository {

    private const val CLIENT_ID = ""
    private const val CLIENT_SECRET = ""

    /**
     * Intercambia el authorization code (authCode) por un access_token OAuth
     * para poder acceder a los servicios de Google.
     */
    suspend fun getAccessToken(authCode: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://oauth2.googleapis.com/token")

            val params = listOf(
                "code" to authCode,
                "client_id" to CLIENT_ID,
                "client_secret" to CLIENT_SECRET,
                "redirect_uri" to "",
                "grant_type" to "authorization_code"
            ).joinToString("&") { "${it.first}=${it.second}" }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            conn.outputStream.use { it.write(params.toByteArray()) }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            json.getString("access_token")

        } catch (e: Exception) {
            Log.e("MailingRepository", "Error al obtener accessToken: ${e.message}")
            null
        }
    }

    /**
     * Llama a la Gmail API para obtener la lista de mensajes .
     * Se requiere un access_token válido con el scope gmail.readonly.
     */
    suspend fun getMensajes(token: String): GmailMessagesResponse? = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer $token"
            val response = GmailRetrofitInstance.api.getMessages(authHeader)

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MailingRepository", "Error en getMessages: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MailingRepository", "Excepción en getMessages: ${e.message}")
            null
        }
    }


}
