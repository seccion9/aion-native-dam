package com.example.gestionreservas.viewModel.listado.CalendarioDiario

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
import com.example.gestionreservas.models.entity.HoraReserva
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.models.repository.CompraRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
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

    private val _horas = MutableLiveData<List<HoraReserva>>()
    val horas: LiveData<List<HoraReserva>> = _horas

    private val _sesiones = MutableLiveData<List<SesionConCompra>>()
    val sesiones: LiveData<List<SesionConCompra>> = _sesiones

    private val _ocupaciones = MutableLiveData<List<Ocupacion>>()
    val ocupaciones: LiveData<List<Ocupacion>> = _ocupaciones

    private val _bloqueoExitoso = MutableLiveData<Boolean>()
    val bloqueoExitoso: LiveData<Boolean> = _bloqueoExitoso


    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerSesionConCompraDesdeOcupacion(token: String, hora: String, calendarioId: String, ocupacion: Ocupacion?): LiveData<SesionConCompra> {
        val resultado = MutableLiveData<SesionConCompra>()
        viewModelScope.launch {
            try {
                val compra = if (ocupacion != null) {
                    val listaCompras = compraRepository.obtenerCompras(token)
                    listaCompras.find { it.id == ocupacion.idCompra }
                } else null

                val sesion = if (ocupacion != null && compra != null) {
                    transformarItemSesioncompra(compra, ocupacion)
                } else {
                    Sesion(
                        hora = hora,
                        calendario = calendarioId,
                        nombre = "",
                        participantes = 0,
                        totalPagado = 0.0,
                        estado = "sin_reserva",
                        idiomas = ""
                    )
                }

                resultado.postValue(SesionConCompra(sesion, compra))
            } catch (e: Exception) {
                Log.e("ViewModel", "Error obteniendo sesión con compra: ${e.message}")
            }
        }
        return resultado
    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun bloquearYEliminar(context: Context, token: String, bloqueos: List<Bloqueo>): Boolean {
        val success = calendarioRepository.bloquearFechas(token, bloqueos)
        _bloqueoExitoso.postValue(success)

        if (success) {
            val fechas = bloqueos.map { it.fecha }.distinct()
            for (fechaStr in fechas) {
                val fecha = LocalDate.parse(fechaStr)
                borrarComprasTrasBloqueo(token, fecha)
            }
        }

        return success
    }

    fun obtenerOcupacion(hora: String, calendarioId: String): Ocupacion? {
        return _ocupaciones.value?.find {
            it.start == hora && it.calendarioId == calendarioId
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

    fun transformarItemSesioncompra(compra: Compra, ocupacion: Ocupacion): Sesion {
        val item = compra.items.firstOrNull { it.id == ocupacion.idCompra } ?: compra.items.first()
        return Sesion(
            hora = item.start.substring(11, 16),
            calendario = item.idCalendario,
            nombre = compra.name,
            participantes = item.peopleNumber,
            totalPagado = item.priceTotal,
            estado = compra.status,
            idiomas = compra.language
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarSesionesDesdeMock(token: String, fecha: LocalDate) {
        viewModelScope.launch {
            try {
                val sesiones = compraRepository.obtenerSesionesDelDia(token, fecha)
                _sesiones.postValue(sesiones)
                val bloqueos = calendarioRepository.obtenerBloqueos("Bearer $token")
                    ?.filter { it.fecha == fecha.toString() }
                Log.e("BLOQUEOS_OBTENIDOS","$token")
                val bloqueosPorDiaSala = bloqueos?.groupBy { it.fecha to it.calendarioId }


                val listaOcupaciones = sesiones.mapNotNull { sc ->
                    sc.compra?.let { compra ->
                        val item = compra.items.firstOrNull()
                        if (item != null) {
                            Ocupacion(
                                experienciaId = item.idExperience,
                                calendarioId = sc.sesion.calendario,
                                date = fecha.toString(),
                                start = sc.sesion.hora,
                                end = calcularHoraFin(sc.sesion.hora),
                                idCompra = compra.id
                            )
                        } else null
                    }
                }

                prepararListaReservas(listaOcupaciones, bloqueos ?: emptyList())
                _ocupaciones.postValue(listaOcupaciones)

            } catch (e: Exception) {
                try {
                    val fallback = calendarioRepository.obtenerOcupacionesDelDia(
                        token,
                        fecha.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    )
                    val listaHoras = calendarioRepository.transformarOcupacionesAHoraReserva(fallback)
                    _horas.postValue(listaHoras)
                    _ocupaciones.postValue(fallback)
                } catch (fallbackError: Exception) {
                    Log.e("Error", fallbackError.message.toString())
                }
            }
        }
    }

    suspend fun borrarComprasTrasBloqueo(token: String, fecha: LocalDate) {
            val ocupaciones = _ocupaciones.value ?: return
        val bloqueos = calendarioRepository.obtenerBloqueos("Bearer $token")
                ?.filter { it.fecha == fecha.toString() }

            val bloqueosPorSala = bloqueos?.groupBy { it.calendarioId }

            val ocupacionesAEliminar = ocupaciones.filter {
                bloqueosPorSala?.containsKey(it.calendarioId) == true
            }

            val idsAEliminar = ocupacionesAEliminar.map { it.idCompra }.distinct()

            for (id in idsAEliminar) {
                val ok = compraRepository.eliminarCompra(token, id)
                if (!ok) Log.e("CANCELAR", "Error al eliminar compra $id")
            }

            Log.d("CANCELAR", "Reservas eliminadas: $idsAEliminar")

    }


    fun calcularHoraFin(horaInicio: String): String {
        val partes = horaInicio.split(":")
        val hora = partes[0].toInt()
        val minutos = partes[1].toInt()
        return String.format("%02d:%02d", (hora + 1) % 24, minutos)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun prepararListaReservas(ocupaciones: List<Ocupacion>, bloqueos: List<Bloqueo>) {
        val listaBase = generarHorasDelDia()

        listaBase.forEach { hora ->
            // Comprobar si hay bloqueos por sala
            bloqueos.forEach { bloqueo ->
                if (bloqueo.fecha == _fechaActual.value.toString()) { // Comparación por fecha
                    when (bloqueo.calendarioId) {
                        "cal1" -> {
                            hora.sala1Libre = false
                            hora.sala1Bloqueada = true
                        }
                        "cal2" -> {
                            hora.sala2Libre = false
                            hora.sala2Bloqueada = true
                        }
                        "ambas" -> {
                            hora.sala1Libre = false
                            hora.sala2Libre = false
                            hora.sala1Bloqueada = true
                            hora.sala2Bloqueada = true
                        }
                    }
                }
            }

            // Comprobar si hay ocupaciones (reservas reales)
            ocupaciones.forEach { ocupacion ->
                if (ocupacion.coincideCon(hora)) {
                    when (ocupacion.obtenerSala()) {
                        "sala1" -> hora.sala1Libre = false
                        "sala2" -> hora.sala2Libre = false
                    }
                }
            }
        }

        _horas.postValue(listaBase)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun generarHorasDelDia(): List<HoraReserva> {
        val listaHoras = mutableListOf<HoraReserva>()
        val formato = DateTimeFormatter.ofPattern("HH:mm")

        var horaActual = LocalTime.of(9, 0)
        val horaFin = LocalTime.of(17, 0)

        while (horaActual < horaFin) {
            val siguiente = horaActual.plusHours(1)
            listaHoras.add(
                HoraReserva(
                    horaInicio = horaActual.format(formato),
                    horaFin = siguiente.format(formato),
                    sala1Libre = true,    // por defecto libre
                    sala2Libre = true
                )
            )
            horaActual = siguiente
        }

        return listaHoras
    }
    fun Ocupacion.obtenerSala(): String {
        return when (this.calendarioId) {
            "cal1" -> "sala1"
            "cal2" -> "sala2"
            else -> "desconocida"
        }
    }

    fun Ocupacion.coincideCon(hora: HoraReserva): Boolean {
        return this.start == hora.horaInicio
    }
}
