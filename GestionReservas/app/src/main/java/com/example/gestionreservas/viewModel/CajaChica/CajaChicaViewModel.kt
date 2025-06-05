package com.example.gestionreservas.viewModel.CajaChica

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.PagoCajaChica
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import kotlinx.coroutines.launch

class CajaChicaViewModel(
    private val cajaChicaRepository: CajaChicaRepository,
    private val compraRepository: CompraRepository
) : ViewModel() {

    private val _pagosCaja = MutableLiveData<List<PagoCajaChica>>()
    val pagosCaja: LiveData<List<PagoCajaChica>> = _pagosCaja

    private val _compras = MutableLiveData<List<Compra>>()
    val compras : LiveData<List<Compra>> = _compras

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    private val _pagosFiltrados = MutableLiveData<List<PagoCajaChica>>()
    val pagosFiltrados: LiveData<List<PagoCajaChica>> get() = _pagosFiltrados

    private val _mapaPagosACompras = mutableMapOf<String, Compra>()
    val mapaPagosACompras: Map<String, Compra> get() = _mapaPagosACompras


    fun aplicarFiltroPorMetodo(metodo: String) {
        val listaOriginal = _pagosCaja.value ?: return

        _pagosFiltrados.value = if (metodo == "-- Todos --") {
            listaOriginal
        } else {
            listaOriginal.filter { it.tipo == metodo }
        }
    }
    fun aplicarFiltroPorConcepto(texto: String) {
        val pagosFiltrados = _pagosCaja.value?.filter {
            it.concepto.contains(texto, ignoreCase = true)
        } ?: emptyList()
        _pagosFiltrados.value = pagosFiltrados
    }


    fun obtenerPagos(token: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val listaPagos = cajaChicaRepository.obtenerPagosTotales(token)
                val listaCompras= compraRepository.obtenerCompras(token)
                var pagosDesdeCompras =generarPagosConCompras(listaCompras)
                if (listaPagos != null || listaCompras!=null) {
                    _compras.value=listaCompras
                    _pagosCaja.value = (listaPagos + pagosDesdeCompras).sortedByDescending { it.fecha }
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
    fun generarPagosConCompras(listaCompras: List<Compra>): List<PagoCajaChica> {
        val listaPagos: MutableList<PagoCajaChica> = mutableListOf()
        _mapaPagosACompras.clear()

        listaCompras.forEach { compra ->
            compra.payments.forEach { payment ->
                val pago = PagoCajaChica(
                    id = payment.id,
                    fecha = compra.fechaCompra,
                    concepto = compra.name,
                    cantidad = payment.amount,
                    tipo = payment.method
                )
                listaPagos.add(pago)
                if (payment.method != "Manual") {
                    _mapaPagosACompras[payment.id] = compra
                }
            }
        }
        return listaPagos
    }
}