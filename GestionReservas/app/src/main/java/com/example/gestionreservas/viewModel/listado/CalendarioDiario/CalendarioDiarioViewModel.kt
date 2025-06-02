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

    private var sesionesGuardadas: List<SesionConCompra> = emptyList()

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

        for (franja in listaBase) {
            val comprasDeFranja = sesionesGuardadas
                .filter { it.sesion.hora == franja.horaInicio }
                .mapNotNull { it.compra }
                .distinctBy { it.id }

            franja.reservas = comprasDeFranja
            Log.d("DEBUG_FRANJA", "Franja ${franja.horaInicio}-${franja.horaFin} tiene ${franja.reservas.size} reservas")
            franja.reservas.forEach { Log.d("DEBUG_RESERVA", "Reserva de ${it.name} en franja ${franja.horaInicio}") }
        }

        _franjasHorarias.postValue(listaBase)
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
    private fun generarHorasDelDia(): List<FranjaHorariaReservas> {
        val listaHoras = mutableListOf<FranjaHorariaReservas>()
        val formato = DateTimeFormatter.ofPattern("HH:mm")

        var horaActual = LocalTime.of(9, 0)
        val horaFin = LocalTime.of(17, 0)

        while (horaActual < horaFin) {
            val siguiente = horaActual.plusHours(1)

            listaHoras.add(
                FranjaHorariaReservas(
                    horaInicio = horaActual.format(formato),
                    horaFin = siguiente.format(formato),
                    reservas = emptyList()
                )
            )

            horaActual = siguiente
        }

        return listaHoras
    }

    fun mapearCalendarioASala(calId: String): String {
        return when (calId) {
            "cal1" -> "sala1"
            "cal2" -> "sala2"
            "cal3" -> "sala3"
            "cal4" -> "sala4"
            "cal5" -> "sala5"
            "cal6" -> "sala6"
            "cal7" -> "sala7"
            "cal8" -> "sala8"
            else -> calId
        }
    }

}
