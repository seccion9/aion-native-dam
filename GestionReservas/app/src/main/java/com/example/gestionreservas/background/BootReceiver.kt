package com.example.gestionreservas.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "ACTION RECIBIDA: ${intent?.action}")

        Log.d("CheckReservasWorker", "BootReceiver recibió acción: ${intent?.action}")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent?.action == Intent.ACTION_USER_PRESENT ||
            intent?.action == "com.example.LANZAR_WORKER" // <-- ESTA LÍNEA FALTABA
        ) {
            val prefs = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            val notificacionesActivas = prefs.getBoolean("notificaciones_activadas", false)

            if (notificacionesActivas) {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()

                val periodicWork = PeriodicWorkRequestBuilder<CheckReservasWorker>(
                    15, TimeUnit.MINUTES
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "CheckReservasWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWork
                )

                // AÑADIR EJECUCIÓN INMEDIATA para comprobar sin esperar
                val oneTime = androidx.work.OneTimeWorkRequestBuilder<CheckReservasWorker>().build()
                WorkManager.getInstance(context).enqueue(oneTime)

                Log.d("BootReceiver", "Worker reprogramado y lanzado tras acción ${intent.action}")
            } else {
                Log.d("BootReceiver", "Notificaciones desactivadas, no se lanza worker")
            }
        }
    }
}
