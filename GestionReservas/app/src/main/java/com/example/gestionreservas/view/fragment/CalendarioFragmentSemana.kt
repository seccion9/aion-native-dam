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
import com.example.gestionreservas.databinding.FragmentCalendarioSemanaBinding
import com.example.gestionreservas.models.entity.DiaSemana
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorDiaSemana
import com.example.gestionreservas.view.adapter.OnDiaSemanaClickListener
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarioFragmentSemana:Fragment(),OnClickListener,OnDiaSemanaClickListener {
    private lateinit var binding:FragmentCalendarioSemanaBinding
    private lateinit var adaptadorDiaSemana:AdaptadorDiaSemana
    private var listaCalendarioSemana: ArrayList<DiaSemana> = arrayListOf()
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaLunesActual: LocalDate = calcularLunesDeHoy()
    private val calendarioRepository = CalendarioRepository(RetrofitFakeInstance.apiFake)

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

        adaptadorDiaSemana= AdaptadorDiaSemana(requireContext(),listaCalendarioSemana,this)
        binding.recyclerDiasSemana.adapter=adaptadorDiaSemana
        val orientation = resources.configuration.orientation
        val layoutManager = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager(requireContext(), 2) // 2 columnas en landscape
        } else {
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        binding.recyclerDiasSemana.layoutManager = layoutManager
        calcularSemana()
        generarDiasSemana()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generarDiasSemana() {
        //Obtenemos nuestro token y la fecha de inicio y fin de la busqueda semanal de ocupaciones
        val lunes = fechaLunesActual
        val domingo = lunes.plusDays(6)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaInicio = lunes.format(formatter)
        val fechaFin = domingo.format(formatter)
        val token = getTokenFromSharedPreferences()
        val ids = listOf(1, 2)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val mapaOcupacionSemanal = calendarioRepository.obtenerOcupacionSemanalFake(
                    token ?: return@launch, ids, fechaInicio, fechaFin)

                listaCalendarioSemana = calendarioRepository.transformarMapaADiasSemana(
                    mapaOcupacionSemanal,
                    fechaLunesActual
                )

                adaptadorDiaSemana.actualizarLista(listaCalendarioSemana)

            } catch (e: Exception) {
                Log.e("calendarioFragmentSemana", "Error en la API falsa: ${e.message}")
            }
        }
    }
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularLunesDeHoy(): LocalDate {
        fechaLunesActual = LocalDate.now()
        val diaSemana = fechaLunesActual.dayOfWeek.value
        return fechaLunesActual.minusDays((diaSemana - 1).toLong())
    }

    /*Cambia el text de las fechas segun vayan avanzando o disminuyendo las semanas y
        llamara a una funcion que cargue los datos de la API de esa semana
     */
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularSemana(){
        val fechaActual=fechaLunesActual
        val diaSemanaNumero=fechaActual.dayOfWeek.value

        val lunes = fechaActual.minusDays((diaSemanaNumero - 1).toLong())
        val domingo = lunes.plusDays(6)

        fechaLunesActual = lunes

        val mesLunes = lunes.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
        val mesDomingo = domingo.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }

        // Si están en el mismo mes, mostramos uno solo
        if (mesLunes == mesDomingo) {
            binding.tvFechaSemana.text = "${lunes.dayOfMonth} - ${domingo.dayOfMonth} $mesLunes"
        } else {
            // Si caen en meses distintos, lo mostramos completo
            binding.tvFechaSemana.text = "${lunes.dayOfMonth} $mesLunes - ${domingo.dayOfMonth} $mesDomingo"
        }
        adaptadorDiaSemana.actualizarLista(listaCalendarioSemana)
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
                calcularLunesDeHoy()
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
                fechaLunesActual = fechaLunesActual.minusDays(7)
                actualizarSemana()
            }
            binding.ImgFlechaDer.id->{
                fechaLunesActual = fechaLunesActual.plusDays(7)
                actualizarSemana()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarSemana(){
        calcularSemana()
        generarDiasSemana()
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