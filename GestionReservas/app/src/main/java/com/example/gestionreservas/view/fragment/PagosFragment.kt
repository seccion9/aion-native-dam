package com.example.gestionreservas.view.fragment

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentPagosBinding
import com.example.gestionreservas.models.entity.PagoReserva
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorPagos
import com.example.gestionreservas.view.dialogs.FiltroPagosBottomSheetFragment
import com.example.gestionreservas.viewModel.Pagos.PagosViewModel
import com.example.gestionreservas.viewModel.Pagos.PagosViewModelFactory


class PagosFragment:Fragment() {
    private lateinit var binding: FragmentPagosBinding
    private lateinit var listaMetodosPago: List<String>
    private lateinit var adaptadorMetodosPago: ArrayAdapter<String>
    private lateinit var listaPagos: List<PagoReserva>
    private lateinit var adaptadorPagos: AdaptadorPagos
    private lateinit var viewModel: PagosViewModel
    private val cajaChicaRepository = CajaChicaRepository(RetrofitFakeInstance)
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private var refrescoManual = false

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pagos, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPagosBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Pagos"

        val factory = PagosViewModelFactory(cajaChicaRepository, compraRepository)
        viewModel = ViewModelProvider(this, factory)[PagosViewModel::class.java]

        instancias()
        return binding.root
    }

    private fun instancias() {


        //Adaptador recyclerview
        listaPagos = mutableListOf()
        adaptadorPagos = AdaptadorPagos(
            context = requireContext(),
            listaPagos = listaPagos,
            mapaPagosACompras = viewModel.mapaPagosACompras
        )

        binding.recyclerPagosCajaChica.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerPagosCajaChica.adapter = adaptadorPagos

        binding.recyclerPagosCajaChica.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fade_in)

        getTokenFromSharedPreferences()?.let { viewModel.obtenerPagos(it) }
        //Observers
        observersFragment()

        //Refrescar vista

        binding.swipeRefreshLayout.setOnRefreshListener {
            refrescoManual = true
            refrescarListado()
        }
        //Metodos de filtrar por spinner y a través del edit

        filtrarListaPorConcepto()
    }
    private fun filtrarListaPorConcepto() {
        binding.editTextBuscarConcepto.addTextChangedListener { editable ->
            val concepto = editable?.toString() ?: ""
            viewModel.actualizarFiltros(concepto = concepto)
        }
    }

    private fun observersFragment() {
        viewModel.pagosCaja.observe(viewLifecycleOwner) { lista ->

            Log.d("CajaChicaFragment", "Recibidos ${lista.size} pagos caja")
            adaptadorPagos.actualizarLista(lista)
            binding.recyclerPagosCajaChica.scheduleLayoutAnimation()
        }
        viewModel.pagosFiltrados.observe(viewLifecycleOwner){pagos ->
            adaptadorPagos.actualizarLista(pagos)
            binding.recyclerPagosCajaChica.scheduleLayoutAnimation()
        }
        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.swipeRefreshLayout.isRefreshing = cargando
            if (!cargando && refrescoManual) {
                Toast.makeText(
                    requireContext(),
                    "Datos actualizados correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                refrescoManual = false
            }
        }
    }

    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    private fun refrescarListado() {
        getTokenFromSharedPreferences()?.let { viewModel.obtenerPagos(it) }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filtro -> {
                mostrarBottomSheetFiltro()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun mostrarBottomSheetFiltro() {
        val bottomSheet = FiltroPagosBottomSheetFragment(viewModel)
        bottomSheet.show(parentFragmentManager, "filtro_bottom_sheet")
    }


}
