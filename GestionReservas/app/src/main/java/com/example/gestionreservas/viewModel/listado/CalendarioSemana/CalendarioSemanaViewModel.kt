package com.example.gestionreservas.viewModel.listado.CalendarioSemana

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.DiaSemana
import com.example.gestionreservas.models.entity.HoraReserva
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.repository.CalendarioRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarioSemanaViewModel(
    private val calendarioRepository: CalendarioRepository
): ViewModel() {

    // Fecha actual del lunes de la semana
    private val _fechaLunesActual = MutableLiveData<LocalDate>()
    val fechaLunesActual: LiveData<LocalDate> = _fechaLunesActual

    private val _textoResumenSemana = MutableLiveData<String>()
    val textoResumenSemana: LiveData<String> = _textoResumenSemana

    // Lista de días con ocupación
    private val _diasSemana = MutableLiveData<List<DiaSemana>>()
    val diasSemana: LiveData<List<DiaSemana>> = _diasSemana

    @RequiresApi(Build.VERSION_CODES.O)
    fun inicializarSemana(){
        val hoy=LocalDate.now()
        val diaSemana = hoy.dayOfWeek.value
        val lunes = hoy.minusDays((diaSemana - 1).toLong())
        _fechaLunesActual.value = lunes
        actualizarTextoResumen()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun actualizarTextoResumen(){
        val lunes = _fechaLunesActual.value ?: return
        val domingo = lunes.plusDays(6)

        val mesLunes = lunes.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
        val mesDomingo = domingo.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }

        _textoResumenSemana.value = if (mesLunes == mesDomingo) {
            "${lunes.dayOfMonth} - ${domingo.dayOfMonth} $mesLunes"
        } else {
            "${lunes.dayOfMonth} $mesLunes - ${domingo.dayOfMonth} $mesDomingo"
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun avanzarSemana() {
        _fechaLunesActual.value = _fechaLunesActual.value?.plusDays(7)
        actualizarTextoResumen()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun retrocederSemana() {
        _fechaLunesActual.value = _fechaLunesActual.value?.minusDays(7)
        actualizarTextoResumen()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarDiasSemana(token: String, idsSalas: List<Int>){
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaInicio = _fechaLunesActual.value?.format(formatter) ?: return
        val fechaFin = _fechaLunesActual.value?.plusDays(6)?.format(formatter) ?: return

        viewModelScope.launch {
            try{
                val mapa=calendarioRepository.obtenerOcupacionSemanalFake(token,idsSalas,fechaInicio, fechaFin)
                val dias=calendarioRepository.transformarMapaADiasSemana(mapa,_fechaLunesActual.value!!)
                _diasSemana.value=dias
            }catch (e:Exception){
            }
        }
    }
}