package com.example.gestionreservas.viewModel.listado.ListadosReservas

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate



class ListadoViewModel(
    private val compraRepository: CompraRepository
) : ViewModel() {
    private val _sesiones= MutableLiveData<List<SesionConCompra>>()
    val sesiones: LiveData<List<SesionConCompra>> = _sesiones

    fun obtenerDatosCompras(token:String){
        viewModelScope.launch {
            try {

                val compras = withContext(Dispatchers.IO) {
                    compraRepository.obtenerCompras(token)
                }
                Log.e("ViewModelListado","Compras recuperadas : $compras")
                val sesiones = withContext(Dispatchers.Default) {
                    compraRepository.transformarComprasASesionesTodas(compras)
                }.sortedByDescending { it.compra?.items?.lastOrNull()?.start ?: "" }


                _sesiones.value = sesiones

            } catch (e: Exception) {
                Log.e("ViewModelListado", "Error obteniendo compras: ${e.message}")
                _sesiones.value = emptyList()
            }
        }
    }
}