package com.example.gestionreservas.viewModel.Mailing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.models.repository.ExperienciaRepository
import com.example.gestionreservas.models.repository.MailingRepository
import com.example.gestionreservas.viewModel.ListadosReservas.ListadoViewModel

class MailingViewModelFactory (
    private val repository: MailingRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MailingViewModel(repository) as T
        }
}