package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
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
import com.example.gestionreservas.databinding.FragmentListadoSemanalBinding
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.utils.SalaHelper
import com.example.gestionreservas.view.adapter.AdaptadorListado
import com.example.gestionreservas.viewModel.listado.ListadosReservas.ListadoViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class ListadoSemanalFragment:Fragment(),OnClickListener, AdapterView.OnItemSelectedListener {
    private lateinit var binding: FragmentListadoSemanalBinding
    private lateinit var adaptadorListado:AdaptadorListado
    @SuppressLint("NewApi")
    private var fechaActual=LocalDate.now()
    private var nombreExperience: String? = null
    private var idExperience: String? = null
    private lateinit var viewModel: ListadoViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoSemanalBinding.inflate(layoutInflater)
        idExperience = arguments?.getString("idExperience")
        nombreExperience = arguments?.getString("nombreExperience")
        val sala = idExperience?.let { SalaHelper.obtenerCalendarioDeExperiencia(it) }
        Log.d("SalaDetectada", "La experiencia pertenece a $sala")

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            nombreExperience?.let { "Listado Semanal– $it" } ?: "Listado Semanal"
        val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
        viewModel = ViewModelProvider(this, ListadoViewModelFactory(compraRepository))[ListadoViewModel::class.java]

        instancias()
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias(){
        binding.spinnerExperiencia.onItemSelectedListener = this
        asignarSpinnerYAdaptador()
        calcularFechaActual()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.sesiones.collect { lista ->
                    adaptadorListado.actualizarLista(lista)
                }
            }
        }
        //Instancias click
        binding.tvListadoDia.setOnClickListener(this)
        binding.tvFlechaDerechaSemana.setOnClickListener(this)
        binding.tvFlechaIzquierdaSemana.setOnClickListener(this)
        binding.tvListadoSemanal.setOnClickListener(this)
        binding.layoutSpinnerContainer.setOnClickListener(this)
        binding.tvSemana.setOnClickListener(this)
        //Adaptador
        adaptadorListado= AdaptadorListado(
            requireContext(),
            mutableListOf()
        ) { sesion: SesionConCompra ->
            val fragment=DetalleSesionFragment()
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
        binding.recyclerReservasListado.adapter=adaptadorListado
        //cargamos datos sesiones semana
        obtenerListadoReservasSemanal()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvListadoDia.id->{
                val fragment=ListadoFragment()
                val transaction=parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                transaction.commit()
            }
            binding.tvSemana.id->{
                fechaActual=LocalDate.now()
                calcularFechaActual()
                obtenerListadoReservasSemanal()
            }
            binding.tvListadoSemanal.id->{
                fechaActual=LocalDate.now()
                calcularFechaActual()
                obtenerListadoReservasSemanal()
            }
            binding.tvFlechaDerechaSemana.id->{

                fechaActual=fechaActual.plusDays(7)
                calcularFechaActual()
                obtenerListadoReservasSemanal()
            }
            binding.layoutSpinnerContainer.id->{
                binding.spinnerExperiencia.performClick()
            }
            binding.tvFlechaIzquierdaSemana.id->{

                fechaActual=fechaActual.minusDays(7)
                calcularFechaActual()
                obtenerListadoReservasSemanal()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    //Obtenemos las sesiones filtradas por la fecha actual y las ordenamos por fecha
    private fun obtenerListadoReservasSemanal(){
        val token = getTokenFromSharedPreferences().orEmpty().replace("Bearer ", "")
        val fechaInicio = fechaActual.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val fechaFin = fechaActual.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val seleccion = binding.spinnerExperiencia.selectedItem?.toString()
        val idExpSeleccionado = when (seleccion) {
            "Escape Jungle" -> "exp_jungle_1"
            "Zombie Room" -> "exp_zombie_2"
            else -> null
        }
        val calendario = idExpSeleccionado?.let { SalaHelper.obtenerCalendarioDeExperiencia(it) }


        viewModel.cargarSesionesSemana(token, fechaInicio, fechaFin, calendario)

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
    @SuppressLint("NewApi")
    private fun calcularFechaActual(){
        val hoy=fechaActual
        // Lunes de esta semana
        val lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val domingo = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val texto = "${lunes.dayOfMonth}-${domingo.dayOfMonth}/${hoy.monthValue}"
        binding.tvFechaSemana.text = texto
    }
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        obtenerListadoReservasSemanal()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}