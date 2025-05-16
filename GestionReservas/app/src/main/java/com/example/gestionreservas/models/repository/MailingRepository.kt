package com.example.gestionreservas.repository

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.gestionreservas.BuildConfig
import com.example.gestionreservas.R
import com.example.gestionreservas.model.CorreoItem
import com.example.gestionreservas.models.entity.GmailMessageDetailResponse
import com.example.gestionreservas.models.entity.GmailMessagesResponse
import com.example.gestionreservas.models.entity.TokenResponse
import com.example.gestionreservas.network.GmailRetrofitInstance
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MailingRepository {
    /**
     * ID de cliente y clave secreta generados en Google Cloud Console para OAuth 2.0.
     */
    private const val CLIENT_ID = BuildConfig.GMAIL_CLIENT_ID
    private const val CLIENT_SECRET = BuildConfig.GMAIL_CLIENT_SECRET

    /**
     * Funcion para obtener el token de acceso al gmail e iniciar sesion,conectamos con la url de la API
     * usamos nuestro codigo de autenticacion,nuestros clienteId y clave de cliente secreta(configurada en Auth 2.0),
     * Intentamos hacer la conexion con POST y devolvemos el tokenResponse que devuelve la API
     */
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

    /**
     * Con nuestro token response obtenemos los mensajes de ese usuario de GMAIL,si es exitoso devolvemos
     * el body de la respuesta.
     */
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

    /**
     * Obtiene el detalle de cada mensaje con el token y el id del mensaje y si es exitoso responde con el
     * body del mensaje(asunto,remitente,cuerpo)
     */
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

    /**
     * Con el body que devuelve getMessages depuramos la informacion del correo en una lista de tipo
     * CorreoItem que luego cargaremos en nustro recyclerView.
     * Usamos async para realizar llamadas independientes a la API y ser mas eficientes y awwaitall
     * para esperar a todas las llamadas antes de mostrar los mensajes
     */
    suspend fun obtenerMensajes(token: TokenResponse): MutableList<CorreoItem> {
        val mensajes = getMensajes(token)
        // Ejecutamos las peticiones a detalle en paralelo con async
        val listaCorreos = withContext(Dispatchers.IO) {
            mensajes?.messages?.take(10)?.map { mensaje ->
                async {
                    val detalle = getMensajeDetalle(token, mensaje.id)
                    /**
                     * Limpiamos cabeceras de los mensajes para mostrar la info que queremos y si es mucho texto
                     * nos quedamos solo con una parte a mostrar en el recycler.
                     */
                    val fechaEnvio = detalle?.internalDate
                        ?.toLongOrNull()
                        ?.let { obtenerFechaLegible(it) }
                        ?: "Sin fecha"
                    val cabeceras = detalle?.payload?.headers ?: emptyList()
                    val subjectRaw = cabeceras.find { it.name == "Subject" }?.value ?: "(Sin asunto)"
                    val asunto = if (subjectRaw.length > 21) subjectRaw.take(20) + "..." else subjectRaw
                    val fromRaw = cabeceras.find { it.name == "From" }?.value ?: "(Desconocido)"
                    val remitente = if (fromRaw.length > 21) fromRaw.take(20) else fromRaw
                    val nombreRemitente = remitente.substringBefore("<").trim()
                    val from = nombreRemitente

                    val partTextoPlano = detalle?.payload?.parts
                        ?.firstOrNull { it.mimeType.equals("text/plain", true) }
                        ?.body?.data

                    val bodyEncoded = detalle?.payload?.body?.data ?: partTextoPlano

                    /**
                     * Decodificar mensaje de como nos lo da google a nuestro formato y limpieza del mensaje.
                     */
                    val preview = bodyEncoded?.let {
                        val decoded = it.replace("-", "+").replace("_", "/")
                        val cuerpoPlano = String(Base64.decode(decoded, Base64.DEFAULT))
                        org.jsoup.Jsoup.parse(cuerpoPlano).text()
                            .replace(Regex("\\[image:.*?\\]", RegexOption.IGNORE_CASE), "")
                            .replace("\n", " ").replace("\r", " ")
                            .take(35) + "..."
                    } ?: "(Sin contenido)"

                    CorreoItem(mensaje.id, asunto, from, preview,fechaEnvio)
                }
            }?.awaitAll() ?: emptyList()
        }

        return listaCorreos.toMutableList()
    }

    /**
     *  Obtiene el token desde get preferences e itenta iniciar sesion en gmail con nuestro codigo
     *  de identificacion
     */
    suspend fun loginYObtenerCorreos(authCode: String, context: Context): List<CorreoItem> {
        val tokenResponse = getAccessToken(authCode) ?: throw Exception("Token nulo")

        val prefs = context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("access_token", tokenResponse.access_token)
            .putString("refresh_token", tokenResponse.refresh_token.toString())
            .apply()

        return obtenerMensajes(tokenResponse)
    }

    /**
     * Funcion identica a obtenerMensajes pero con paginacion para que nos vaya devolviendo los mensajes
     * segun hacemos scroll en el recycler view
     * Usamos async para realizar llamadas independientes a la API y ser mas eficientes y awwaitall
     * para esperar a todas las llamadas antes de mostrar los mensajes
     */
    suspend fun obtenerMensajesPagina(token: TokenResponse, pageToken: String? = null): Pair<List<CorreoItem>, String?> {
        return withContext(Dispatchers.IO) {
            val authHeader = "Bearer ${token.access_token}"
            val response = GmailRetrofitInstance.api.getMessages(authHeader, pageToken)

            if (response.isSuccessful) {
                val body = response.body()

                val lista = body?.messages?.map { mensaje ->
                    async {
                        val detalle = getMensajeDetalle(token, mensaje.id)

                        val cabeceras = detalle?.payload?.headers ?: emptyList()
                        val subjectRaw = cabeceras.find { it.name == "Subject" }?.value ?: "(Sin asunto)"
                        val asunto = if (subjectRaw.length > 21) subjectRaw.take(20) + "..." else subjectRaw
                        val fechaEnvio = detalle?.internalDate
                            ?.toLongOrNull()
                            ?.let { obtenerFechaLegible(it) }
                            ?: "Sin fecha"
                        val fromRaw = cabeceras.find { it.name == "From" }?.value ?: "(Desconocido)"
                        val remitente = if (fromRaw.length > 21) fromRaw.take(20) else fromRaw
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
                                .take(35) + "..."
                        } ?: "(Sin contenido)"

                        CorreoItem(mensaje.id, asunto, from, preview,fechaEnvio)
                    }
                }?.awaitAll() ?: emptyList()

                Pair(lista, body?.nextPageToken)
            } else {
                throw Exception("Error en paginación: ${response.code()}")
            }
        }
    }
     fun depurarMensajeParaObtenerDetalles(mensaje: GmailMessageDetailResponse?):CorreoItem{
         Log.e("fecha mensaje","${mensaje?.internalDate}")
         val fechaEnvio = mensaje?.internalDate
             ?.toLongOrNull()
             ?.let { obtenerFechaLegible(it) }
             ?: "Sin fecha"

         val cabeceras = mensaje?.payload?.headers ?: emptyList()
         val asunto = cabeceras.find { it.name == "Subject" }?.value ?: "(Sin asunto)"

         val remitente = cabeceras.find { it.name == "From" }?.value ?: "(Desconocido)"

         val partTextoPlano = mensaje?.payload?.parts
             ?.firstOrNull { it.mimeType.equals("text/plain", true) }
             ?.body?.data

         val bodyEncoded = mensaje?.payload?.body?.data ?: partTextoPlano

         val preview = bodyEncoded?.let {
             val decoded = it.replace("-", "+").replace("_", "/")
             val cuerpoPlano = String(Base64.decode(decoded, Base64.DEFAULT))
             org.jsoup.Jsoup.parse(cuerpoPlano).wholeText()
                 .replace(Regex("\\[image:.*?\\]", RegexOption.IGNORE_CASE), "")
         } ?: "(Sin contenido)"

         return CorreoItem(mensaje?.id ?: "", asunto, remitente, preview,fechaEnvio)
    }
    fun obtenerEmailUsuario(context: Context): String? {
        val cuenta = GoogleSignIn.getLastSignedInAccount(context)
        return cuenta?.email
    }
    fun cerrarSesion(context: Context, onComplete: (() -> Unit)? = null) {
        val googleSignInClient = GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
                .requestServerAuthCode(context.getString(R.string.default_web_client_id), true)
                .build()
        )

        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient.revokeAccess().addOnCompleteListener {
                Log.d("MailingRepository", "Cuenta desconectada y permisos revocados")

                // Borrar tokens
                context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

                onComplete?.invoke()
            }
        }
    }
    fun obtenerFechaLegible(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}