package com.example.gestionreservas.viewModel.Comentarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.models.repository.ComentariosRepository

class ComentariosViewModelFactory(
    private val comentariosRepository: ComentariosRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComentariosViewModel::class.java)) {
            return ComentariosViewModel(comentariosRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
