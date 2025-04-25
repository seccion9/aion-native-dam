package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import com.example.gestionreservas.databinding.FragmentCalendarioDiarioBinding
import com.example.gestionreservas.models.entity.Experiencia
import com.example.gestionreservas.models.entity.ExperienciaConHorarios
import com.example.gestionreservas.models.entity.HoraReserva
import com.example.gestionreservas.network.RetrofitInstance
import com.example.gestionreservas.view.adapter.AdaptadorHoraReserva
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random
import android.app.AlertDialog.Builder


class CalendarioFragmentDiario: Fragment() ,OnClickListener{
    private lateinit var binding: FragmentCalendarioDiarioBinding
    private val reservasPorDia: MutableMap<LocalDate, List<HoraReserva>> = mutableMapOf()
    private lateinit var listaReservaHoras:ArrayList<HoraReserva>
    private lateinit var adaptadorHoraReserva: AdaptadorHoraReserva
    private val listaIdsExperiencias = mutableListOf<Int>()
    private var token: String? = null
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaActual: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout del fragmento para que cargue la vista correctamente
        binding = FragmentCalendarioDiarioBinding.inflate(inflater, container, false)
        instancias()
        /*recibimos por argumentos de nuestro fragment semanal y mensual la fecha seleccionada
          si no es nula llamamos a la funcion actualizar fecha para obtener la
          fecha que se selecciono y sus horarios.
          Si es nula se obtiene la fecha de hoy
         */
        val fechaString = arguments?.getString("fechaSeleccionada") ?: arguments?.getString("fecha")
        if (fechaString != null) {
            fechaActual = LocalDate.parse(fechaString)
            actualizarFecha()
            val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            obtenerHorariosDelDia(getTokenFromSharedPreferences()!!, listaIdsExperiencias, fechaStr)
        } else {
            obtenerFechaHoy()
        }

