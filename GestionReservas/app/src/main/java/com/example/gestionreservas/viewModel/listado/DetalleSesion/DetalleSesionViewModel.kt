package com.example.gestionreservas.viewModel.listado.DetalleSesion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import kotlinx.coroutines.launch

class DetalleSesionViewModel(
    private val compraRepository: CompraRepository
) : ViewModel() {
    /**
     * Actualiza los campos con las modificaciones para mostrarlas en el fragment
     */
    private val _compra = MutableLiveData<Compra?>()
    val compra: LiveData<Compra?> = _compra

    private val _reserva = MutableLiveData<ItemReserva?>()
    val reserva: LiveData<ItemReserva?> = _reserva

    private val _pago = MutableLiveData<Pago?>()
    val pago: LiveData<Pago?> = _pago
    /**
     * Obtiene los datos pasados por parámetros al fragment de una SesionConCompra y rellena las
     * variables del viewmodel
     */
    fun cargarSesion(sesionConCompra: SesionConCompra?) {
        if (sesionConCompra == null || sesionConCompra.compra == null) {
            _compra.value = null
            _reserva.value = null
            _pago.value = null
        } else {
            _compra.value = sesionConCompra.compra
            _reserva.value = sesionConCompra.compra.items.lastOrNull()
            _pago.value = sesionConCompra.compra.payments.lastOrNull()
        }
    }

    /**
     * Modifica la compra a través del repository compra cumpliendo estructura MVVM
     */
    fun modificarCompra(token: String, compra: Compra, onSuccess: () -> Unit, onError: (String) -> Unit) {

        viewModelScope.launch {
            try{
                val success=compraRepository.modificarCompra(token,compra)
                if (success) onSuccess() else onError("Fallo al guardar")

            }catch (e:Exception){
                onError(e.message ?: "Error desconocido")
            }
        }

    }
    /**
     * Actualiza los campos con las modificaciones para mostrarlas en el fragment
     */
    fun actualizarSesion(compra: Compra?, reserva: ItemReserva?, pago: Pago?) {
        _compra.value = compra
        _reserva.value = reserva
        _pago.value = pago
    }

}