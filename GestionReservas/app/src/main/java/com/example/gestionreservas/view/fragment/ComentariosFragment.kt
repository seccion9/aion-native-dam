package com.example.gestionreservas.view.fragment

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentComentariosBinding
import com.example.gestionreservas.databinding.FragmentListadoBinding
import com.example.gestionreservas.models.repository.ComentariosRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorComentarios
import com.example.gestionreservas.viewModel.CajaChica.CajaChicaViewModel
import com.example.gestionreservas.viewModel.CajaChica.CajaChicaViewModelFactory
import com.example.gestionreservas.viewModel.Comentarios.ComentariosViewModel
import com.example.gestionreservas.viewModel.Comentarios.ComentariosViewModelFactory

class ComentariosFragment: Fragment() {
    private lateinit var binding:FragmentComentariosBinding
    private lateinit var viewModel:ComentariosViewModel
    private val comentariosRepository=ComentariosRepository(RetrofitFakeInstance.apiFake)
    private lateinit var adaptadorComentarios: AdaptadorComentarios

    //Añadimos al menú el boton de filtro
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pagos, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentComentariosBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Comentarios"

        //Viewmodel
        val factory = ComentariosViewModelFactory(comentariosRepository)
        viewModel = ViewModelProvider(this, factory)[ComentariosViewModel::class.java]

        viewModel.obtenerComentarios(getTokenFromSharedPreferences().toString())
        instancias()
        return binding.root
    }

    private fun instancias(){
        //Adaptador
        adaptadorComentarios= AdaptadorComentarios(requireContext(), emptyList())
        binding.recylerComentarios.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recylerComentarios.adapter = adaptadorComentarios

        observers()
    }

    /**
     * Observers del viewmodel para cambiar datos de la vista en tiempo rela.
     */
    private fun observers(){
        //Actualiza la lista de comentarios al detecta un cambio el viewmodel
        viewModel.comentarios.observe(viewLifecycleOwner){comentarios ->
            Log.d("ComentariosFragment", "Se recibieron ${comentarios.size} comentarios")
            adaptadorComentarios.actualizarLista(comentarios)
        }
    }
    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

}