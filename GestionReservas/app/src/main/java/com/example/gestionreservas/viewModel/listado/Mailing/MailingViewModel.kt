package com.example.gestionreservas.viewModel.listado.Mailing

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.TokenResponse
import com.example.gestionreservas.repository.MailingRepository
import kotlinx.coroutines.launch

class MailingViewModel(
    private val mailingRepository: MailingRepository
): ViewModel() {

    private val _sesionCerrada = MutableLiveData<Boolean>()
    val sesionCerrada: LiveData<Boolean> get() = _sesionCerrada

    /*suspend fun loginYGuardarToken(authCode: String, context: Context): TokenResponse {
        return mailingRepository.loginYGuardarToken(authCode, context)
    }*/

    fun logout(context: Context) {
        viewModelScope.launch {
            try {
                mailingRepository.cerrarSesion(context) {
                    _sesionCerrada.value = true
                }
            } catch (e: Exception) {
                Log.e("MailingViewModel", "Error cerrando sesión: ${e.message}")
                _sesionCerrada.value = false
            }
        }
    }

    fun resetearEstadoSesionCerrada() {
        _sesionCerrada.value = false
    }
}
