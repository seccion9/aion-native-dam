package com.example.gestionreservas.view.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.ActivityReservasBinding
import com.example.gestionreservas.background.CheckReservasWorker
import com.example.gestionreservas.view.fragment.CalendarioFragmentDiario
import com.example.gestionreservas.view.fragment.ConfiguracionFragment
import com.example.gestionreservas.view.fragment.HomeFragment
import com.example.gestionreservas.view.fragment.ListadoFragment
import com.example.gestionreservas.view.fragment.MailingFragment
import java.util.concurrent.TimeUnit

class ReservasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservasBinding
    private lateinit var toggle: ActionBarDrawerToggle

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        crearCanalNotificacionesSiNoExiste()
        pedirPermisoNotificaciones()
        lanzarWorkerSiNotificacionesActivas()

        //Establecemos el toolbar en la activity
        setSupportActionBar(binding.toolbar)

        // Configuracion de toolbar para abrir o cerrarla en el drawerlayout
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /* Manejo de diferentes selecciones en la activity,cada zona del menu llevara a un fragment
        * diferente para que haya menos consumo de datos y la logica de cerrar sesion al pulsarlo
        * en el menu
        * */

        binding.navView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.home -> {
                    Log.d("Navigation", "Se seleccionó Home")
                    replaceFragment(HomeFragment())
                }

                R.id.listado -> {
                    Log.d("Navigation", "Se seleccionó Listado")
                    replaceFragment(ListadoFragment())
                }

                R.id.calendario -> {
                    replaceFragment(CalendarioFragmentDiario())
                }

                R.id.mailing -> {
                    replaceFragment(MailingFragment())
                }
                R.id.configuracion ->{
                    replaceFragment(ConfiguracionFragment())
                }
                R.id.cerrar_sesion -> {
                    cerrarSesion(this)
                    return@setNavigationItemSelectedListener true
                }

                else -> false
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        //Metodo para cerrar el menu lateral una vez que se selecciona una opcion en el
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        true
        //Carga el fragment home por defecto al inicio despues del login
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    //Metodo para reemplazar un fragmento por otro
    private fun replaceFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_principal, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    //Funcion que permite responder a los click del usuario,es necesario sobreescribirla
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true
        else super.onOptionsItemSelected(item)
    }

    /*
    Borra el token de sharedpreferences y nos redirige a la pantalla de login
     */
    private fun cerrarSesion(context: Context) {
        val preferencias = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        preferencias.edit()
            .remove("auth_token")
            .remove("mantener_sesion")
            .apply()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    private fun lanzarWorkerSiNotificacionesActivas() {
        val prefs = getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val notificacionesActivas = prefs.getBoolean("notificaciones_activadas", false)

        if (notificacionesActivas) {
            // Se lanza una vez al iniciar
            val oneTime = OneTimeWorkRequestBuilder<CheckReservasWorker>().build()
            WorkManager.getInstance(this).enqueue(oneTime)

            // Se programa también periódico cada 15 minutos
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false) // opcional, pero puedes dejarlo así
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<CheckReservasWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CheckReservasWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }
    private fun crearCanalNotificacionesSiNoExiste() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_reservas",
                "Reservas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevas reservas"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }
    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val yaConcedido = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!yaConcedido) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

}