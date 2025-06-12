package com.example.gestionreservas.TestDetalleSesion

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gestionreservas.getOrAwaitValue
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.Field
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.viewModel.DetalleSesion.DetalleSesionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
import org.mockito.Mockito.`when`

class DetalleSesionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DetalleSesionViewModel
    private lateinit var mockRepository: CompraRepository

    @Before
    fun setUp(){
        Dispatchers.setMain(testDispatcher)

        // Creamos el mock del repo
        mockRepository= mock(CompraRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargarSesionTest()= runTest {

        val sesion=SesionConCompra(Sesion(hora="2024-06-10", calendario ="cal1" , nombre ="Juan" , participantes =5 , totalPagado =100.0 , estado ="confirmado" , idiomas ="es" ), Compra(
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
            name = "Juan",
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

        viewModel=DetalleSesionViewModel(mockRepository)

        viewModel.cargarSesion(sesion)

        assertTrue(viewModel.compra.getOrAwaitValue()!=null)
        assertTrue(viewModel.pago.getOrAwaitValue()!=null)
        assertTrue(viewModel.reserva.getOrAwaitValue()!=null)
    }

    @Test
    fun actualizarCompraTest() = runTest {
        val compra = Compra(
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
            name = "Juan",
            mail = "cliente@email.com",
            dni = "12345678A",
            phone = "600123456",
            direction = "Calle Falsa 123",
            language = "es",
            ip = "192.168.0.1",
            comment = "Comentario",
            automaticActions = "",
            items = listOf(
                ItemReserva(
                    id = "item1",
                    idExperience = "exp001",
                    idCalendario = "cal1",
                    idBusinessUnit = "bu1",
                    status = "confirmado",
                    internaPermanent = false,
                    start = "2024-06-10T10:00:00",
                    end = "2024-06-10T11:00:00",
                    duration = 60,
                    peopleNumber = 5,
                    priceOriginal = 100.0,
                    priceTotal = 100.0,
                    priceFractioned = 0.0,
                    discountAmount = 0,
                    fields = mutableListOf(
                        Field(
                            id = "f1",
                            title = "Monitor asignado",
                            name = "monitor",
                            value = "Carlos",
                            amount = 0.0
                        )
                    ),
                    salas = listOf("sala1", "sala2")
                )

            ),
            payments = listOf(
                Pago(id = "p1", amount = 60.0, method = "Tarjeta", tipo = "Web", estado = "confirmado"),
                Pago(id = "p2", amount = 40.0, method = "Efectivo", tipo = "Efectivo", estado = "pendiente")
            ),
            resumenItems = null
        )

        viewModel = DetalleSesionViewModel(mockRepository)

        val item = compra.items.lastOrNull()
        val pago = compra.payments.lastOrNull()

        viewModel.actualizarSesion(compra, item, pago)

        assertTrue(viewModel.compra.getOrAwaitValue() == compra)
        assertTrue(viewModel.reserva.getOrAwaitValue() == item)
        assertTrue(viewModel.pago.getOrAwaitValue() == pago)
    }
    @Test
    fun cancelarCompraTest()= runTest {
        val compra = Compra(
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
            name = "Juan",
            mail = "cliente@email.com",
            dni = "12345678A",
            phone = "600123456",
            direction = "Calle Falsa 123",
            language = "es",
            ip = "192.168.0.1",
            comment = "Comentario",
            automaticActions = "",
            items = listOf(
                ItemReserva(
                    id = "item1",
                    idExperience = "exp001",
                    idCalendario = "cal1",
                    idBusinessUnit = "bu1",
                    status = "confirmado",
                    internaPermanent = false,
                    start = "2024-06-10T10:00:00",
                    end = "2024-06-10T11:00:00",
                    duration = 60,
                    peopleNumber = 5,
                    priceOriginal = 100.0,
                    priceTotal = 100.0,
                    priceFractioned = 0.0,
                    discountAmount = 0,
                    fields = mutableListOf(
                        Field(
                            id = "f1",
                            title = "Monitor asignado",
                            name = "monitor",
                            value = "Carlos",
                            amount = 0.0
                        )
                    ),
                    salas = listOf("sala1", "sala2")
                )

            ),
            payments = listOf(
                Pago(id = "p1", amount = 60.0, method = "Tarjeta", tipo = "Web", estado = "confirmado"),
                Pago(id = "p2", amount = 40.0, method = "Efectivo", tipo = "Efectivo", estado = "pendiente")
            ),
            resumenItems = null
        )
        viewModel = DetalleSesionViewModel(mockRepository)

        `when`(mockRepository.eliminarCompra("token_de_prueba", "c1")).thenReturn(true)
        viewModel.cancelarReserva(
            token = "token_de_prueba",
            idCompra = compra.id,
            onSuccess = {},
            onError = {}
        )
        advanceUntilIdle()

        val borrado=viewModel.estadoCancelacion.getOrAwaitValue()
        assertEquals(true,borrado)

    }

}