package com.example.gestionreservas.view.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentCalendarioBinding
import java.time.LocalDate


class CalendarioFragment : Fragment(),OnClickListener {
    private lateinit var binding: FragmentCalendarioBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentCalendarioBinding.inflate(inflater, container, false)

        instancias()

        return binding.root
    }
    @SuppressLint("NewApi")
    private fun instancias(){
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth)
            Log.d("Calendario", "Fecha seleccionada: $fechaSeleccionada")

            val fragment = CalendarioFragmentDiario()
            val args = Bundle().apply {
                putString("fecha", fechaSeleccionada.toString())
            }
            fragment.arguments = args

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_principal, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.tvDiario.setOnClickListener(this)
        binding.tvSemana.setOnClickListener(this)

    }

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
        }
    }

}