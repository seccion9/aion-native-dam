package com.example.gestionreservas.viewModel.listado.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.models.repository.ExperienciaRepository

class HomeViewModelFactory(
    private val compraRepository: CompraRepository,
    private val cajaChicaRepository: CajaChicaRepository,
    private val experienciaRepository: ExperienciaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(compraRepository, cajaChicaRepository,experienciaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
