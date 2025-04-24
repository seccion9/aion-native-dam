package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentCalendarioSemanaBinding
import com.example.gestionreservas.models.entity.DiaSemana
import com.example.gestionreservas.view.adapter.AdaptadorDiaSemana
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class CalendarioFragmentSemana:Fragment(),OnClickListener {
    private lateinit var binding:FragmentCalendarioSemanaBinding
    private lateinit var listaCalendarioSemana:ArrayList<DiaSemana>
    private lateinit var adaptadorDiaSemana:AdaptadorDiaSemana
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaLunesActual: LocalDate = calcularLunesDeHoy()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentCalendarioSemanaBinding.inflate(inflater, container, false)

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

        /*listaCalendarioSemana= arrayListOf(
            DiaSemana("Lunes","4","6"),
            DiaSemana("Martes","3","6"),
            DiaSemana("Miercoles","2","6"),
            DiaSemana("Jueves","5","6"),
            DiaSemana("Viernes","7","8"),
            DiaSemana("Sabado","8","8"),
            DiaSemana("Domingo","0","0"),
        )*/
        listaCalendarioSemana=generarDiasSemana()
        adaptadorDiaSemana= AdaptadorDiaSemana(requireContext(),listaCalendarioSemana)
        binding.recyclerDiasSemana.adapter=adaptadorDiaSemana
        binding.recyclerDiasSemana.layoutManager=
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        calcularSemanaActual()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generarDiasSemana(): ArrayList<DiaSemana>{
        var listaCalendarioSemanaSimulada:ArrayList<DiaSemana>
        listaCalendarioSemanaSimulada= java.util.ArrayList()
        for(i in 0..6){
            val reservas=(0..6).random()
            val fechaDia = fechaLunesActual.plusDays(i.toLong())
            val numeroDia = fechaDia.dayOfMonth.toString()
            val diaSemanaNombre=fechaDia.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            if(i<6) {
                listaCalendarioSemanaSimulada.add(DiaSemana(numeroDia, "${reservas}", "8", diaSemanaNombre))
            }else{
                listaCalendarioSemanaSimulada.add(DiaSemana(numeroDia, "0", "0", diaSemanaNombre))
            }
                Log.d("Lista de dias semana","Dia ${numeroDia} ,reservas: ${reservas} ,sesiones: 8, ${diaSemanaNombre}")
        }
        return listaCalendarioSemanaSimulada
    }
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularSemanaActual(){
        calcularSemana()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularLunesDeHoy(): LocalDate {
        val hoy = LocalDate.now()
        val diaSemana = hoy.dayOfWeek.value
        return hoy.minusDays((diaSemana - 1).toLong())
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

        listaCalendarioSemana = generarDiasSemana()
        adaptadorDiaSemana.actualizarLista(listaCalendarioSemana)


        calcularDatosSemana()
    }
    private fun calcularDatosSemana(){

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
                val fragment=CalendarioFragmentSemana()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                    .commit()
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
                calcularSemana()
            }
            binding.ImgFlechaDer.id->{
                fechaLunesActual = fechaLunesActual.plusDays(7)
                calcularSemana()
            }
        }
    }

}