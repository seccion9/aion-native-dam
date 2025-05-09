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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentListadoSemanalBinding
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.network.RetrofitInstance.api
import com.example.gestionreservas.view.adapter.AdaptadorListado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class ListadoSemanalFragment:Fragment(),OnClickListener {
    private lateinit var binding: FragmentListadoSemanalBinding
    private lateinit var adaptadorListado:AdaptadorListado
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    @SuppressLint("NewApi")
    private var fechaActual=LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoSemanalBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Listado Semanal"
        instancias()
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias(){
        calcularFechaActual()
        //Instancias click
        binding.tvListadoDia.setOnClickListener(this)
        binding.tvFlechaDerechaSemana.setOnClickListener(this)
        binding.tvFlechaIzquierdaSemana.setOnClickListener(this)
        binding.tvListadoSemanal.setOnClickListener(this)
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
                obtenerListadoReservasSemanal()
            }
            binding.tvListadoSemanal.id->{
                obtenerListadoReservasSemanal()
            }
            binding.tvFlechaDerechaSemana.id->{
                fechaActual=fechaActual.plusDays(7)
                obtenerListadoReservasSemanal()
            }
            binding.tvFlechaIzquierdaSemana.id->{
                fechaActual=fechaActual.minusDays(7)
                obtenerListadoReservasSemanal()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerListadoReservasSemanal(){
        val token=getTokenFromSharedPreferences()
        viewLifecycleOwner.lifecycleScope.launch {
            try{
                calcularFechaActual()
                val compras = withContext(Dispatchers.IO) {
                    RetrofitFakeInstance.apiFake.getPurchases(token.toString())
                }
                val sesiones = compraRepository.obtenerSesionesDeSemana(compras, fechaActual)
                adaptadorListado.actualizarLista(sesiones)
            }catch (e:Exception){
                Log.e("ListadoFragmentSemanal","Error en la API fake ${e.localizedMessage}")
            }
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
}