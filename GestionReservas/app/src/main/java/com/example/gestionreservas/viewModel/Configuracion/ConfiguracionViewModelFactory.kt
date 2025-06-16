package com.example.gestionreservas.viewModel.Configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.repository.EmailRepository

class ConfiguracionViewModelFactory(
    private val mailingRepository:EmailRepository
):ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>):T{
        return ConfiguracionViewModel(mailingRepository) as T
    }
}