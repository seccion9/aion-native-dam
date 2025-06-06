package com.example.gestionreservas.viewModel.Pagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository

class PagosViewModelFactory (
private val cajaChicaRepository: CajaChicaRepository,
    private val compraRepository: CompraRepository
    ) : ViewModelProvider.Factory{

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PagosViewModel::class.java)) {
                return PagosViewModel(cajaChicaRepository,compraRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
