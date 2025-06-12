package com.example.gestionreservas.viewModel.Mailing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gestionreservas.models.entity.Jugador

class MailingViewModel : ViewModel() {
    private val _jugadores = MutableLiveData<List<Jugador>>()
    val jugadores: LiveData<List<Jugador>> = _jugadores

    fun asignarImagenAJugador(id: String, uri: String) {
        val nuevaLista = _jugadores.value?.map {
            if (it.id == id) it.copy(imagen = uri) else it
        }
        _jugadores.value = nuevaLista ?: emptyList()
    }

    fun cargarJugadoresIniciales() {
        _jugadores.value = listOf(
            Jugador("1", "Mario", "", listOf()),
            Jugador("2", "Laura", "", listOf())
        )
    }
    fun agregarJugador(jugador: Jugador) {
        val listaActual = _jugadores.value ?: emptyList()
        _jugadores.value = listaActual + jugador
    }

}
