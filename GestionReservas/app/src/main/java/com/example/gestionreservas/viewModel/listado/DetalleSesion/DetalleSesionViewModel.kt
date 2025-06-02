package com.example.gestionreservas.viewModel.listado.DetalleSesion

import android.util.Log
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

    private val _estadoCancelacion = MutableLiveData<String?>()
    val estadoCancelacion: LiveData<String?> = _estadoCancelacion

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

    /**
     * Lanzamos una corrutina e intentamos eliminar compra a través e repository que realizará la eliminación
     * a través del ApiServiceFake,si es exitoso cambiamos estado de cancelación a éxito,si no a fallo o error.
     */
    fun cancelarReserva(token:String,idCompra:String,onSuccess: () -> Unit, onError: (String) -> Unit){
        Log.e("TOKEN EN CANCELAR RESERVA:",token)
        viewModelScope.launch {
            try{
                val success=compraRepository.eliminarCompra(token,idCompra)
                if (success) {
                    _estadoCancelacion.value = "Reserva cancelada con éxito"
                    onSuccess()
                } else {
                    _estadoCancelacion.value = "Fallo al cancelar la reserva"
                    onError("Fallo al cancelar")
                }
            }catch (e:Exception){
                _estadoCancelacion.value = "Error al cancelar la compra/reserva"
                onError(e.message ?: "Error desconocido")
            }
        }
    }
    fun cargarSoloCompra(compra: Compra?) {
        _compra.value = compra
        _reserva.value = compra?.items?.lastOrNull()
        _pago.value = compra?.payments?.lastOrNull()
    }


}