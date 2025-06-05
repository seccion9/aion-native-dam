import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.gestionreservas.models.entity.PagoCajaChica
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.viewModel.CajaChica.CajaChicaViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*

@OptIn(ExperimentalCoroutinesApi::class)
class CajaChicaViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var cajaChicaRepository: CajaChicaRepository
    private lateinit var compraRepository: CompraRepository
    private lateinit var viewModel: CajaChicaViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxed = true)
        cajaChicaRepository = mockk()
        compraRepository = mockk()
        viewModel = CajaChicaViewModel(cajaChicaRepository, compraRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCargaCajaChica() = runTest {
        val token = "fake-token"
        val pagosFicticios = listOf(
            PagoCajaChica("1", "2025-06-04", "factura", 12.0, "Tarjeta")
        )

        // Simulamos respuesta del repo
        coEvery { cajaChicaRepository.obtenerPagosTotales(token) } returns pagosFicticios
        coEvery { compraRepository.obtenerCompras(token) } returns emptyList()

        // Observador simulado
        val observer = mockk<Observer<List<PagoCajaChica>>>(relaxed = true)
        viewModel.pagosCaja.observeForever(observer)

        // Ejecutar función
        viewModel.obtenerPagos(token)

        // Avanza corrutinas para que se ejecuten
        advanceUntilIdle()

        verify { observer.onChanged(pagosFicticios) } // ya no fallará
        coVerify { cajaChicaRepository.obtenerPagosTotales(token) }
        coVerify { compraRepository.obtenerCompras(token) }

        viewModel.pagosCaja.removeObserver(observer)
    }
}
