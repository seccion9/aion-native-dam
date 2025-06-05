package com.example.gestionreservas.TestConfiguracion

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.gestionreservas.repository.MailingRepository
import com.example.gestionreservas.viewModel.Configuracion.ConfiguracionViewModel
import com.example.gestionreservas.viewModel.Configuracion.getOrAwaitValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConfiguracionViewModelTest {


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ConfiguracionViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var context: Context


    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("ajustes", Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = ConfiguracionViewModel(MailingRepository)
    }

    @Test
    fun modoOscuro_guardarYCargar_correcto() {
        // Guardamos true
        viewModel.cambiarModoOscuro(context, true)

        // Comprobamos que se guarda en prefs
        val valorPrefs = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            .getBoolean("modo_oscuro", false)
        assertTrue(valorPrefs)

        // Cargamos desde ViewModel
        viewModel.cargarModoOscuro(context)

        // Verificamos que el valor LiveData también es true
        assertEquals(true, viewModel.modoOscuro.value)
    }

    @Test
    fun notificaciones_guardarYcargar(){
        //Ponemos en true las notificaciones
        viewModel.cambiarEstadoNotificaciones(context,true)

        //Comprobamos que preferencias de notificaciones son verdaderas
        val preferencias=context.getSharedPreferences("ajustes",Context.MODE_PRIVATE)
            .getBoolean("notificaciones_activadas",false)
        assertTrue(preferencias)

        //Cargamos preferencias al viewmodel
        viewModel.cargarEstadoNotificaciones(context)

        //Verificamos que livedata de notificaciones es true

        assertEquals(true,viewModel.notificacionesActivas.value)

    }

    @Test
    fun comprobarCorreo() {
        val prefs = context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        prefs.edit().putString("email_usuario", "sergio@gmail.com").commit()

        viewModel.obtenerEmailDesdePrefs(context)
        val email = viewModel.email.getOrAwaitValue()
        assertEquals("sergio@gmail.com", email)

    }

}