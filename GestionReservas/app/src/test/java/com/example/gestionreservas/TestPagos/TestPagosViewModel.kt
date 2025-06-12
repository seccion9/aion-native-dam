package com.example.gestionreservas.TestPagos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.viewModel.Pagos.PagosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock


class TestPagosViewModel {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PagosViewModel
    private lateinit var mockRepository: CompraRepository
    private lateinit var mockRepository2: CajaChicaRepository


    @Before
    fun setUp(){
        Dispatchers.setMain(testDispatcher)

        // Creamos el mock del repo
        mockRepository=mock(CompraRepository::class.java)
        mockRepository2=mock(CajaChicaRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun generarPagosConCompras_generarCorrectamente() = runTest {
        // Given: lista de compras con distintos tipos de pagos
        val listaCompras = listOf(
            Compra(
                userId = "1",
                id = "c1",
                uuid = "uuid-123",
                status = "confirmado",
                mailStatus = "enviado",
                internaPermanent = false,
                idDiscount = null,
                idBono = null,
                priceFinal = 100.0,
                priceAfterDiscount = 100.0,
                priceFractioned = 0.0,
                isFractioned = false,
                fechaCompra = "2024-06-10",
                name = "Escape Room 1",
                mail = "cliente@email.com",
                dni = "12345678A",
                phone = "600123456",
                direction = "Calle Falsa 123",
                language = "es",
                ip = "192.168.0.1",
                comment = "Comentario",
                automaticActions = "",
                items = listOf(),
                payments = listOf(
                    Pago(
                        id = "p1",
                        amount = 60.0,
                        method = "Tarjeta",
                        tipo = "Web",
                        estado = "confirmado"
                    ),
                    Pago(
                        id = "p2",
                        amount = 40.0,
                        method = "Efectivo",
                        tipo = "Efectivo",
                        estado = "pendiente"
                    )
                ),
                resumenItems = null
            )
        )

        // ViewModel con dispatcher de test
         viewModel = PagosViewModel(mock(), mock())

        // When
        val resultado = viewModel.generarPagosConCompras(listaCompras)

        // Then
        assertEquals(2, resultado.size)

        val pago1 = resultado[0]
        assertEquals("p1", pago1.id)
        assertEquals("Tarjeta", pago1.metodo)
        assertEquals("Web", pago1.tipo)
        assertEquals("confirmado", pago1.estado)

        val pago2 = resultado[1]
        assertEquals("p2", pago2.id)
        assertEquals("Efectivo", pago2.metodo)
        assertEquals("Efectivo", pago2.tipo)
        assertEquals("pendiente", pago2.estado)

        // Solo el pago no efectivo se guarda en el mapa interno
        assertEquals(1, viewModel.mapaPagosACompras.size)
        assertTrue(viewModel.mapaPagosACompras.containsKey("p1"))
        assertFalse(viewModel.mapaPagosACompras.containsKey("p2"))
    }

}