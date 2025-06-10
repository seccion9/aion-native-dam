package com.example.gestionreservas.viewModel.Comentarios

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.repository.ComentariosRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ComentariosViewModel(
    private val comentariosRepository: ComentariosRepository
) : ViewModel() {
    private val _comentarios = MutableLiveData<List<Comentario>>()
    val comentarios: LiveData<List<Comentario>> = _comentarios

    private var comentariosOriginales: List<Comentario> = emptyList()
    private var filtroDescripcion = ""
    private var fechaInicioFiltro: LocalDateTime? = null
    private var fechaFinFiltro: LocalDateTime? = null

    private val _comentarioEliminado = MutableLiveData<Boolean?>()
    val comentarioEliminado: LiveData<Boolean?> = _comentarioEliminado

    private val _comentarioEditado = MutableLiveData<Boolean?>()
    val comentarioEditado: LiveData<Boolean?> = _comentarioEditado

    @RequiresApi(Build.VERSION_CODES.O)
    fun actualizarFiltros(
        descripcion: String? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null
    ) {
        descripcion?.let { filtroDescripcion = it }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        fechaInicio?.let { fechaInicioFiltro = LocalDateTime.parse("$it 00:00", formatter) }
        fechaFin?.let { fechaFinFiltro = LocalDateTime.parse("$it 23:59", formatter) }

        _comentarios.value = comentariosOriginales.filter { comentario ->
            val coincideDescripcion =
                comentario.descripcion.contains(filtroDescripcion, ignoreCase = true)
            val coincideFecha = try {
                val fechaComentario = LocalDateTime.parse(comentario.fecha, formatter)
                (fechaInicioFiltro == null || fechaComentario >= fechaInicioFiltro) &&
                        (fechaFinFiltro == null || fechaComentario <= fechaFinFiltro)
            } catch (e: Exception) {
                false
            }

            coincideDescripcion && coincideFecha
        }.sortedByDescending { it.fecha }
    }

    fun eliminarComentario(token: String, comentario: Comentario) {
        viewModelScope.launch {
            try {
                val success = comentariosRepository.eliminarComentario(token, comentario)
                _comentarioEliminado.value = success
                if (success) {
                    val listaActual = _comentarios.value?.toMutableList() ?: mutableListOf()
                    val index = listaActual.indexOfFirst { it.id == comentario.id }
                    if (index != -1) {
                        listaActual.removeAt(index)
                        _comentarios.value = listaActual
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModelComentarios", "Error al borrar comentario ${e.message}")
                _comentarioEliminado.value = false
            }
        }
    }

    fun obtenerComentarios(token: String) {
        viewModelScope.launch {
            try {
                val listaComentarios = comentariosRepository.obtenerComentariosApi(token)
                if (listaComentarios != null) {
                    comentariosOriginales = listaComentarios
                    _comentarios.value = listaComentarios.sortedByDescending { it.fecha }
                    Log.e("ViewModelComentarios", "Comentarios obtenidos ${listaComentarios.size}")
                } else {
                    Log.e("ViewModelComentarios", "Error al obtener comentarios")
                    _comentarios.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ViewModelComentarios", "Error al obtener comentarios ${e.message}")
                _comentarios.value = emptyList()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filtrarPorFechas(fechaInicio: String, fechaFin: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val inicio = LocalDateTime.parse("$fechaInicio 00:00", formatter)
        val fin = LocalDateTime.parse("$fechaFin 23:59", formatter)

        val comentariosFiltrados = comentariosOriginales.filter { comentario ->
            try {
                val fechaComentario = LocalDateTime.parse(comentario.fecha, formatter)
                fechaComentario in inicio..fin
            } catch (e: Exception) {
                false
            }
        }

        _comentarios.value = comentariosFiltrados.sortedByDescending { it.fecha }
    }

    fun limpiarEstadoComentarioEliminado() {
        _comentarioEliminado.value = null
    }

    fun editarComentario(token: String, comentario: Comentario) {
        viewModelScope.launch {
            try {
                val success = comentariosRepository.editarComentario(token, comentario)
                _comentarioEditado.value = success

                if (success) {
                    val listaActual = _comentarios.value?.toMutableList() ?: mutableListOf()
                    val index = listaActual.indexOfFirst { it.id == comentario.id }

                    if (index != -1) {
                        listaActual[index] = comentario
                        _comentarios.value = listaActual
                        Log.d("ViewModelComentarios", "Comentario editado y actualizado en lista")
                    } else {
                        Log.w("ViewModelComentarios", "Comentario editado pero no encontrado en lista")
                    }
                } else {
                    Log.e("ViewModelComentarios", "Fallo al editar comentario en API")
                }
            } catch (e: Exception) {
                Log.e("ViewModelComentarios", "Error al editar comentario: ${e.message}", e)
                _comentarioEditado.value = false
            }
        }
    }


}