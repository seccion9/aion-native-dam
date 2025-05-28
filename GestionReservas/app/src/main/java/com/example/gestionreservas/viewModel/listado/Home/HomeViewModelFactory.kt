package com.example.gestionreservas.viewModel.listado.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository

class HomeViewModelFactory(
    private val compraRepository: CompraRepository,
    private val cajaChicaRepository: CajaChicaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(compraRepository, cajaChicaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
