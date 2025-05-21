package com.example.gestionreservas.viewModel.listado.CalendarioSemana

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CalendarioRepository

class CalendarioSemanaViewModelFactory(
    private val calendarioRepository: CalendarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarioSemanaViewModel::class.java)) {
            return CalendarioSemanaViewModel(calendarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}