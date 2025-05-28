package com.example.gestionreservas.viewModel.listado.Home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
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
    private val cajaChicaRepository: CajaChicaRepository
):ViewModel() {

    private val _sesiones=MutableLiveData<List<SesionConCompra>>()
    val sesiones: LiveData<List<SesionConCompra>> = _sesiones

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


        val nuevoPago = PagoCaja(fechaFormateada, concepto, cantidad, "Manual", "")

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

                Log.d("HomeViewModel", "Compras obtenidas: ${compras.size}")
                val sesionesFiltradas = withContext(Dispatchers.Default) {
                    compraRepository.transformarComprasASesiones(compras, fecha)
                        .filter { it.sesion.estado.lowercase() == "confirmada" }
                }
                Log.d("HomeViewModel", "Sesiones confirmadas: ${sesionesFiltradas.size}")
                val comprasConfirmadas = sesionesFiltradas.mapNotNull { it.compra }

                val pagosReservas = withContext(Dispatchers.Default) {
                    cajaChicaRepository.transformarComprasAPagos(comprasConfirmadas, fecha)
                }
                val pagosCaja = withContext(Dispatchers.IO) {
                    val pagosApi = cajaChicaRepository.obtenerPagosDelDia(token, fecha.toString())
                    cajaChicaRepository.transformarPagosCajaApi(pagosApi)
                }
                _sesiones.value = sesionesFiltradas
                _pagos.value = (pagosReservas + pagosCaja).take(5)
            }catch (e:Exception){
                Log.e("HOMEVIEWMODEL","Error al cargar sesiones: ${e.message}")
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