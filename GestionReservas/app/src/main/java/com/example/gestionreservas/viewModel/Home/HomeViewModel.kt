package com.example.gestionreservas.viewModel.Home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.entity.ExperienciaCompleta
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.models.repository.ExperienciaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class HomeViewModel(
    private val compraRepository: CompraRepository,
    private val cajaChicaRepository: CajaChicaRepository,
    private val experienciaRepository: ExperienciaRepository
):ViewModel() {

    private val _sesiones=MutableLiveData<List<SesionConCompra>>()
    val sesiones: LiveData<List<SesionConCompra>> = _sesiones

    private val _experiencias=MutableLiveData<List<ExperienciaCompleta>>()
    val experiencias: LiveData<List<ExperienciaCompleta>> = _experiencias

    private val _pagos = MutableLiveData<List<PagoCaja>>()
    val pagos: LiveData<List<PagoCaja>> = _pagos

    private val _comentarios = MutableLiveData<List<Comentario>>()
    val comentarios: LiveData<List<Comentario>> = _comentarios


    private val _comentarioEnviado = MutableLiveData<Boolean?>()
    val comentarioEnviado: LiveData<Boolean?> = _comentarioEnviado

    private val _mensajeUsuario = MutableLiveData<String>()
    val mensajeUsuario: LiveData<String> = _mensajeUsuario

    @RequiresApi(Build.VERSION_CODES.O)
    private val _fechaActual = MutableLiveData(LocalDate.now())
    @RequiresApi(Build.VERSION_CODES.O)
    val fechaActual: LiveData<LocalDate> = _fechaActual

    @RequiresApi(Build.VERSION_CODES.O)
    fun actualizarFecha(nuevaFecha: LocalDate) {
        _fechaActual.value = nuevaFecha
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun agregarPagoCajaChica(token: String,concepto: String, cantidad: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (concepto.isBlank() || cantidad.isBlank()) {
            onError("Rellena todos los campos antes de añadir")
            return
        }

        val fecha = fechaActual.value ?: LocalDate.now()
        val fechaFormateada = fecha.toString()


        val nuevoPago = PagoCaja(
            fecha = fechaFormateada, concepto = concepto, cantidad = cantidad, tipo = "Efectivo",
            id = UUID.randomUUID().toString(),
            parcial = ""
        )

        viewModelScope.launch {
            try {
                val exito = cajaChicaRepository.registrarPagoCajaChica(token, nuevoPago)
                if (exito) {
                    val actual = _pagos.value?.toMutableList() ?: mutableListOf()
                    actual.add(0, nuevoPago)
                    _pagos.postValue(actual.take(5))
                    onSuccess()
                } else {
                    onError("Error al guardar el pago")
                }
            }catch (e:Exception){
                Log.e("RegistroPago","Error al registrar pago : ${e.message}")
                onError("Error al registrar el pago: ${e.message}")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerComentarios(token:String, fechaActual:LocalDate){
        viewModelScope.launch {
            try {
                val comentarios=compraRepository.obtenerComentariosUsuario(token)
                if (comentarios != null) {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val comentariosFiltrados = comentarios
                        .filter {
                            val fechaComentario = LocalDateTime.parse(it.fecha, formatter).toLocalDate()
                            fechaComentario == fechaActual
                        }
                        .sortedByDescending { LocalDateTime.parse(it.fecha, formatter) }
                        .take(5)
                    _comentarios.value = comentariosFiltrados.toList()
                    //_comentarios.postValue(ArrayList(comentariosFiltrados))

                }
            }catch (e:Exception){
                Log.e("HOMEVIEWMODEL","Error al cargar comentarios: ${e.message}")
            }
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarDatosDesdeJsonServer(token:String, fecha: LocalDate){
        viewModelScope.launch {
            try{
                val compras = cajaChicaRepository.obtenerCompras(token)
                val pagosApi:List<PagoCaja>
                Log.d("HomeViewModel", "Compras obtenidas: ${compras.size}")
                val sesionesFiltradas = withContext(Dispatchers.Default) {
                    compraRepository.transformarComprasASesiones(compras, fecha)
                        .filter { it.sesion.estado.lowercase() == "confirmada" }.sortedBy { it.sesion.hora }
                }
                Log.d("HomeViewModel", "Sesiones confirmadas: ${sesionesFiltradas.size}")

                val pagosCaja = withContext(Dispatchers.IO) {
                    cajaChicaRepository.obtenerPagosDelDia(token, fecha.toString())
                }
                Log.d("HomeViewModel", "Pagos de caja obtenidos: $pagosCaja")

                _sesiones.value = sesionesFiltradas
                _pagos.value = pagosCaja
            }catch (e:Exception){
                Log.e("HOMEVIEWMODEL","Error al cargar sesiones: ${e.message}")
            }
        }
    }

    fun obtenerExperiencias(token: String) {
        viewModelScope.launch {
            try {
                val experienciasObtenidas = withContext(Dispatchers.IO) {
                    experienciaRepository.obtenerExperienciasApi(token)
                }
                experienciasObtenidas?.let {
                    _experiencias.value = it
                } ?: Log.e("HOMEVIEWMODEL", "Lista de experiencias es nula")
            } catch (e: Exception) {
                Log.e("HOMEVIEWMODEL", "Error al obtener experiencias: ${e.message}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun enviarComentario(token: String, tipo: String, texto: String, nombreUsuario: String, fecha: LocalDateTime) {
        viewModelScope.launch {
            try {
                val fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                val comentario = Comentario(
                    id = UUID.randomUUID().toString(),
                    tipo = tipo,
                    fecha = fechaFormateada,
                    nombreUsuario = nombreUsuario,
                    descripcion = texto
                )
                val success = compraRepository.registrarComentarioAPI(token, comentario)
                if (success) {
                    _comentarioEnviado.value = true
                    delay(500)
                    obtenerComentarios(token, fecha.toLocalDate())
                } else {
                    _mensajeUsuario.value = "Error al enviar comentario"
                    _comentarioEnviado.value = false
                }
            } catch (e: Exception) {
                Log.e("HOMEVIEWMODEL", "Error al enviar comentario: ${e.message}")
                _mensajeUsuario.value = "Error inesperado"
                _comentarioEnviado.value = false
            }
        }
    }

    fun limpiarEstadoComentario() {
        _comentarioEnviado.value = null
    }
}