        return binding.root
    }

     @RequiresApi(Build.VERSION_CODES.O)
     fun instancias(){
         //Obtenemos la fecha de hoy
         obtenerFechaHoy()
         //generarReservasParaMesActual()
         obtenerExperiencias()
         //Instancias click
         binding.tvFlechaDerechaHoy.setOnClickListener(this)
         binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
         binding.tvMes.setOnClickListener(this)
         binding.btnTestExp.setOnClickListener(this)
         binding.btnTodasExp.setOnClickListener(this)
         binding.selectFecha.setOnClickListener(this)
         binding.tvHoy.setOnClickListener(this)
         binding.tvDiario.setOnClickListener(this)
         binding.tvRecargar.setOnClickListener(this)
         binding.tvSemana.setOnClickListener(this)

         //Adaptadores y recycler reservas
         listaReservaHoras = arrayListOf()
         adaptadorHoraReserva = AdaptadorHoraReserva(requireContext(), listaReservaHoras)
         binding.recyclerHorasSalas.adapter = adaptadorHoraReserva
         binding.recyclerHorasSalas.layoutManager = LinearLayoutManager(requireContext())
    }

    //Este metodo obtiene la fecha actual y la asigna al textview tvFecha

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerFechaHoy(){
        val fechaHoy = LocalDate.now()
        val dia = fechaHoy.dayOfMonth
        val mes = fechaHoy.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        val anio = fechaHoy.year
        val diaSemana = fechaHoy.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        val fechaDiaSemana="${diaSemana.replaceFirstChar { it.titlecase(Locale("es", "ES")) }} $dia $mes $anio"

        binding.tvFecha.text = fechaDiaSemana
        Log.d("Fecha actual",fechaHoy.toString())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarFecha(){
        val diaSemana = fechaActual.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        val dia = fechaActual.dayOfMonth
        val mes = fechaActual.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        val anio = fechaActual.year

        val fechaFormateada = "${diaSemana.replaceFirstChar { it.titlecase(Locale("es", "ES")) }} $dia $mes $anio"
        binding.tvFecha.text = fechaFormateada

        //Actualizamos reservas para dia seleccionado
        val reservasDelDia = reservasPorDia[fechaActual] ?: emptyList()
        adaptadorHoraReserva.actualizarLista(reservasDelDia)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        val token=getTokenFromSharedPreferences()
        when(v?.id){

            binding.tvFlechaIzquierdaHoy.id->{
                fechaActual = fechaActual.minusDays(1)
                actualizarFecha()
                val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                Log.d("CalendarioDiario", "Flecha izquierda - Fecha: $fechaStr")
                obtenerHorariosDelDia(token!!, listaIdsExperiencias, fechaStr)
            }
            binding.tvFlechaDerechaHoy.id->{
                fechaActual = fechaActual.plusDays(1)
                actualizarFecha()
                val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                Log.d("CalendarioDiario", "Flecha derecha - Fecha: $fechaStr")
                obtenerHorariosDelDia(token!!, listaIdsExperiencias, fechaStr)
            }
            binding.tvMes.id->{
                val fragment=CalendarioFragment()
                val transacion=parentFragmentManager.beginTransaction()
                transacion.replace(R.id.fragment_principal,fragment)
                transacion.addToBackStack(null)
                transacion.commit()
            }
            binding.btnTestExp.id->{
                val fragment=ExperienciaTestFragment()
                val transacion=parentFragmentManager.beginTransaction()
                transacion.replace(R.id.fragment_principal,fragment)
                transacion.addToBackStack(null)
                transacion.commit()
            }
            binding.btnTodasExp.id->{
                val fragment=ExperienciaTestFragment()
                val transacion=parentFragmentManager.beginTransaction()
                transacion.replace(R.id.fragment_principal,fragment)
                transacion.addToBackStack(null)
                transacion.commit()
            }
            binding.selectFecha.id->{

                mostrarSeleccionFecha()
            }
            binding.tvHoy.id->{
                volverDiaActual()
            }
            binding.tvDiario.id->{
                volverDiaActual()
            }
            binding.tvSemana.id->{
                val fragment=CalendarioFragmentSemana()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                    .commit()
            }
            binding.tvRecargar.id->{
                val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                obtenerHorariosDelDia(token!!,listaIdsExperiencias,fechaStr)
                Log.d("Recargando datos", "cargando datos para dia: $fechaStr")
            }
        }
    }
    @SuppressLint("NewApi")
    private fun volverDiaActual(){
        fechaActual = LocalDate.now()
        actualizarFecha()
        val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        obtenerHorariosDelDia(getTokenFromSharedPreferences()!!, listaIdsExperiencias, fechaStr)
    }
    @SuppressLint("NewApi")
    private fun mostrarSeleccionFecha(){

        val fechaHoy = LocalDate.now()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val nuevaFecha = LocalDate.of(year, month + 1, dayOfMonth)
                fechaActual = nuevaFecha
                actualizarFecha()

                val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                obtenerHorariosDelDia(getTokenFromSharedPreferences()!!, listaIdsExperiencias, fechaStr)
            },
            fechaHoy.year,
            fechaHoy.monthValue - 1,
            fechaHoy.dayOfMonth
        )

        datePicker.show()


    }
    private fun obtenerHorariosDelDia(token: String, listaIds: List<Int>,fecha:String) {

        if (token != null) {
            Log.d("API", "Llamando a la API con fecha: $fecha")
            RetrofitInstance.api.obtenerReservas(token, listaIds, fecha)
                .enqueue(object : Callback<List<ExperienciaConHorarios>> {
                    override fun onResponse(
                        call: Call<List<ExperienciaConHorarios>>,
                        response: Response<List<ExperienciaConHorarios>>
                    ) {
                        /*false añadido para que salte al else y genere los datos falsos ya que la API
                           actualmente no nos devuelve datos consistentes,se enseñara un mensaje en la
                           pantalla advirtiendo que los mensajes son falseados
                         */
                        if (response.isSuccessful && false) {
                            val experiencias = response.body()
                            Log.d("API Response", "Experiencias recibidas: $experiencias")
                            val mapaHorarios: MutableMap<String, MutableMap<Int, Boolean>> = mutableMapOf()

                            //Rellenamos el mapa con cada franja y el estado por ID
                            experiencias?.forEach { experiencia ->
                                Log.d("Experiencia", "ID: ${experiencia.id}")
                                val idSala = experiencia.id

                                experiencia.calendarios?.forEach { calendario ->
                                    calendario.horarios.forEach { horario ->
                                        val inicio = horario.horaInicio.substring(0, 5)
                                        val fin = horario.horaFin.substring(0, 5)
                                        val clave = "$inicio-$fin"

                                        val mapaPorSala = mapaHorarios.getOrPut(clave) { mutableMapOf() }
                                        mapaPorSala[idSala] = horario.estaLibre
                                    }
                                }
                            }


                            val sala1Id = listaIds.getOrNull(0)
                            val sala2Id = listaIds.getOrNull(1)

                            listaReservaHoras.clear()

                            // Paso 2: Convertimos a HoraReserva
                            mapaHorarios.forEach { (clave, mapaSalas) ->
                                val (inicio, fin) = clave.split("-")

                                val sala1 = mapaSalas[sala1Id] ?: true // Si no viene, asumimos libre
                                val sala2 = mapaSalas[sala2Id] ?: true

                                listaReservaHoras.add(
                                    HoraReserva(
                                        horaInicio = inicio,
                                        horaFin = fin,
                                        sala1Libre = sala1,
                                        sala2Libre = sala2
                                    )
                                )
                            }
                            // Actualizamos el RecyclerView con la nueva lista
                            adaptadorHoraReserva.actualizarLista(listaReservaHoras)

                        } else {
                            val dialogo= AlertDialog.Builder(context)
                            dialogo.setTitle("Datos simulados")
                            dialogo.setMessage("Los datos mostrados no son reales. Estamos usando información falsa debido a un fallo en la conexión.")
                            dialogo.setPositiveButton("Aceptar") { dialog, _ ->
                                dialog.dismiss()
                            }
                            dialogo.setCancelable(true)
                            dialogo.show()

                            val codigo = response.code()
                            val errorBody = response.errorBody()?.string()
                            Log.e("Horarios", "Error al obtener los horarios. Código: $codigo")
                            Log.e("Horarios", "Mensaje del error: $errorBody")
                            // Generar horarios falsos según la fecha actual
                            val horariosFalsos = generarDatosFalsosParaFecha(fecha)
                            listaReservaHoras.clear()
                            listaReservaHoras.addAll(horariosFalsos)
                            adaptadorHoraReserva.actualizarLista(listaReservaHoras)
                        }
                    }

                    override fun onFailure(call: Call<List<ExperienciaConHorarios>>, t: Throwable) {
                        Log.e("Horarios", "Error de red: ${t.message}")
                    }
                })
        }

    }
    private fun obtenerExperiencias(){
        val token=getTokenFromSharedPreferences()
        if (token != null) {
            RetrofitInstance.api.obtenerExperiencias(token)
                .enqueue(object : Callback<List<Experiencia>> {
                    @SuppressLint("NewApi")
                    override fun onResponse(
                        call: Call<List<Experiencia>>,
                        response: Response<List<Experiencia>>
                    ) {
                        if (response.isSuccessful) {
                            val experiencias = response.body()
                            val listaIds = experiencias?.map { it.id } ?: emptyList()

                            listaIdsExperiencias.clear()
                            listaIdsExperiencias.addAll(listaIds)
                            Log.d("IDs", "IDs recogidos: $listaIdsExperiencias")

                            if (listaIdsExperiencias.isNotEmpty()) {
                                val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                obtenerHorariosDelDia(token, listaIdsExperiencias,fechaStr.toString())
                            }else {
                                Log.e("IDs", "Lista de IDs vacía")
                            }
                        } else {
                            val errorTexto = response.errorBody()?.string()
                            Log.e("CalendarioDiario", "Error ${response.code()} - Detalle: $errorTexto")
                        }
                    }

                    override fun onFailure(call: Call<List<Experiencia>>, t: Throwable) {
                        Log.e("CalendarioDiario", "Fallo al conectar: ${t.message}")
                    }
                })
        }

    }
    //Funcion creada para devolver datos de reservas diarias cuando la respuesta a la api no devuelva nada
    private fun generarDatosFalsosParaFecha(fecha: String): List<HoraReserva> {
        val franjas = listOf("10:00-11:00", "11:10-12:10", "12:20-13:20", "13:30-14:30", "16:20-17:20", "18:30-19:30")
        val aleatorio = Random(fecha.hashCode())

        return franjas.map {
            val (inicio, fin) = it.split("-")
            HoraReserva(
                horaInicio = inicio,
                horaFin = fin,
                sala1Libre = aleatorio.nextBoolean(),
                sala2Libre = aleatorio.nextBoolean()
            )
        }
    }
    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }
}