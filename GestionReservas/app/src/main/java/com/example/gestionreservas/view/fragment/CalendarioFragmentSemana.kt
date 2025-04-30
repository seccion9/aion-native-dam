package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
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
import com.example.gestionreservas.models.entity.OcupacionCalendarioSemanal
import com.example.gestionreservas.network.RetrofitInstance
import com.example.gestionreservas.view.adapter.AdaptadorDiaSemana
import com.example.gestionreservas.view.adapter.OnDiaSemanaClickListener
import retrofit2.Call
import retrofit2.Response
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

        adaptadorDiaSemana= AdaptadorDiaSemana(requireContext(),listaCalendarioSemana,this)
        binding.recyclerDiasSemana.adapter=adaptadorDiaSemana
        binding.recyclerDiasSemana.layoutManager=
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        calcularSemana()
        generarDiasSemana()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generarDiasSemana(){
        //Obtenemos nuestro token y la fecha de inicio y fin de la busqueda semanal de ocupaciones
        val lunes=fechaLunesActual
        val domingo=lunes.plusDays(6)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaInicio = lunes.format(formatter)
        val fechaFin = domingo.format(formatter)
        val token=getTokenFromSharedPreferences()
        val ids = listOf(1, 2)

        if (token != null) {
            RetrofitInstance.api.obtenerCalendarioSemanal(token, ids, fechaInicio, fechaFin)
                .enqueue(object : retrofit2.Callback<Map<String, OcupacionCalendarioSemanal>> {
                    override fun onResponse(call: Call<Map<String, OcupacionCalendarioSemanal>>, response: Response<Map<String, OcupacionCalendarioSemanal>>) {
                      if(response.isSuccessful){
                          val mapaOcupacionSemanal=response.body()
                          listaCalendarioSemana=transformarMapaADiasSemana(mapaOcupacionSemanal)
                          adaptadorDiaSemana.actualizarLista(listaCalendarioSemana)
                      }else{
                          //Mostrar errores en el log en caso de respuesta no exitosa
                          val codigo = response.code()
                          val errorBody = response.errorBody()?.string()
                          Log.e("Horarios", "Error al obtener los horarios. Código: $codigo")
                          Log.e("Horarios", "Mensaje del error: $errorBody")

                      }
                    }

                    override fun onFailure(call: Call<Map<String, OcupacionCalendarioSemanal>>, t: Throwable) {
                        Log.e("API", "Error de red: ${t.message}")
                    }
                })
        }

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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun transformarMapaADiasSemana(
        mapa: Map<String, OcupacionCalendarioSemanal>?
    ): ArrayList<DiaSemana> {
        val lista = arrayListOf<DiaSemana>()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        for (i in 0..6) {
            val fecha = fechaLunesActual.plusDays(i.toLong())
            val clave = fecha.format(formatter)
            val datos = mapa?.get(clave)
            val nombreDia = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))

            val reservas = datos?.ocupadas?.toString() ?: "0"
            val sesiones = datos?.sesiones?.toString() ?: "0"

            lista.add(DiaSemana(fecha, reservas, sesiones, nombreDia))
        }

        return lista
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