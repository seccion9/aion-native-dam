package com.example.gestionreservas.TestCajaChica

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gestionreservas.getOrAwaitValue
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.Field
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.ResumenItem
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.ComentariosRepository
import com.example.gestionreservas.viewModel.CajaChica.CajaChicaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CajaChicaViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CajaChicaViewModel
    private lateinit var mockRepository: CajaChicaRepository

    @Before
    fun setUp(){
        Dispatchers.setMain(testDispatcher)

        // Creamos el mock del repo
        mockRepository = mock(CajaChicaRepository::class.java)

        runTest {
            whenever(mockRepository.obtenerPagosDelDia("token_de_prueba", "2024-06-01")).thenReturn(
                listOf(
                    PagoCaja(
                        id = "1",
                        fecha = "2024-06-01",
                        concepto = "Compra de material",
                        cantidad = "50.00",
                        tipo = "ingreso",
                        parcial = null
                    ),
                    PagoCaja(
                        id = "2",
                        fecha = "2024-06-01",
                        concepto = "Otra compra",
                        cantidad = "30.00",
                        tipo = "gasto",
                        parcial = null
                    )
                )
            )
            whenever(mockRepository.registrarPagoCajaChica("token_de_prueba",
                PagoCaja(
                    id = "4",
                    fecha = "2024-06-11",
                    concepto = "Compra de bebidas",
                    cantidad = "50.00",
                    tipo = "efectivo",
                    parcial = null
                ))
            ).thenReturn(true)
            whenever(mockRepository.editarPago("token_de_prueba",
                PagoCaja(
                    id = "4",
                    fecha = "2024-06-11",
                    concepto = "Compra de bebidas",
                    cantidad = "50.00",
                    tipo = "efectivo",
                    parcial = null
                ))
            ).thenReturn(true)
            whenever(mockRepository.eliminarPago("token_de_prueba",
                PagoCaja(
                    id = "4",
                    fecha = "2024-06-11",
                    concepto = "Compra de bebidas",
                    cantidad = "50.00",
                    tipo = "efectivo",
                    parcial = null
                ))
            ).thenReturn(true)
        }
        viewModel=CajaChicaViewModel(mockRepository)

    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun obtenerPagosDiasTest()= runTest {
        viewModel.obtenerPagosCajaDia("token_de_prueba","2024-06-01")

        advanceUntilIdle()

        val pagos=viewModel.pagosCajaChica.getOrAwaitValue()
        assertEquals(2,pagos.size)
        assertEquals("Compra de material",pagos[0].concepto)
    }
    @Test
    fun obtenerPagosTotales()= runTest {
        viewModel.agregarPago("token_de_prueba",
            PagoCaja(
                id = "4",
                fecha = "2024-06-11",
                concepto = "Compra de bebidas",
                cantidad = "50.00",
                tipo = "efectivo",
                parcial = null
            ))

        advanceUntilIdle()

        val agregado=viewModel.pagoRegistrado.getOrAwaitValue()
        assertEquals(true,agregado)
    }
    @Test
    fun editarPago()= runTest {
        viewModel.editarPago("token_de_prueba",
            PagoCaja(
                id = "4",
                fecha = "2024-06-11",
                concepto = "Compra de bebidas",
                cantidad = "50.00",
                tipo = "efectivo",
                parcial = null
            ))

        advanceUntilIdle()
        val editado=viewModel.pagoRegistrado.getOrAwaitValue()
        assertEquals(true,editado)
    }
    @Test
    fun eliminarPago()=runTest {
        viewModel.eliminarPago("token_de_prueba",
            PagoCaja(
                id = "4",
                fecha = "2024-06-11",
                concepto = "Compra de bebidas",
                cantidad = "50.00",
                tipo = "efectivo",
                parcial = null
            ))

        advanceUntilIdle()
        val editado=viewModel.pagoEliminado.getOrAwaitValue()
        assertEquals(true,editado)
    }

}