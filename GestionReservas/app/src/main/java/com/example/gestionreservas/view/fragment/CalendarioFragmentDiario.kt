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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentCalendarioDiarioBinding
import com.example.gestionreservas.models.entity.HoraReserva
import com.example.gestionreservas.view.adapter.AdaptadorHoraReserva
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import kotlinx.coroutines.launch



class CalendarioFragmentDiario: Fragment() ,OnClickListener{
    private lateinit var binding: FragmentCalendarioDiarioBinding
    private val reservasPorDia: MutableMap<LocalDate, List<HoraReserva>> = mutableMapOf()
    private lateinit var listaReservaHoras:ArrayList<HoraReserva>
    private lateinit var adaptadorHoraReserva: AdaptadorHoraReserva
    private var listaOcupaciones:List<Ocupacion> = mutableListOf()
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private val calendarioRepository = CalendarioRepository(RetrofitFakeInstance.apiFake)
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaActual: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout del fragmento para que cargue la vista correctamente
        binding = FragmentCalendarioDiarioBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Calendario Diario"
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
        } else {
            obtenerFechaHoy()
        }

        /* Lanza la mock para la fecha mostrada */
        val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

        cargarDesdeMock(fechaStr)

        return binding.root
    }

     @RequiresApi(Build.VERSION_CODES.O)
     fun instancias(){
         //Obtenemos la fecha de hoy
         obtenerFechaHoy()

         //Instancias nuestro adaptador y recycler
         listaReservaHoras = arrayListOf()
         adaptadorHoraReserva = AdaptadorHoraReserva(
             requireContext(),
             listaReservaHoras

         ) { hora: HoraReserva, calendarioId: String ->


             /*Buscamos en nuestra lista ocupaciones si nuetro item coincide en hora y calendario
                con el seleccionado.
              */
             val ocupacion = listaOcupaciones.find {
                 it.start == hora.horaInicio && it.calendarioId == calendarioId

             }


             val token = getTokenFromSharedPreferences()
             viewLifecycleOwner.lifecycleScope.launch {
                 try {
                     var compra: Compra? = null
                     val sesion: Sesion

                     if (ocupacion != null && token != null) {

                         //Buscamos la compra en la API y buscamos por su id para obtenerla
                         val listaCompras = compraRepository.obtenerCompras(token)
                         compra = listaCompras.find { it.id == ocupacion.idCompra }


                         //Si hay compra se trnasfor,a sesion para usarlo en sesionConCompra
                         sesion = if (compra != null) {
                             transformarItemASesion(compra, ocupacion)
                         } else {
                             // Si no hay compra se pasan datos "vacios"
                             Sesion(
                                 hora = hora.horaInicio,
                                 calendario = calendarioId,
                                 nombre = "",
                                 participantes = 0,
                                 totalPagado = 0.0,
                                 estado = "sin_reserva",
                                 idiomas = ""
                             )
                         }
                     } else {
                         Log.d("DEBUG_COMPRAS", "se paso al else")
                         // No hay ocupación: crear sesión vacía para pasarla a detalles
                         sesion = Sesion(hora = hora.horaInicio,
                             calendario = calendarioId,
                             nombre = "",
                             participantes = 0,
                             totalPagado = 0.0,
                             estado = "sin_reserva",
                             idiomas = ""
                         )
                     }

                     val compraSesion = SesionConCompra(sesion, compra)
                     val bundle = Bundle()
                     bundle.putSerializable("sesionConCompra", compraSesion)

                     val fragment = DetalleSesionFragment()
                     fragment.arguments = bundle

                     parentFragmentManager.beginTransaction()
                         .replace(R.id.fragment_principal, fragment)
                         .addToBackStack(null)
                         .commit()

                 } catch (e: Exception) {
                     Log.e("CalendarioDiarioFragment", "Error navegando: ${e.message}")
                 }
             }

         }
         binding.recyclerHorasSalas.apply {
             adapter = adaptadorHoraReserva
             layoutManager = LinearLayoutManager(requireContext())
         }

         //Cargamos datos desde nuestro mock API
         val hoyStr = fechaActual.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

         cargarDesdeMock(hoyStr)

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

    }

    /*Obtenemos nuestro token y con la fecha obtenida por parametros cargamos los datos del dia
     despues actualizamos nuestro adaptador para notificarle que cambiaron los datos,esta funcion nos
     vale cada vez que cambiemos de fecha
    */
    @SuppressLint("NewApi")
    private fun cargarDesdeMock(fechaDDMM: String) {
        val tkn = getTokenFromSharedPreferences() ?: return

        // Cambiamos formato a lo que devuelve la API -> 05/05/2025 -> 2025-05-05

        val fechaAPI = try {
            // Intenta parsear como dd/MM/yyyy
            LocalDate.parse(fechaDDMM, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            // Si falla, intenta como yyyy/MM/dd
            LocalDate.parse(fechaDDMM, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        }.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))


        viewLifecycleOwner.lifecycleScope.launch {
            val listaFiltrada=calendarioRepository.obtenerOcupacionesDelDia(tkn,fechaAPI)
            listaOcupaciones = listaFiltrada
            val listaHoras = calendarioRepository.transformarOcupacionesAHoraReserva(listaFiltrada)
            adaptadorHoraReserva.actualizarLista(listaHoras)

            Log.e("fun cargarDatosDesdeMock","Lista Ocupacion ${fechaDDMM}: ~${listaOcupaciones}")
            adaptadorHoraReserva.actualizarLista(listaHoras)
        }
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
    //Funciones on click de nuestro fragment
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {

        when(v?.id){

            binding.tvFlechaIzquierdaHoy.id->{
                fechaActual = fechaActual.minusDays(1)
                actualizarFecha()
                val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                cargarDesdeMock(fechaStr)
            }
            binding.tvFlechaDerechaHoy.id->{
                fechaActual = fechaActual.plusDays(1)
                actualizarFecha()
                val fechaStr = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                cargarDesdeMock(fechaStr)
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
                cargarDesdeMock(fechaStr)
            }
        }
    }
    //Volvemos al dia actual y se cargan los datos de ese dia
    @SuppressLint("NewApi")
    private fun volverDiaActual(){
        fechaActual = LocalDate.now()
        actualizarFecha()
        val hoy = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        cargarDesdeMock(hoy)
    }
    //Muestra calendario en boton seleccionar fecha para elegir cualquier fecha
    @SuppressLint("NewApi")
    private fun mostrarSeleccionFecha() {
        val hoy = LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                fechaActual = LocalDate.of(y, m + 1, d)
                actualizarFecha()
                val diaSel = fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                cargarDesdeMock(diaSel)         // ← solo mock
            },
            hoy.year, hoy.monthValue - 1, hoy.dayOfMonth
        ).show()
    }

    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }
    fun transformarItemASesion(compra: Compra, ocupacion: Ocupacion): Sesion {
        val item = compra.items.firstOrNull { it.id == ocupacion.idCompra } ?: compra.items.first()
        return Sesion(
            hora = item.start.substring(11, 16),
            calendario = item.idCalendario,
            nombre = compra.name,
            participantes = item.peopleNumber,
            totalPagado = item.priceTotal,
            estado = compra.status,
            idiomas = compra.language
        )
    }
}