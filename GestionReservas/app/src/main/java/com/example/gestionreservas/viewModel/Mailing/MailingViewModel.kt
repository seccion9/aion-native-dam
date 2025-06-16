package com.example.gestionreservas.viewModel.Mailing

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Jugador
import com.example.gestionreservas.models.entity.Monitor
import com.example.gestionreservas.models.entity.SesionMailing
import com.example.gestionreservas.models.repository.MailingRepository
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class MailingViewModel(private val repository: MailingRepository) : ViewModel() {

    private val _sesion = MutableLiveData<SesionMailing>()
    val sesion: LiveData<SesionMailing> = _sesion

    private val _monitores = MutableLiveData<List<Monitor>>()
    val monitores: LiveData<List<Monitor>> = _monitores

    init {
        // Inicializamos con una sesión vacía con UUID y fecha actual
        _sesion.value = SesionMailing(
            fecha = obtenerFechaActual(),
            monitor = "",
            puntuacion = 0f,
            email = "",
            jugadores = emptyList(),
            fotosBase64 = emptyList()
        )
    }

    // Añadir jugador localmente
    fun agregarJugador(jugador: Jugador) {
        val listaActual = _sesion.value?.jugadores?.toMutableList() ?: mutableListOf()
        listaActual.add(jugador)
        _sesion.value = _sesion.value?.copy(jugadores = listaActual)
    }

    // Añadir foto localmente
    fun agregarFoto(fotoBase64: String) {
        val listaActual = _sesion.value?.fotosBase64?.toMutableList() ?: mutableListOf()
        listaActual.add(fotoBase64)
        _sesion.value = _sesion.value?.copy(fotosBase64 = listaActual)
    }

    // Actualizar monitor
    fun actualizarMonitor(monitor: String) {
        _sesion.value = _sesion.value?.copy(monitor = monitor)
    }

    // Actualizar email
    fun actualizarEmail(email: String) {
        _sesion.value = _sesion.value?.copy(email = email)
    }

    // Actualizar puntuación final
    fun actualizarPuntuacion(puntuacion: Float) {
        _sesion.value = _sesion.value?.copy(puntuacion = puntuacion)
    }

    // Enviar toda la sesión a la API
    fun registrarSesion(token: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val sesionFinal = _sesion.value ?: throw Exception("Sesión inválida")
                val success = repository.registrarSesion(token, sesionFinal)
                if (success) onSuccess()
                else onError("Error al registrar sesión")
            } catch (e: Exception) {
                Log.e("MailingVM", "Error al registrar sesión", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Obtiene los monitores a traves del repository y actualiza _monitores
     */
    fun obtenerMonitores(token: String) {
        viewModelScope.launch {
            try {
                val listaMonitores = repository.obtenerMonitores(token)
                _monitores.value = listaMonitores
            } catch (e: Exception) {
                Log.e("MailingViewModel", "Error al obtener monitores ${e.message}")
                _monitores.value = emptyList()
            }
        }
    }

    /**
     * Obtiene la fecha actual
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerFechaActual(): String {
        return java.time.LocalDate.now().toString()
    }

    /**
     * Asigna una imagen en base64 a un jugador
     */
    fun asignarImagenAJugadorLocal(idJugador: String, imagenBase64: String) {
        val jugadoresActuales = _sesion.value?.jugadores?.toMutableList() ?: return

        val nuevosJugadores = jugadoresActuales.map { jugador ->
            if (jugador.id == idJugador) {
                jugador.copy(imagen = imagenBase64)
            } else jugador
        }

        _sesion.value = _sesion.value?.copy(jugadores = nuevosJugadores)
    }

    /**
     * Resetea la sesion para empezar una nueva con fotos y usuarios diferentes
     */
    fun resetearSesion() {
        _sesion.value = SesionMailing(
            fecha = obtenerFechaActual(),
            monitor = "",
            puntuacion = 0f,
            email = "",
            jugadores = emptyList(),
            fotosBase64 = emptyList()
        )
    }


}
