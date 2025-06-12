package com.example.gestionreservas.TestComentarios

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gestionreservas.getOrAwaitValue
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.repository.ComentariosRepository
import com.example.gestionreservas.viewModel.Comentarios.ComentariosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.Assert.assertEquals
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ComentariosViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ComentariosViewModel
    private lateinit var mockRepository: ComentariosRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Creamos el mock del repo
        mockRepository = mock(ComentariosRepository::class.java)

        // Preparamos respuesta simulada
        runTest {
            whenever(mockRepository.obtenerComentariosApi("token_de_prueba")).thenReturn(
                listOf(
                    Comentario("1", "2025-06-11 09:00", "Comentario 1", "comentario", "sergio"),
                    Comentario("2", "2025-06-11 10:00", "Hola mundo", "comentario", "sergio")
                )
            )
            whenever(
                mockRepository.eliminarComentario(
                    "token_de_prueba",
                    Comentario("1", "2025-06-11 09:00", "Comentario 1", "comentario", "sergio")
                )
            ).thenReturn(true)
            whenever(
                mockRepository.editarComentario("token_de_prueba", Comentario("1", "2025-06-11 09:00", "Comentario 1", "comentario", "sergio"))
            ).thenReturn(true)
        }

        viewModel = ComentariosViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `obtenerComentarios actualiza lista correctamente`() = runTest {
        viewModel.obtenerComentarios("token_de_prueba")
        viewModel.eliminarComentario("token_de_prueba",Comentario("1", "2025-06-11 09:00", "Comentario 1", "comentario", "sergio"))

        advanceUntilIdle()

        val eliminado = viewModel.comentarioEliminado.getOrAwaitValue()
        assertEquals(true, eliminado)
        val comentarios = viewModel.comentarios.getOrAwaitValue()
        assertEquals(1, comentarios.size)
        assertEquals("Hola mundo", comentarios[0].descripcion)


    }
    @Test
    fun `editarComentario falla correctamente`() = runTest {
        whenever(
            mockRepository.editarComentario("token_de_prueba", Comentario("3", "2025-06-11 11:00", "Error", "comentario", "sergio"))
        ).thenReturn(false)

        viewModel.editarComentario("token_de_prueba", Comentario("3", "2025-06-11 11:00", "Error", "comentario", "sergio"))

        advanceUntilIdle()

        val editado = viewModel.comentarioEditado.getOrAwaitValue()
        assertEquals(false, editado)
    }

}