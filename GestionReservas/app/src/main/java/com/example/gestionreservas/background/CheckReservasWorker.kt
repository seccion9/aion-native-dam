package com.example.gestionreservas.background

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gestionreservas.R
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.activities.ReservasActivity

class CheckReservasWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val token = getTokenFromSharedPreferences()
            val response = RetrofitFakeInstance.apiFake.getPurchasesV2(token.toString())

            if (response.isSuccessful) {
                val comprasActuales = response.body() ?: emptyList()
                val idsActuales = comprasActuales.map { it.id }.toSet()

                val prefs =
                    applicationContext.getSharedPreferences("prefs_reservas", Context.MODE_PRIVATE)
                val idsPrevios = prefs.getStringSet("ids_compras", emptySet()) ?: emptySet()

                Log.d("CheckReservasWorker", "IDs actuales: $idsActuales")
                Log.d("CheckReservasWorker", "IDs previos: $idsPrevios")

                // Primera ejecución: guarda y no notifica
                if (idsPrevios.isEmpty()) {
                    Log.d("CheckReservasWorker", "Primera ejecución: guardando IDs sin notificar.")
                    prefs.edit().putStringSet("ids_compras", idsActuales).apply()
                    return Result.success()
                }

                val nuevos = idsActuales.subtract(idsPrevios)
                Log.d("CheckReservasWorker", "Nuevos detectados: $nuevos")

                if (nuevos.isNotEmpty()) {
                    lanzarNotificacion(
                        "¡Nuevas reservas!",
                        "Hay ${nuevos.size} nuevas compras registradas."
                    )
                }

                // Actualizar IDs
                prefs.edit().putStringSet("ids_compras", idsActuales).apply()
            } else {
                Log.e("CheckReservasWorker", "Error en compras: ${response.code()} - ${response.message()}")
            }

        } catch (e: Exception) {
            Log.e("CheckReservasWorker", "Error al buscar nuevas reservas: ${e.message}")
            return Result.retry()
        }

        return Result.success()
    }

    private fun lanzarNotificacion(titulo: String, mensaje: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_reservas",
                "Reservas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevas reservas"
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }

        // Este intent abrirá la actividad principal
        val intent = Intent(applicationContext, ReservasActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, "canal_reservas")
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("CheckReservasWorker", "Permiso POST_NOTIFICATIONS no concedido.")
                return
            }
            Log.d("CheckReservasWorker", "Lanzando notificación con título: $titulo y mensaje: $mensaje")
            notify(456, builder.build())
            Log.d("CheckReservasWorker", "Notificación enviada con éxito.")
        }
    }



    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            applicationContext.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }
}
