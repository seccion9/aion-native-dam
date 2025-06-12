package com.example.gestionreservas.viewModel.Email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.repository.MailingRepository


class EmailViewModelFactory(
    private val mailingRepository: MailingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmailViewModel(mailingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


