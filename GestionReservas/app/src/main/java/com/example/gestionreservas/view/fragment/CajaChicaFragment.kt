package com.example.gestionreservas.view.fragment

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
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
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentCajaChicaBinding
import com.example.gestionreservas.models.entity.PagoCajaChica
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorCajaChica
import com.example.gestionreservas.viewModel.CajaChica.CajaChicaViewModel
import com.example.gestionreservas.viewModel.CajaChica.CajaChicaViewModelFactory
import com.example.gestionreservas.viewModel.CalendarioDiario.CalendarioDiarioViewModel


class CajaChicaFragment:Fragment() {
    private lateinit var binding: FragmentCajaChicaBinding
    private lateinit var listaMetodosPago: List<String>
    private lateinit var adaptadorMetodosPago: ArrayAdapter<String>
    private lateinit var listaPagos: List<PagoCajaChica>
    private lateinit var adaptadorPagos: AdaptadorCajaChica
    private lateinit var viewModel: CajaChicaViewModel
    private val cajaChicaRepository = CajaChicaRepository(RetrofitFakeInstance)
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private var refrescoManual = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCajaChicaBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Caja Chica"

        val factory = CajaChicaViewModelFactory(cajaChicaRepository, compraRepository)
        viewModel = ViewModelProvider(this, factory)[CajaChicaViewModel::class.java]

        instancias()
        return binding.root
    }

    private fun instancias() {

        //Adaptador spinner
        listaMetodosPago = listOf("-- Todos --", "Tarjeta", "Bizum", "Manual", "Efectivo")
        adaptadorMetodosPago =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaMetodosPago)
        adaptadorMetodosPago.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipoPagos.adapter = adaptadorMetodosPago

        //Adaptador recyclerview
        listaPagos = mutableListOf()
        adaptadorPagos = AdaptadorCajaChica(
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
        setupSpinnerFiltro()
        filtrarListaPorConcepto()
    }
    private fun filtrarListaPorConcepto() {
        binding.editTextBuscarConcepto.addTextChangedListener { editable ->
            val texto = editable?.toString() ?: ""
            viewModel.aplicarFiltroPorConcepto(texto)
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
    private fun setupSpinnerFiltro() {
        binding.spinnerTipoPagos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val metodoSeleccionado = binding.spinnerTipoPagos.selectedItem.toString()
                viewModel.aplicarFiltroPorMetodo(metodoSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.aplicarFiltroPorMetodo("-- Todos --")
            }
        }
    }
}
