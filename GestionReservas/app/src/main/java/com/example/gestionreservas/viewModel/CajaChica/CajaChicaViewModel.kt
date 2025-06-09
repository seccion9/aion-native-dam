package com.example.gestionreservas.viewModel.CajaChica

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.ComentariosRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class CajaChicaViewModel(
    private val cajaChicaRepository: CajaChicaRepository
) : ViewModel() {
    private val _pagosCajaChica = MutableLiveData<List<PagoCaja>>()
    val pagosCajaChica: LiveData<List<PagoCaja>> = _pagosCajaChica

    @RequiresApi(Build.VERSION_CODES.O)
    private val _fechaActual = MutableLiveData(LocalDate.now())

    @RequiresApi(Build.VERSION_CODES.O)
    val fechaActual: LiveData<LocalDate> = _fechaActual

    private val _pagoRegistrado = MutableLiveData<Boolean?>()
    val pagoRegistrado: LiveData<Boolean?> = _pagoRegistrado

    private val _pagoEliminado = MutableLiveData<Boolean?>()
    val pagoEliminado: LiveData<Boolean?> = _pagoEliminado


    /**
     * Obtiene la fecha actual
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerFechaHoy() {
        _fechaActual.value = LocalDate.now()
    }

    /**
     * Obtiene los pagos del día de hoy y los asigna a los datos del viewmodel.Se comprueban datos en consola.
     */
    fun obtenerPagosCajaDia(token: String, fecha: String) {
        viewModelScope.launch {
            try {
                var listaPagos = cajaChicaRepository.obtenerPagosDelDia(token, fecha)
                if (listaPagos != null) {
                    _pagosCajaChica.value = listaPagos
                    Log.e("ViewModelCajaChica", "Pagos obtenidos: $listaPagos")
                } else {
                    Log.e("ViewModelCajaChica", "Error al obtener pagos caja")
                    _pagosCajaChica.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("ViewModelCajaChica", "Error al obtener pagos caja : ${e.message}")
            }
        }
    }

    /**
     * Agrega un pago en la API a través del repository y actualiza si se registro o no exitosamente aparte
     * de añadir el pago a la lista si es exitoso.
     */
    fun agregarPago(token: String, pago: PagoCaja) {
        viewModelScope.launch {
            try {
                val success = cajaChicaRepository.registrarPagoCajaChica(token, pago)
                _pagoRegistrado.value=success
                if (success) {
                    val listaActual = _pagosCajaChica.value?.toMutableList() ?: mutableListOf()
                    listaActual.add(pago)
                    _pagosCajaChica.value = listaActual
                }
            } catch (e: Exception) {
                _pagoRegistrado.postValue(false)
                Log.e("CajaChicaViewModel", "Error al registrar pago: ${e.message}")
            }
        }
    }

    /**
     * Agrega un pago en la API a través del repository y actualiza si se registra o no exitosamente aparte
     * de editar el pago en la lista si es exitoso.
     */
    fun editarPago(token: String, pago: PagoCaja){
        viewModelScope.launch {
            try {
                val success=cajaChicaRepository.editarPago(token,pago)
                _pagoRegistrado.value=success
                if (success) {
                    val listaActual = _pagosCajaChica.value?.toMutableList() ?: mutableListOf()
                    val index = listaActual.indexOfFirst { it.id == pago.id }
                    if (index != -1) {
                        listaActual[index] = pago
                        _pagosCajaChica.value = listaActual
                    }
                }
            }catch (e:Exception){
                _pagoRegistrado.postValue(false)
                Log.e("CajaChicaViewModel", "Error al editar pago: ${e.message}")
            }
        }
    }

    /**
     * Elimina el pago de la APIa través del repository y si es exitoso borra de la lista el pago que corresponda
     * con el id del pago eliminado.
     */
    fun eliminarPago(token: String, pago: PagoCaja){
        viewModelScope.launch {
            try {
                val success = cajaChicaRepository.eliminarPago(token, pago)
                _pagoEliminado.value = success

                if (success) {
                    val listaActual = _pagosCajaChica.value?.toMutableList() ?: mutableListOf()
                    val index = listaActual.indexOfFirst { it.id == pago.id }
                    if (index != -1) {
                        listaActual.removeAt(index)
                        _pagosCajaChica.value = listaActual
                    }
                }

            } catch (e: Exception) {
                _pagoEliminado.value = false
                Log.e("CajaChicaViewModel", "Error al borrar pago: ${e.message}")
            }
        }
    }

    /**
     * Reestablece estado de pagos para volver a comprobar en otras transacciones desde cero el estado de los
     * pagos
     */
    fun limpiarEstadoPago() {
        _pagoRegistrado.value = null
    }
    fun limpiarEstadoPagoEliminado() {
        _pagoEliminado.value = null
    }


}