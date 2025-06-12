package com.example.gestionreservas.viewModel.Email

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.model.CorreoItem
import com.example.gestionreservas.models.entity.TokenResponse
import com.example.gestionreservas.repository.MailingRepository
import kotlinx.coroutines.launch

class EmailViewModel(
    private val mailingRepository: MailingRepository
): ViewModel() {

    private val _sesionCerrada = MutableLiveData<Boolean>(false)
    val sesionCerrada: LiveData<Boolean> get() = _sesionCerrada


    private val _listaCorreos = MutableLiveData<List<CorreoItem>>()
    val listaCorreos : LiveData<List<CorreoItem>> = _listaCorreos

    private val _tokenResponse = MutableLiveData<TokenResponse?>()
    val tokenResponse: MutableLiveData<TokenResponse?> get() = _tokenResponse

    private var siguienteToken: String? = null

    private val _cargando = MutableLiveData<Boolean>(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _modoCargaInicial = MutableLiveData<Boolean>()
    val modoCargaInicial: LiveData<Boolean> get() = _modoCargaInicial

    private val _mensajeSeleccionado = MutableLiveData<CorreoItem?>()
    val mensajeSeleccionado: LiveData<CorreoItem?> get() = _mensajeSeleccionado


    fun obtenerDetalleCorreo(idMensaje: String,context: Context) {
        viewModelScope.launch {
            try {
                val token = mailingRepository.obtenerTokenGuardado(context)
                Log.d("MailingViewModel", "Token usado: ${token?.access_token}")
                Log.d("MailingViewModel", "ID del mensaje: $idMensaje")

                val mensajeDetallado = mailingRepository.getMensajeDetalle(token!!, idMensaje)
                val mensajeDepurado = mailingRepository.depurarMensajeParaObtenerDetalles(mensajeDetallado)
                _mensajeSeleccionado.value = mensajeDepurado
            } catch (e: Exception) {
                Log.e("MailingViewModel", "Error detalle correo: ${e.message}", e)
            }
        }
    }

    fun cargarPaginaSiguiente(token: TokenResponse) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val (nuevosCorreos, nuevoToken) = mailingRepository.obtenerMensajesPagina(token, siguienteToken)
                val listaActual = _listaCorreos.value ?: emptyList()
                _listaCorreos.value = listaActual + nuevosCorreos
                siguienteToken = nuevoToken
            } catch (e: Exception) {
                Log.e("MailingViewModel", "Error cargando página: ${e.message}")
            } finally {
                _cargando.value = false
            }
        }
    }
    fun hacerLoginYobtenerCorreos(authCode: String, context: Context) {
        viewModelScope.launch {
            _modoCargaInicial.value = true
            _cargando.value = true

            try {
                _sesionCerrada.value = false
                val lista = mailingRepository.loginYObtenerCorreos(authCode, context)
                _listaCorreos.value = lista
                _tokenResponse.value = mailingRepository.obtenerTokenGuardado(context)
                guardarSesionActiva(context, true)

            } catch (e: Exception) {
                Log.e("MailingViewModel", "Error iniciando sesión: ${e.message}")
                _listaCorreos.value = emptyList()
            } finally {
                _cargando.value = false
                _modoCargaInicial.value = false

            }
        }
    }
    fun recargarCorreosDesdeToken(token: TokenResponse, esInicial: Boolean = false) {
        viewModelScope.launch {
            try {
                _modoCargaInicial.value = esInicial

                _cargando.value = true

                val (correos, nuevoToken) = mailingRepository.obtenerMensajesPagina(token, null)
                _listaCorreos.value = correos
                siguienteToken = nuevoToken
            } catch (e: Exception) {
                Log.e("MailingViewModel", "Error al recargar correos: ${e.message}")
            }finally {
                _cargando.value = false
                _modoCargaInicial.value = false

            }
        }
    }
    fun logout(context: Context) {
        viewModelScope.launch {
            try {
                mailingRepository.cerrarSesion(context) {
                    _sesionCerrada.value = true
                }
                guardarSesionActiva(context, false)

            } catch (e: Exception) {
                Log.e("MailingViewModel", "Error cerrando sesión: ${e.message}")
                _sesionCerrada.value = false
            }
        }
    }
    private fun guardarSesionActiva(context: Context, activa: Boolean) {
        val prefs = context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("sesion_activa", activa).apply()
    }

    fun resetearEstadoSesionCerrada() {
        _sesionCerrada.value = false
    }
    fun resetToken() {
        _tokenResponse.value = null
    }
    fun limpiarMensajeSeleccionado() {
        _mensajeSeleccionado.value = null
    }

}
