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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentCalendarioSemanaBinding
import com.example.gestionreservas.models.entity.DiaSemana
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorDiaSemana
import com.example.gestionreservas.view.adapter.OnDiaSemanaClickListener
import com.example.gestionreservas.viewModel.listado.CalendarioSemana.CalendarioSemanaViewModel
import com.example.gestionreservas.viewModel.listado.CalendarioSemana.CalendarioSemanaViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarioFragmentSemana:Fragment(),OnClickListener,OnDiaSemanaClickListener {
    private lateinit var binding:FragmentCalendarioSemanaBinding
    private lateinit var adaptadorDiaSemana:AdaptadorDiaSemana
    private var listaCalendarioSemana: ArrayList<DiaSemana> = arrayListOf()
    private lateinit var viewModel: CalendarioSemanaViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentCalendarioSemanaBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Calendario Semanal"

        instancias()

        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias(){

        binding.tvSemana.setOnClickListener(this)
        binding.tvMes.setOnClickListener(this)
        binding.tvDiario.setOnClickListener(this)
        binding.ImgFlechaDer.setOnClickListener(this)
        binding.ImgFlechaIzq.setOnClickListener(this)

        val calendarioRepository = CalendarioRepository(RetrofitFakeInstance.apiFake)
        val factory = CalendarioSemanaViewModelFactory(calendarioRepository)
        viewModel = ViewModelProvider(this, factory).get(CalendarioSemanaViewModel::class.java)

        adaptadorDiaSemana= AdaptadorDiaSemana(requireContext(),listaCalendarioSemana,this)
        binding.recyclerDiasSemana.adapter=adaptadorDiaSemana
        val orientation = resources.configuration.orientation
        val layoutManager = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager(requireContext(), 2) // 2 columnas en landscape
        } else {
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        binding.recyclerDiasSemana.layoutManager = layoutManager

        viewModel.inicializarSemana()

        observarDatos()
    }
    private fun observarDatos() {
        viewModel.textoResumenSemana.observe(viewLifecycleOwner) {
            binding.tvFechaSemana.text = it
        }

        viewModel.diasSemana.observe(viewLifecycleOwner) {
            adaptadorDiaSemana.actualizarLista(ArrayList(it))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvDiario.id->{
                val fragment=CalendarioFragmentDiario()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                    .commit()
            }
            binding.tvSemana.id->{
                actualizarSemana()
            }
            binding.tvMes.id->{
                val fragment=CalendarioFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                    .commit()
            }
            binding.ImgFlechaIzq.id->{
                viewModel.retrocederSemana()
                actualizarSemana()
            }
            binding.ImgFlechaDer.id->{
                viewModel.avanzarSemana()
                actualizarSemana()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarSemana(){
        val token = getTokenFromSharedPreferences() ?: return
        val ids = listOf(1, 2)
        viewModel.cargarDiasSemana(token, ids)
    }
    override fun onDiaClick(dia: DiaSemana) {
        /*Implementamos la interfaz de nuestro adaptador para que al pincchar en el dia de la semana
          nos lleve al calendario diario de ese dia,en un bundle metemos el dia seleccionado y se lo pasamos
          por argumentos al fragment
         */
        val fragment = CalendarioFragmentDiario()
        val bundle = Bundle()
        bundle.putString("fechaSeleccionada", dia.fecha.toString()) // ejemplo: "2025-04-28"
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_principal, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }
}