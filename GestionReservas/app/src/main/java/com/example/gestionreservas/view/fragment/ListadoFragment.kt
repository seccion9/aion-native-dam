package com.example.gestionreservas.view.fragment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentListadoBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.network.RetrofitInstance
import com.example.gestionreservas.view.adapter.AdaptadorListado
import kotlinx.coroutines.launch
import java.time.LocalDate

class ListadoFragment: Fragment(),OnClickListener {
    private lateinit var binding:FragmentListadoBinding
    private lateinit var adaptadorListado:AdaptadorListado
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaActual: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Listado Diario"
        instancias()
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        binding.tvListadoDia.setOnClickListener(this)
        binding.tvListadoSemanal.setOnClickListener(this)
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        binding.tvHoy.setOnClickListener(this)
        // Crear adaptador para pasar datos con el click
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
        cargarDatosSesionesHoy()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarDatosSesionesHoy() {
        val token=getTokenFromSharedPreferences()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                actualizarFecha()
                val sesiones = compraRepository.obtenerSesionesDelDia(token.toString(), fechaActual)
                adaptadorListado.actualizarLista(sesiones)
            } catch (e: Exception) {
                Log.e("ListadoFragment", "Error: ${e.localizedMessage}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvListadoDia.id->{
                cargarDatosSesionesHoy()
            }
            binding.tvHoy.id->{
                fechaActual=LocalDate.now()
                cargarDatosSesionesHoy()
            }
            binding.tvListadoSemanal.id->{
                val fragment=ListadoSemanalFragment()
                val transaction=parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                transaction.commit()
            }
            binding.tvFlechaIzquierdaHoy.id->{
                fechaActual=fechaActual.minusDays(1)
                cargarDatosSesionesHoy()
            }
            binding.tvFlechaDerechaHoy.id->{
                fechaActual=fechaActual.plusDays(1)
                cargarDatosSesionesHoy()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarFecha(){
        val dia = fechaActual.dayOfMonth
        val mes = fechaActual.month.value

        val fechaFormateada = "$dia/$mes"
        binding.tvFecha.text = fechaFormateada
    }
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }
}