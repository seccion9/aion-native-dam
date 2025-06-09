package com.example.gestionreservas.viewModel.Comentarios

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.repository.ComentariosRepository
import kotlinx.coroutines.launch

class ComentariosViewModel(
    private val comentariosRepository: ComentariosRepository
) : ViewModel() {
    private val _comentarios=MutableLiveData<List<Comentario>>()
    val comentarios:LiveData<List<Comentario>> = _comentarios

    fun obtenerComentarios(token:String){
        viewModelScope.launch {
            try {
                val listaComentarios=comentariosRepository.obtenerComentariosApi(token)
                if(listaComentarios!=null){
                    _comentarios.value=listaComentarios
                    Log.e("ViewModelComentarios","Comentarios obtenidos ${listaComentarios.size}")
                }else{
                    Log.e("ViewModelComentarios","Error al obtener comentarios")
                    _comentarios.value= emptyList()
                }
            }catch (e:Exception){
                Log.e("ViewModelComentarios","Error al obtener comentarios ${e.message}")
                _comentarios.value= emptyList()
            }
        }
    }

}