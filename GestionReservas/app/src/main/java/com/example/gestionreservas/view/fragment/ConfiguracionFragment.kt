package com.example.gestionreservas.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentConfiguracionBinding
import com.example.gestionreservas.models.entity.CheckReservasWorker
import com.example.gestionreservas.repository.MailingRepository
import com.example.gestionreservas.view.activities.MainActivity
import java.util.concurrent.TimeUnit

class ConfiguracionFragment:Fragment(),OnClickListener {
    private lateinit var binding:FragmentConfiguracionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentConfiguracionBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Configuracion"
        programarWorkerNotificaciones()
        instancias()

        return binding.root
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(requireContext(), "Notificaciones activadas con exito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No tiene permisos para activar notificaciones", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun instancias(){

        //Comprobacion correo
        val email = MailingRepository.obtenerEmailUsuario(requireContext())
        binding.tvCorreoGmail.text = email ?: "No conectado"
        //Instancia funciones inicilaes
        configurarNotificaciones()
        cambiarAModoOscuro()
        btnSpannable()
        crearCanalNotificaciones()

        //Instancias listener
        if (email != null && email != "No conectado") {
            binding.tvCerrarSesionGmail.setOnClickListener(this)
        }
        binding.tvCerrarSesionGlobal.setOnClickListener(this)

    }
    private fun cambiarAModoOscuro(){
        val prefs = requireContext().getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val modoOscuroActivado = prefs.getBoolean("modo_oscuro", false)

        binding.switchModoOscuro.isChecked = modoOscuroActivado

        binding.switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            val nuevoModo = if (isChecked)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

            AppCompatDelegate.setDefaultNightMode(nuevoModo)

            prefs.edit().putBoolean("modo_oscuro", isChecked).apply()
        }
    }
    private fun btnSpannable() {
        val texto = SpannableString("Desconectar")

        val color = ContextCompat.getColor(requireContext(), R.color.cerrar_sesion)

        texto.setSpan(ForegroundColorSpan(color), 0, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        texto.setSpan(UnderlineSpan(), 0, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvCerrarSesionGmail.text = texto

        val textoSesion = SpannableString("Cerrar sesión")

        textoSesion.setSpan(ForegroundColorSpan(color), 0, textoSesion.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textoSesion.setSpan(UnderlineSpan(), 0, textoSesion.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvCerrarSesionGlobal.text = textoSesion
    }
    private fun configurarNotificaciones() {
        val prefs = requireContext().getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val notificacionesActivas = prefs.getBoolean("notificaciones_activadas", false)

        binding.switchNotificaciones.setOnCheckedChangeListener(null)
        binding.switchNotificaciones.isChecked = notificacionesActivas

        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notificaciones_activadas", isChecked).apply()

            if (isChecked) {
                pedirPermisoNotificaciones()
                programarWorkerNotificaciones()
                Toast.makeText(requireContext(), "Notificaciones activadas", Toast.LENGTH_SHORT).show()
            } else {
                cancelarWorkerNotificaciones()
                Toast.makeText(requireContext(), "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
            }
        }

        // Activar automáticamente si estaba ya activado
        if (notificacionesActivas) {
            programarWorkerNotificaciones()
        }
    }
    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }
    private fun cancelarWorkerNotificaciones() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("CheckReservasWorker")
    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvCerrarSesionGmail.id->{

                MailingRepository.cerrarSesion(requireContext()) {
                    Toast.makeText(requireContext(), "Sesión de Gmail cerrada", Toast.LENGTH_SHORT).show()
                    requireActivity().invalidateOptionsMenu()
                    binding.tvCorreoGmail.text="No conectado"
                }
            }
            binding.tvCerrarSesionGlobal.id->{
                cerrarSesion(requireContext())
            }
        }
    }
    private fun cerrarSesion(context: Context) {
        val preferencias = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        preferencias.edit().remove("auth_token").apply()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reservas"
            val descriptionText = "Canal para notificaciones de nuevas reservas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("canal_reservas", name, importance)


            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun programarWorkerNotificaciones() {
        /*val workRequest = PeriodicWorkRequestBuilder<CheckReservasWorker>(
            15, TimeUnit.MINUTES
        ).build()*/

        val workRequest = OneTimeWorkRequestBuilder<CheckReservasWorker>().build()
        WorkManager.getInstance(requireContext()).enqueue(workRequest)

        /*WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "CheckReservasWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )*/
    }
}