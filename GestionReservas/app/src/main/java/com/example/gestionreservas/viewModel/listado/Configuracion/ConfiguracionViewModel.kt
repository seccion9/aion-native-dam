package com.example.gestionreservas.viewModel.listado.Configuracion

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gestionreservas.repository.MailingRepository

class ConfiguracionViewModel(
    private val mailingRepository: MailingRepository
):ViewModel() {

    private val _email= MutableLiveData<String?>()
    val email: MutableLiveData<String?> = _email

    fun obtenerEmailUsuario(context:Context){
        var email=mailingRepository.obtenerEmailUsuario(context)
        _email.value = email
    }
}