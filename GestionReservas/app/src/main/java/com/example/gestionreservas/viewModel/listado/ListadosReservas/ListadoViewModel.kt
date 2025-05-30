package com.example.gestionreservas.viewModel.listado.ListadosReservas

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.ExperienciaCompleta
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.models.repository.ExperienciaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate



class ListadoViewModel(
    private val compraRepository: CompraRepository,
    private val experienciaRepository: ExperienciaRepository
) : ViewModel() {
    private val _sesiones = MutableLiveData<List<SesionConCompra>>()
    val sesiones: LiveData<List<SesionConCompra>> = _sesiones

    private val _estadoSeleccionado = MutableLiveData<String?>()
    val estadoSeleccionado: LiveData<String?> get() = _estadoSeleccionado

    private val _listaCompleta = MutableLiveData<List<SesionConCompra>>()
    private val _listaFiltrada = MutableLiveData<List<SesionConCompra>>()
    val listaFiltrada: LiveData<List<SesionConCompra>> get() = _listaFiltrada

    private val _nombreBusqueda = MutableLiveData<String?>()
    val nombreBusqueda: LiveData<String?> get() = _nombreBusqueda

    private val _experiencias=MutableLiveData<List<ExperienciaCompleta>>()
    val experiencias: LiveData<List<ExperienciaCompleta>> = _experiencias


    private fun filtrarSesiones() {
        val estado = _estadoSeleccionado.value
        val nombre = _nombreBusqueda.value?.trim()?.lowercase() ?: ""
        val lista = _listaCompleta.value.orEmpty()

        _listaFiltrada.value = lista.filter { sesion ->
            val coincideEstado = estado.isNullOrEmpty() || estado == "Todas" ||
                    sesion.compra?.status.equals(estado, ignoreCase = true)
            val coincideNombre = sesion.sesion.nombre.lowercase().contains(nombre)

            coincideEstado && coincideNombre
        }
    }

    fun actualizarNombreBusqueda(nombre: String?) {
        _nombreBusqueda.value = nombre
        filtrarSesiones()
    }

    fun actualizarEstadoSeleccionado(estado: String?) {
        _estadoSeleccionado.value = estado
        filtrarSesiones()
    }


    fun obtenerDatosCompras(token: String) {
        viewModelScope.launch {
            try {
                val compras = withContext(Dispatchers.IO) {
                    compraRepository.obtenerCompras(token)
                }
                Log.e("ViewModelListado", "Compras recuperadas : $compras")
                val sesiones = withContext(Dispatchers.Default) {
                    compraRepository.transformarComprasASesionesTodas(compras)
                }.sortedByDescending { it.compra?.fechaCompra ?: "" }

                _sesiones.value = sesiones
                _listaCompleta.value = sesiones
                filtrarSesiones()

            } catch (e: Exception) {
                Log.e("ViewModelListado", "Error obteniendo compras: ${e.message}")
                _sesiones.value = emptyList()
                _listaCompleta.value = emptyList()
                _listaFiltrada.value = emptyList()
            }
        }
    }

    fun obtenerExperiencias(token: String) {
        viewModelScope.launch {
            try {
                val experienciasObtenidas = withContext(Dispatchers.IO) {
                    experienciaRepository.obtenerExperienciasApi(token)
                }
                experienciasObtenidas?.let {
                    _experiencias.value = it
                } ?: Log.e("HOMEVIEWMODEL", "Lista de experiencias es nula")
            } catch (e: Exception) {
                Log.e("HOMEVIEWMODEL", "Error al obtener experiencias: ${e.message}")
            }
        }
    }
}