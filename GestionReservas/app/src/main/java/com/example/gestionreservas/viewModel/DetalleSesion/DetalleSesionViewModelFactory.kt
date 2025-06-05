package com.example.gestionreservas.viewModel.DetalleSesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.gestionreservas.models.repository.CompraRepository

/**
 * Encargado de crear objetos de tipo DetalleSesionViewModel
 */
class DetalleSesionViewModelFactory (
    private val repo: CompraRepository
):ViewModelProvider.Factory{
    override fun <T:ViewModel> create(modelClass: Class<T>):T{
        return DetalleSesionViewModel(repo) as T
    }
}