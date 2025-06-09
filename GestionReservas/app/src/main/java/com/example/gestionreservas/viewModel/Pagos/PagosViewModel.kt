package com.example.gestionreservas.viewModel.Pagos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.PagoCajaChica
import com.example.gestionreservas.models.entity.PagoReserva
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import kotlinx.coroutines.launch

class PagosViewModel(
    private val cajaChicaRepository: CajaChicaRepository,
    private val compraRepository: CompraRepository
) : ViewModel() {

    private val _pagosCaja = MutableLiveData<List<PagoReserva>>()
    val pagosCaja: LiveData<List<PagoReserva>> = _pagosCaja

    private val _compras = MutableLiveData<List<Compra>>()
    val compras : LiveData<List<Compra>> = _compras

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    private val _pagosFiltrados = MutableLiveData<List<PagoReserva>>()
    val pagosFiltrados: LiveData<List<PagoReserva>> get() = _pagosFiltrados

    private val _mapaPagosACompras = mutableMapOf<String, Compra>()
    val mapaPagosACompras: Map<String, Compra> get() = _mapaPagosACompras

    private var filtroTipo = "-- Todos --"
    private var filtroEstado = "-- Todos --"
    private var filtroConcepto = ""


    fun actualizarFiltros(tipo: String? = null, estado: String? = null, concepto: String? = null) {
        tipo?.let { filtroTipo = it }
        estado?.let { filtroEstado = it }
        concepto?.let { filtroConcepto = it }

        val listaOriginal = _pagosCaja.value ?: return
        _pagosFiltrados.value = listaOriginal.filter { pago ->
            val coincideTipo = (filtroTipo == "-- Todos --" || pago.tipo == filtroTipo)
            val coincideEstado = (filtroEstado == "-- Todos --" || pago.estado == filtroEstado)
            val coincideConcepto = pago.concepto.contains(filtroConcepto, ignoreCase = true)
            coincideTipo && coincideEstado && coincideConcepto
        }
    }


    fun obtenerPagos(token: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val listaCajaChica = cajaChicaRepository.obtenerPagosTotales(token)
                    .filter { it.cantidad > 0 }
                    .map { pagoCaja ->
                        PagoReserva(
                            id = pagoCaja.id ?: "sin_id",
                            fecha = pagoCaja.fecha,
                            concepto = pagoCaja.concepto,
                            cantidad = pagoCaja.cantidad,
                            tipo = "Manual",
                            metodo = "Efectivo",
                            reserva = null,
                            estado = "confirmado"
                        )
                    }


                val listaCompras= compraRepository.obtenerCompras(token)
                var pagosDesdeCompras =generarPagosConCompras(listaCompras)
                if (listaCompras!=null || listaCajaChica!=null) {
                    _compras.value=listaCompras
                    _pagosCaja.value =(pagosDesdeCompras+listaCajaChica).sortedByDescending { it.fecha }
                } else {
                    Log.e("ViewModelCajaChica", "Error al obtener bloqueos")
                    _compras.value= emptyList()
                    _pagosCaja.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ViewModelCajaChica", "Error al obtener pagos ${e.message}")
            }finally {
                _cargando.value = false
            }
        }
    }
    fun generarPagosConCompras(listaCompras: List<Compra>): List<PagoReserva> {
        val listaPagos: MutableList<PagoReserva> = mutableListOf()
        _mapaPagosACompras.clear()

        listaCompras.forEach { compra ->
            compra.payments.forEach { payment ->
                Log.d("PagosDebug", "Payment: ${payment.id}, tipo: ${payment.tipo}")
                val pago = PagoReserva(
                    id = payment.id,
                    fecha = compra.fechaCompra,
                    concepto = compra.name,
                    cantidad = payment.amount,
                    metodo = payment.method,
                    tipo = payment.tipo ?: "Desconocido",
                    estado = payment.estado ?: "Desconocido"
                )
                listaPagos.add(pago)

                if (payment.tipo != "Efectivo") {
                    _mapaPagosACompras[payment.id] = compra
                }
            }
        }
        return listaPagos
    }
}