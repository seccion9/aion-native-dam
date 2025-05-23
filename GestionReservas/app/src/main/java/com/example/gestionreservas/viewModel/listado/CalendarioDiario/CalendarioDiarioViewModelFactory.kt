package com.example.gestionreservas.viewModel.listado.CalendarioDiario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.models.repository.CompraRepository

class CalendarioDiarioViewModelFactory(
    private val compraRepository :CompraRepository,
    private val calendarioRepository :CalendarioRepository,
) :ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarioDiarioViewModel::class.java)) {
            return CalendarioDiarioViewModel(compraRepository,calendarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}