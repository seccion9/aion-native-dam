package com.example.gestionreservas.TestConfiguracion

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gestionreservas.getOrAwaitValue
import com.example.gestionreservas.repository.EmailRepository
import com.example.gestionreservas.viewModel.Configuracion.ConfiguracionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ConfiguracionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ConfiguracionViewModel
    private lateinit var mockRepository: EmailRepository

    @Before
    fun setUp(){
        Dispatchers.setMain(testDispatcher)
        mockRepository=mock(EmailRepository::class.java)
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun modoOscuroTest() = runTest {
        // Arrange: mocks necesarios
        val contextoFalso = mock(Context::class.java)
        val sharedPrefs = mock(SharedPreferences::class.java)

        // Simula que el modo oscuro estaba activado en SharedPreferences
        whenever(contextoFalso.getSharedPreferences("ajustes", Context.MODE_PRIVATE)).thenReturn(sharedPrefs)
        whenever(sharedPrefs.getBoolean("modo_oscuro", false)).thenReturn(true)

        viewModel = ConfiguracionViewModel(mockRepository)

        viewModel.cargarModoOscuro(contextoFalso)

        val resultado = viewModel.modoOscuro.getOrAwaitValue()
        assertTrue(resultado)
    }
    @Test
    fun cargarEstadoNotificacionesTest()= runTest {
        val contextoFalso = mock(Context::class.java)
        val sharedPrefs = mock(SharedPreferences::class.java)

        whenever(contextoFalso.getSharedPreferences("ajustes", Context.MODE_PRIVATE)).thenReturn(sharedPrefs)
        whenever(sharedPrefs.getBoolean("notificaciones_activadas", false)).thenReturn(true)

        viewModel = ConfiguracionViewModel(mockRepository)
        viewModel.cargarEstadoNotificaciones(contextoFalso)

        val resultado=viewModel.notificacionesActivas.getOrAwaitValue()
        assertTrue(resultado)
    }
    @Test
    fun obtenerEmailUsuarioTest() = runTest {

        val contextoFalso = mock(Context::class.java)
        val emailEsperado = "test@email.com"

        whenever(mockRepository.obtenerEmailUsuario(contextoFalso)).thenReturn(emailEsperado)

        viewModel = ConfiguracionViewModel(mockRepository)

        viewModel.obtenerEmailUsuario(contextoFalso)

        val resultado = viewModel.email.getOrAwaitValue()
        assertEquals(emailEsperado, resultado)
    }


}