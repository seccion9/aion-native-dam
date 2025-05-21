package com.example.gestionreservas.viewModel.listado.ListadosReservas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate



class ListadoViewModel(
    private val compraRepository: CompraRepository
) : ViewModel() {
    private val _sesiones = MutableStateFlow<List<SesionConCompra>>(emptyList())
    val sesiones: StateFlow<List<SesionConCompra>> get() = _sesiones

    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarSesiones(token: String, fecha: LocalDate, idExperience: String? = null) {
        viewModelScope.launch {
            try {
                val lista = compraRepository.obtenerSesionesDelDia(token, fecha)

                _sesiones.value = if (!idExperience.isNullOrEmpty()) {
                    lista.filter { it.sesion.calendario == idExperience }

                } else {
                    lista
                }

            } catch (e: Exception) {
                println("Error cargando sesiones: ${e.localizedMessage}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarSesionesSemana(token: String, fechaInicio: LocalDate, fechaFin: LocalDate, calendario: String? = null) {
        viewModelScope.launch {
            try {
                val compras = compraRepository.obtenerCompras(token)
                val sesiones = compraRepository.obtenerSesionesDeSemana(compras, fechaInicio)

                val sesionesFiltradas = if (calendario != null) {
                    sesiones.filter { it.sesion.calendario == calendario }
                } else {
                    sesiones
                }

                val ordenadas = sesionesFiltradas.sortedBy {
                    java.time.LocalTime.parse(it.sesion.hora)
                }

                _sesiones.value = ordenadas

            } catch (e: Exception) {
                println("Error cargando sesiones semanales: ${e.localizedMessage}")
            }
        }
    }

}