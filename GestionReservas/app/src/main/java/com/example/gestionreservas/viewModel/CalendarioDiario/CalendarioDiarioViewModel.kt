package com.example.gestionreservas.viewModel.CalendarioDiario

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Bloqueo
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.EstadoSala
import com.example.gestionreservas.models.entity.FranjaHorariaReservas
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.entity.SalaConEstado
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.models.repository.CompraRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CalendarioDiarioViewModel(
    private val compraRepository : CompraRepository,
    private val calendarioRepository : CalendarioRepository
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _fechaActual = MutableLiveData<LocalDate>(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val fechaActual: LiveData<LocalDate> = _fechaActual

    private val _franjasHorarias = MutableLiveData<List<FranjaHorariaReservas>>()
    val franjasHorarias: LiveData<List<FranjaHorariaReservas>> = _franjasHorarias


    private val _ocupaciones = MutableLiveData<List<Ocupacion>>()
    val ocupaciones: LiveData<List<Ocupacion>> = _ocupaciones

    private val _bloqueoExitoso = MutableLiveData<Boolean>()
    val bloqueoExitoso: LiveData<Boolean> = _bloqueoExitoso

    private val _bloqueos = MutableLiveData<List<Bloqueo>?>()
    val bloqueos: LiveData<List<Bloqueo>?> = _bloqueos

    private var sesionesGuardadas: List<SesionConCompra> = emptyList()

    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarBloqueo(token:String, bloqueo:Bloqueo){
        viewModelScope.launch {
            try {
                val success = calendarioRepository.bloquearFechas("Bearer $token", bloqueo)
                _bloqueoExitoso.postValue(success)
                if (success) {
                    val listaBloqueos = calendarioRepository.obtenerBloqueos("Bearer $token")
                    _bloqueos.postValue(listaBloqueos)

                    actualizarFranjasConOcupaciones()
                }
            } catch (e: Exception) {
                Log.e("ViewModelBloqueo", "Error al bloquear: ${e.message}")
                _bloqueoExitoso.postValue(false)
            }
        }
    }
    fun obtenerBloqueos(token:String){
        viewModelScope.launch {
            try{
                val listaBloqueos=calendarioRepository.obtenerBloqueos("Bearer $token")
                if(listaBloqueos!=null){
                    _bloqueos.value=listaBloqueos
                }
            }catch (e:Exception){
                Log.e("ViewModelBloqueo", "Error al obtener bloqueos: ${e.message}")
                _bloqueos.value= emptyList()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun avanzarDia() {
        _fechaActual.value = _fechaActual.value?.plusDays(1)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun retrocederDia() {
        _fechaActual.value = _fechaActual.value?.minusDays(1)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun irAHoy() {
        _fechaActual.value = LocalDate.now()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun seleccionarFecha(fecha: LocalDate) {
        _fechaActual.value = fecha
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarSesionesDesdeMock(token: String, fecha: LocalDate) {
        viewModelScope.launch {
            try {
                val sesiones = compraRepository.obtenerSesionesDelDia(token, fecha)
                sesionesGuardadas = sesiones


                val listaOcupaciones = sesiones.mapNotNull { sc ->
                    sc.compra?.let { compra ->
                        val item = compra.items.firstOrNull()
                        if (item != null) {
                            Ocupacion(
                                experienciaId = item.idExperience,
                                salas = item.salas ?: emptyList(),
                                calendarioId = sc.sesion.calendario,
                                date = fecha.toString(),
                                start = sc.sesion.hora,
                                end = calcularHoraFin(sc.sesion.hora),
                                idCompra = compra.id
                            )
                        } else null
                    }
                }
                Log.e("LISTAOCUPACIONES",listaOcupaciones.toString())
                _ocupaciones.value = listaOcupaciones
                actualizarFranjasConOcupaciones()



            } catch (e: Exception) {
                try {
                    val fallback = calendarioRepository.obtenerOcupacionesDelDia(
                        token,
                        fecha.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    )

                    _ocupaciones.postValue(fallback)
                } catch (fallbackError: Exception) {
                    Log.e("Error", fallbackError.message.toString())
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun actualizarFranjasConOcupaciones() {
        val listaBase = generarHorasDelDia().toMutableList()
        val totalSalas = 8

        for (franja in listaBase) {
            // Inicializa todas las salas como LIBRES
            val salasDeFranja = MutableList(totalSalas) { index ->
                SalaConEstado(
                    idSala = "sala${index + 1}",
                    estado = EstadoSala.LIBRE,
                    reserva = null
                )
            }

            // Añadir ocupaciones reales
            val ocupacionesDeFranja = _ocupaciones.value?.filter {
                it.start == franja.horaInicio
            } ?: emptyList()

            for (ocupacion in ocupacionesDeFranja) {
                val compraAsociada = sesionesGuardadas
                    .firstOrNull { it.compra?.id == ocupacion.idCompra }
                    ?.compra

                for (salaId in ocupacion.salas!!) {
                    val index = salaId.removePrefix("cal").toIntOrNull()?.minus(1)
                    Log.d("DEBUG_SALA", "Asignando ${salaId} a index $index para la franja ${franja.horaInicio}")
                    if (index != null && index in 0 until totalSalas) {
                        salasDeFranja[index] = SalaConEstado(
                            idSala = salaId,
                            estado = EstadoSala.RESERVADA,
                            reserva = compraAsociada
                        )
                    }
                }
            }
            val bloqueosDelDia = _bloqueos.value?.filter { bloqueo ->
                val tipo = bloqueo.tipo.lowercase()
                val fechaFranja = fechaActual.value?.toString() ?: return@filter false

                when (tipo) {
                    "dia_entero" -> {
                        // Si la franja coincide con el día del bloqueo
                        bloqueo.inicio == fechaFranja
                    }
                    "franja" -> {
                        try {
                            val formatoFechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                            val inicio = LocalDateTime.parse(bloqueo.inicio, formatoFechaHora)
                            val fin = LocalDateTime.parse(bloqueo.fin, formatoFechaHora)

                            val horaFranja = LocalTime.parse(franja.horaInicio, DateTimeFormatter.ofPattern("HH:mm"))
                            val fechaFranjaCompleta = LocalDateTime.of(_fechaActual.value, horaFranja)

                            // Si la franja está dentro del rango de bloqueo
                            fechaFranjaCompleta in inicio..fin
                        } catch (e: Exception) {
                            false
                        }
                    }
                    else -> false
                }
            } ?: emptyList()

        // Aplicar bloqueos a salas de la franja
            for (bloqueo in bloqueosDelDia) {
                for (salaBloqueada in bloqueo.salas) {
                    val idNormalizado = salaBloqueada.lowercase().replace(" ", "")
                    val index = idNormalizado.removePrefix("sala").toIntOrNull()?.minus(1)
                    if (index != null && index in 0 until totalSalas) {
                        salasDeFranja[index] = salasDeFranja[index].copy(estado = EstadoSala.BLOQUEADA)
                    }
                }
            }


            // Asignar la lista final de salas a la franja
            franja.salas = salasDeFranja
        }

        _franjasHorarias.postValue(listaBase)
    }


    fun calcularHoraFin(horaInicio: String): String {
        val partes = horaInicio.split(":")
        val hora = partes[0].toInt()
        val minutos = partes[1].toInt()
        return String.format("%02d:%02d", (hora + 1) % 24, minutos)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generarHorasDelDia(): List<FranjaHorariaReservas> {
        val listaHoras = mutableListOf<FranjaHorariaReservas>()
        val formato = DateTimeFormatter.ofPattern("HH:mm")

        var horaActual = LocalTime.of(9, 0)
        val horaFin = LocalTime.of(20, 0)

        while (horaActual < horaFin) {
            val siguiente = horaActual.plusHours(1)

            listaHoras.add(
                FranjaHorariaReservas(
                    horaInicio = horaActual.format(formato),
                    horaFin = siguiente.format(formato),
                    salas = emptyList()
                )
            )

            horaActual = siguiente
        }

        return listaHoras
    }


}
