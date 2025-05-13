package com.example.gestionreservas.repository

import android.util.Base64
import android.util.Log
import com.example.gestionreservas.model.CorreoItem
import com.example.gestionreservas.models.entity.GmailMessageDetailResponse
import com.example.gestionreservas.models.entity.GmailMessagesResponse
import com.example.gestionreservas.models.entity.TokenResponse
import com.example.gestionreservas.network.GmailRetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL

object MailingRepository {

    private const val CLIENT_ID = ""
    private const val CLIENT_SECRET = ""

    suspend fun getAccessToken(authCode: String): TokenResponse? = withContext(Dispatchers.IO) {
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
            TokenResponse(
                access_token = json.getString("access_token"),
                refresh_token = json.optString("refresh_token", null),
                expires_in = json.getInt("expires_in"),
                token_type = json.getString("token_type"),
                scope = json.getString("scope")
            )

        } catch (e: Exception) {
            Log.e("MailingRepository", "Error al obtener accessToken: ${e.message}")
            null
        }
    }

    suspend fun getMensajes(token: TokenResponse): GmailMessagesResponse? = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${token.access_token}"
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

    suspend fun getMensajeDetalle(token: TokenResponse, id: String): GmailMessageDetailResponse? = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${token.access_token}"
            val response: Response<GmailMessageDetailResponse> = GmailRetrofitInstance.api.getMessageById(authHeader, id)

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MailingRepository", "Error al obtener detalle: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MailingRepository", "Excepción en detalle: ${e.message}")
            null
        }
    }

    suspend fun obtenerMensajes(token: TokenResponse): MutableList<CorreoItem> {
        val mensajes = getMensajes(token)
        val listaCorreos = mutableListOf<CorreoItem>()

        mensajes?.messages?.take(10)?.forEach { mensaje ->
            val detalle = getMensajeDetalle(token, mensaje.id)

            val cabeceras = detalle?.payload?.headers ?: emptyList()
            val subjectRaw = cabeceras.find { it.name == "Subject" }?.value ?: "(Sin asunto)"
            val asunto = if (subjectRaw.length > 40) subjectRaw.take(30) + "..." else subjectRaw

            val fromRaw = cabeceras.find { it.name == "From" }?.value ?: "(Desconocido)"
            val remitente = if (fromRaw.length > 30) fromRaw.take(20) else fromRaw
            val nombreRemitente = remitente.substringBefore("<").trim()
            val from = nombreRemitente

            val partTextoPlano = detalle?.payload?.parts
                ?.firstOrNull { it.mimeType.equals("text/plain", true) }
                ?.body?.data

            val bodyEncoded = detalle?.payload?.body?.data ?: partTextoPlano

            val preview = bodyEncoded?.let {
                val decoded = it.replace("-", "+").replace("_", "/")
                val cuerpoPlano = String(Base64.decode(decoded, Base64.DEFAULT))
                org.jsoup.Jsoup.parse(cuerpoPlano).text()
                    .replace(Regex("\\[image:.*?\\]", RegexOption.IGNORE_CASE), "")
                    .replace("\n", " ").replace("\r", " ")
                    .take(50) + "..."
            } ?: "(Sin contenido)"

            listaCorreos.add(CorreoItem(mensaje.id, asunto, from, preview))
        }

        return listaCorreos
    }
}
