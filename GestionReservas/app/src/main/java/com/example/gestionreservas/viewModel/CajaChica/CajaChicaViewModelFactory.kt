package com.example.gestionreservas.viewModel.CajaChica

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CajaChicaRepository

class CajaChicaViewModelFactory(
    private val cajaChicaRepository: CajaChicaRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CajaChicaViewModel::class.java)) {
            return CajaChicaViewModel(cajaChicaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}