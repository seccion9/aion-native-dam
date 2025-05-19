package com.example.gestionreservas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gestionreservas.models.entity.CheckReservasWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("CheckReservasWorker", "Worker ejecutado - ${System.currentTimeMillis()}")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
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

                Log.d("BootReceiver", "Worker de reservas reprogramado tras reinicio.")
            }
        }
    }
}
