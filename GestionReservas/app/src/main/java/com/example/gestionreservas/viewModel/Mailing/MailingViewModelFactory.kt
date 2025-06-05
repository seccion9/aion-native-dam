package com.example.gestionreservas.viewModel.Mailing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.repository.MailingRepository


class MailingViewModelFactory(
    private val mailingRepository: MailingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MailingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MailingViewModel(mailingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


