package com.example.gestionreservas.viewModel.Configuracion

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gestionreservas.background.CheckReservasWorker
import com.example.gestionreservas.repository.MailingRepository

class ConfiguracionViewModel(
    private val mailingRepository: MailingRepository
):ViewModel() {

    private val _email= MutableLiveData<String?>()
    val email: LiveData<String?> get() = _email

    private val _modoOscuro=MutableLiveData<Boolean>()
    val modoOscuro:MutableLiveData<Boolean> = _modoOscuro

    private val _notificacionesActivas = MutableLiveData<Boolean>()
    val notificacionesActivas: LiveData<Boolean> get() = _notificacionesActivas

    fun cargarEstadoNotificaciones(context: Context) {
        val estado = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .getBoolean("notificaciones_activadas", false)
        _notificacionesActivas.value = estado
    }

    fun cambiarEstadoNotificaciones(context: Context, activadas: Boolean) {
        val prefs = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notificaciones_activadas", activadas).apply()
        _notificacionesActivas.value = activadas

        if (activadas) {
            val workRequest = OneTimeWorkRequestBuilder<CheckReservasWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("CheckReservasWorker")
        }
    }


    fun cambiarModoOscuro(context: Context,isChecked:Boolean){
        _modoOscuro.value = isChecked

        val nuevoModo = if (isChecked)
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO

        AppCompatDelegate.setDefaultNightMode(nuevoModo)

        context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .edit().putBoolean("modo_oscuro", isChecked).apply()

    }
    fun cargarModoOscuro(context: Context) {
        val activado = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .getBoolean("modo_oscuro", false)
        _modoOscuro.value = activado
    }
    fun obtenerEmailUsuario(context:Context){
        var email=mailingRepository.obtenerEmailUsuario(context)
        _email.value = email
    }
    fun obtenerEmailDesdePrefs(context: Context) {
        val prefs = context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        _email.value = prefs.getString("email_usuario", "No conectado")
    }

}