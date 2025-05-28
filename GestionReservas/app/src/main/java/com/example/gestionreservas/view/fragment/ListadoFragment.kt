package com.example.gestionreservas.view.fragment


import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.viewModel.listado.ListadosReservas.ListadoViewModelFactory
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentListadoBinding
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.utils.SalaHelper
import com.example.gestionreservas.view.adapter.AdaptadorListado
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.gestionreservas.viewModel.listado.ListadosReservas.ListadoViewModel

class ListadoFragment: Fragment(),OnClickListener, AdapterView.OnItemSelectedListener {
    private lateinit var binding:FragmentListadoBinding
    private lateinit var adaptadorListado:AdaptadorListado
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaActual: LocalDate = LocalDate.now()
    private lateinit var viewModel: ListadoViewModel
    private var nombreExperience: String? = null
    private var idExperience: String? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoBinding.inflate(layoutInflater)
        idExperience = arguments?.getString("idExperience")
        nombreExperience = arguments?.getString("nombreExperience")

        val sala = idExperience?.let { SalaHelper.obtenerCalendarioDeExperiencia(it) }
        Log.d("SalaDetectada", "La experiencia pertenece a $sala")

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            nombreExperience?.let { "Listado – $it" } ?: "Listado Diario"

        val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
        viewModel = ViewModelProvider(this, ListadoViewModelFactory(compraRepository))[ListadoViewModel::class.java]
        instancias()
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.sesiones.collect { lista ->
                    adaptadorListado.actualizarLista(lista)
                }
            }
        }
        binding.spinnerExperiencia.onItemSelectedListener = this
        binding.tvListadoDia.setOnClickListener(this)
        binding.tvListadoSemanal.setOnClickListener(this)
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        binding.tvHoy.setOnClickListener(this)
        binding.layoutSpinnerContainer.setOnClickListener(this)

        //Adaptador spinner filtrar experiencias
        asignarSpinnerYAdaptador()

        // adaptador para pasar datos con el click
        adaptadorListado = AdaptadorListado(
            requireContext(),
            mutableListOf()
        ) { sesion: SesionConCompra ->
            val fragment=DetallesCorreoFragment()
            val bundle=Bundle()
            bundle.putSerializable("sesionConCompra",sesion)
            fragment.arguments=bundle
            val transacion=parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_principal,fragment)
                .addToBackStack(null)
            transacion.commit()

        }
        val orientation = resources.configuration.orientation
        val layoutManager = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager(requireContext(), 2) // 2 columnas en landscape
        } else {
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        binding.recyclerReservasListado.layoutManager = layoutManager
        binding.recyclerReservasListado.adapter = adaptadorListado

        // Cargar datos del día
        cargarDatosSesionesFiltradas()
        actualizarTextoFecha()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarDatosSesionesFiltradas() {
        val seleccion = binding.spinnerExperiencia.selectedItem?.toString()
        val idExperience = when (seleccion) {
            "Escape Jungle" -> "exp_jungle_1"
            "Zombie Room" -> "exp_zombie_2"
            else -> null
        }

        val calendario = idExperience?.let {
            if (it.endsWith("1")) "cal1"
            else if (it.endsWith("2")) "cal2"
            else null
        }

        viewModel.cargarSesiones(getTokenFromSharedPreferences().orEmpty(), fechaActual, calendario)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvListadoDia.id->{
                fechaActual=LocalDate.now()
                cargarDatosSesionesFiltradas()
                actualizarTextoFecha()
            }
            binding.layoutSpinnerContainer.id->{
                binding.spinnerExperiencia.performClick()
            }
            binding.tvHoy.id->{
                fechaActual=LocalDate.now()
                cargarDatosSesionesFiltradas()
                actualizarTextoFecha()
            }

            binding.tvFlechaIzquierdaHoy.id->{
                fechaActual=fechaActual.minusDays(1)
                cargarDatosSesionesFiltradas()
                actualizarTextoFecha()
            }
            binding.tvFlechaDerechaHoy.id->{
                fechaActual=fechaActual.plusDays(1)
                cargarDatosSesionesFiltradas()
                actualizarTextoFecha()
            }
        }
    }
    private fun asignarSpinnerYAdaptador(){
        val experiencias = listOf("Todas", "Escape Jungle", "Zombie Room")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, experiencias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerExperiencia.adapter = adapter
        val seleccionInicial = when (idExperience) {
            "exp_jungle_1" -> "Escape Jungle"
            "exp_zombie_2" -> "Zombie Room"
            else -> "Todas"
        }
        val index = experiencias.indexOf(seleccionInicial)
        if (index >= 0) {
            binding.spinnerExperiencia.setSelection(index)
        }
    }
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val seleccion = parent?.getItemAtPosition(position).toString()
        val idExperience = when (seleccion) {
            "Escape Jungle" -> "exp_jungle_1"
            "Zombie Room" -> "exp_zombie_2"
            else -> null
        }

        val calendario = idExperience?.let {
            if (it.endsWith("1")) "cal1"
            else if (it.endsWith("2")) "cal2"
            else null
        }

        viewModel.cargarSesiones(getTokenFromSharedPreferences().orEmpty(), fechaActual, calendario)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarTextoFecha() {
        val dia = fechaActual.dayOfMonth
        val mes = fechaActual.month.value
        val texto = "$dia/$mes"
        binding.tvFecha.text = texto
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}