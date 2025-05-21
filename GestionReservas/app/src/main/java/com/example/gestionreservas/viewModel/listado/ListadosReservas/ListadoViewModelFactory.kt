package com.example.gestionreservas.viewModel.listado.ListadosReservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CompraRepository

class ListadoViewModelFactory(
    private val compraRepository: CompraRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListadoViewModel(compraRepository) as T
    }
}