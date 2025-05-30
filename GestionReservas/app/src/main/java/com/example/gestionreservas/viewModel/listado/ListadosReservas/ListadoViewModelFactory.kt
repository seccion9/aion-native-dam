package com.example.gestionreservas.viewModel.listado.ListadosReservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.models.repository.ExperienciaRepository

class ListadoViewModelFactory(
    private val compraRepository: CompraRepository,
    private val experienciaRepository: ExperienciaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListadoViewModel(compraRepository,experienciaRepository) as T
    }
}