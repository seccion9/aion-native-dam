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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.example.gestionreservas.models.entity.Bloqueo
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.Ocupacion
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.viewModel.listado.CalendarioDiario.CalendarioDiarioViewModel
import com.example.gestionreservas.viewModel.listado.CalendarioDiario.CalendarioDiarioViewModelFactory
import kotlinx.coroutines.launch
import java.util.UUID


class CalendarioFragmentDiario : Fragment(), OnClickListener {
    private lateinit var binding: FragmentCalendarioDiarioBinding
    private val reservasPorDia: MutableMap<LocalDate, List<HoraReserva>> = mutableMapOf()
    private lateinit var listaReservaHoras: ArrayList<HoraReserva>
    private lateinit var adaptadorHoraReserva: AdaptadorHoraReserva
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private val calendarioRepository = CalendarioRepository(RetrofitFakeInstance.apiFake)
    private val fechasSeleccionadas = mutableListOf<LocalDate>()
    private lateinit var viewModel: CalendarioDiarioViewModel


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout del fragmento para que cargue la vista correctamente
        binding = FragmentCalendarioDiarioBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Calendario Diario"

        val factory = CalendarioDiarioViewModelFactory(compraRepository, calendarioRepository)
        viewModel = ViewModelProvider(this, factory)[CalendarioDiarioViewModel::class.java]


        instancias()
        /*recibimos por argumentos de nuestro fragment semanal y mensual la fecha seleccionada
          si no es nula llamamos a la funcion actualizar fecha para obtener la
          fecha que se selecciono y sus horarios.
          Si es nula se obtiene la fecha de hoy
         */
        val fechaString = arguments?.getString("fechaSeleccionada") ?: arguments?.getString("fecha")
        if (fechaString != null) {
            val fecha = LocalDate.parse(fechaString)
            viewModel.seleccionarFecha(fecha)
        } else {
            viewModel.irAHoy()
        }

        cargarObservers()
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarObservers(){
        viewModel.horas.observe(viewLifecycleOwner) {
            adaptadorHoraReserva.actualizarLista(it)
        }
        viewModel.sesiones.observe(viewLifecycleOwner) {
            // Si necesitas usarlas para algo, por ejemplo debug o lógica de detalle
            Log.d("SesionesViewModel", "Cargadas ${it.size} sesiones")
        }
        viewModel.fechaActual.observe(viewLifecycleOwner) { fecha ->
            actualizarFecha(fecha)
            val token = getTokenFromSharedPreferences() ?: return@observe
            viewModel.cargarSesionesDesdeMock(token, fecha)
        }
        viewModel.bloqueoExitoso.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Bloqueos guardados correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun instancias() {
        //Instancias nuestro adaptador y recycler
        adaptadorYlogicaDetalles()
        //Instancias click
        instanciasListeners()
    }

    /**
     * Muestra un dialogo en donde el usuario decidirá si bloquear varios dias o solo uno.
     * Se da la opción de cancelar el dialogo.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarDialogoBloqueo() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Cúantos días quieres bloquear?")
            .setItems(arrayOf("Un solo día", "Varios días")) { _, which ->
                when (which) {
                    0 -> mostrarSelectorFecha(bloqueoUnico = true)
                    1 -> mostrarSelectorFecha(bloqueoUnico = false)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun mostrarDialogoSalaYMotivo(onConfirmar: (List<String>, String?) -> Unit){

    val salas = arrayOf("cal1", "cal2")
        val seleccionadas = booleanArrayOf(true, true)

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputMotivo = EditText(requireContext()).apply {
            hint = "Motivo (opcional)"
        }

        layout.addView(inputMotivo)

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona salas a bloquear")
            .setMultiChoiceItems(salas, seleccionadas) { _, which, isChecked ->
                seleccionadas[which] = isChecked
            }
            .setView(layout)
            .setPositiveButton("Confirmar") { _, _ ->
                val salasElegidas = salas.filterIndexed { index, _ -> seleccionadas[index] }
                if (salasElegidas.isEmpty()) {
                    // Evita continuar si no se selecciona ninguna sala
                    Toast.makeText(requireContext(), "Debes seleccionar al menos una sala", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val motivo = inputMotivo.text.toString().ifBlank { null }
                onConfirmar(salasElegidas, motivo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra el datePickerDialog para seleccionar fecha,si selecciono bloqueo de un ida muestra un resumen
     * de las fechas seleccionadas,si no muestra otro dialogo hasta que decida no añadir mas fechas.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarSelectorFecha(bloqueoUnico: Boolean) {
        val hoy = LocalDate.now()
        val picker = DatePickerDialog(requireContext(), { _, year, month, day ->
            val fecha = LocalDate.of(year, month + 1, day)
            if (!fechasSeleccionadas.contains(fecha)) {
                fechasSeleccionadas.add(fecha)
            }

            if (bloqueoUnico) {
                mostrarResumenFechasSeleccionadas()
            } else {
                mostrarDialogoContinuarSeleccion()
            }

        }, hoy.year, hoy.monthValue - 1, hoy.dayOfMonth)

        picker.show()
    }

    /**
     * Si seleccionó varias fechas se le muestra dialogo para continuar seleccionando o no,si pincha en no
     * muestra resumen de fechas seleccionadas,si es si muestra otro datepicker para seleccionar otra fecha.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarDialogoContinuarSeleccion() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Seleccionar otra fecha?")
            .setMessage("¿Quieres añadir otra fecha al bloqueo?")
            .setPositiveButton("Sí") { _, _ -> mostrarSelectorFecha(bloqueoUnico = false) }
            .setNegativeButton("No") { _, _ -> mostrarResumenFechasSeleccionadas() }
            .show()
    }

    /**
     * Dialogo que muestra las fechas seleccionadas para bloquear por el usuario,puede decidir si aceptar
     * y las bloqueara o si decide cancelarlo.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarResumenFechasSeleccionadas() {
        if (fechasSeleccionadas.isEmpty()) return

        mostrarDialogoSalaYMotivo { salasSeleccionadas, motivo ->
            val mensaje = fechasSeleccionadas.joinToString("\n") { it.toString() }
            val salasTexto = salasSeleccionadas.joinToString(", ")

            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar bloqueo")
                .setMessage("¿Quieres bloquear los siguientes días?\n\n$mensaje\n\nSalas: $salasTexto\nMotivo: ${motivo ?: "Sin especificar"}")
                .setPositiveButton("Confirmar") { _, _ ->

                    val listaBloqueos = mutableListOf<Bloqueo>()

                    salasSeleccionadas.forEach { sala ->
                        fechasSeleccionadas.forEach { fecha ->
                            val bloqueo = Bloqueo(
                                id = UUID.randomUUID().toString(),
                                calendarioId = sala,
                                fecha = fecha.toString(), // yyyy-MM-dd
                                motivo = motivo
                            )
                            listaBloqueos.add(bloqueo)
                        }
                    }

                    bloquearFechasSeleccionadas(listaBloqueos)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    /**Obtenemos nuestro token y con la fecha obtenida por parametros cargamos los datos del dia
     *despues actualizamos nuestro adaptador para notificarle que cambiaron los datos,esta funcion nos
     *vale cada vez que cambiemos de fecha
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun adaptadorYlogicaDetalles() {
        listaReservaHoras = arrayListOf()
        adaptadorHoraReserva = AdaptadorHoraReserva(
            requireContext(), listaReservaHoras
        ) { hora: HoraReserva, calendarioId: String ->

            /*Buscamos en nuestra lista ocupaciones si nuetro item coincide en hora y calendario
               con el seleccionado.
             */
            val ocupacion = viewModel.obtenerOcupacion(hora.horaInicio, calendarioId)

            val token = getTokenFromSharedPreferences()
            if (token != null) {
                viewModel.obtenerSesionConCompraDesdeOcupacion(token, hora.horaInicio, calendarioId, ocupacion)
                    .observe(viewLifecycleOwner) { sesionConCompra ->
                        val fragment: Fragment = if (sesionConCompra.compra != null) {
                            DetalleSesionFragment()
                        } else {
                            PostPurchaseFragment()
                        }

                        val bundle = Bundle()
                        bundle.putSerializable("sesionConCompra", sesionConCompra)
                        fragment.arguments = bundle
                        cambiarFragment(fragment)
                    }
            }
        }
        binding.recyclerHorasSalas.apply {
            adapter = adaptadorHoraReserva
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bloquearFechasSeleccionadas(bloqueos: List<Bloqueo>) {
        val token = getTokenFromSharedPreferences() ?: return

        val ocupaciones = viewModel.ocupaciones.value.orEmpty()
        val bloqueosPorSala = bloqueos.groupBy { it.calendarioId }

        val ocupacionesAEliminar = ocupaciones.filter { o ->
            bloqueos.any { b -> b.fecha == o.date && b.calendarioId == o.calendarioId }
        }

        if (ocupacionesAEliminar.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("Se eliminarán ${ocupacionesAEliminar.size} reservas por conflicto con bloqueos. ¿Deseas continuar?")
                .setPositiveButton("Sí") { _, _ ->
                    lifecycleScope.launch {
                        val ok = viewModel.bloquearYEliminar(requireContext(), token, bloqueos)
                        if (ok) {
                            Toast.makeText(requireContext(), "Bloqueos y eliminaciones aplicados", Toast.LENGTH_SHORT).show()
                            val fechaActual = viewModel.fechaActual.value
                            if (fechaActual != null) {
                                viewModel.cargarSesionesDesdeMock(token, fechaActual)
                            }
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            lifecycleScope.launch {
                val ok = viewModel.bloquearYEliminar(requireContext(), token, bloqueos)
                if (ok) {
                    Toast.makeText(requireContext(), "Bloqueos aplicados sin conflictos", Toast.LENGTH_SHORT).show()
                    val fechaActual = viewModel.fechaActual.value
                    if (fechaActual != null) {
                        viewModel.cargarSesionesDesdeMock(token, fechaActual)
                    }
                }
            }
        }

        fechasSeleccionadas.clear()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarFecha(fecha: LocalDate) {
        val diaSemana = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        val dia = fecha.dayOfMonth
        val mes = fecha.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        val anio = fecha.year
        val fechaFormateada = "${diaSemana.replaceFirstChar { it.titlecase(Locale("es", "ES")) }} $dia $mes $anio"
        binding.tvFecha.text = fechaFormateada

        val reservasDelDia = reservasPorDia[fecha] ?: emptyList()
        adaptadorHoraReserva.actualizarLista(reservasDelDia)
    }

    //Funciones on click de nuestro fragment
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvFlechaIzquierdaHoy.id -> viewModel.retrocederDia()
            binding.tvFlechaDerechaHoy.id -> viewModel.avanzarDia()
            binding.tvHoy.id, binding.tvDiario.id -> viewModel.irAHoy()
            binding.tvSemana.id -> cambiarFragment(CalendarioFragmentSemana())
            binding.btnBloquear.id -> cambiarFragment(PostPurchaseFragment())
            binding.btnBloqueoMasivo.id -> mostrarDialogoBloqueo()
            binding.tvMes.id ->  cambiarFragment(CalendarioFragment())
            binding.selectFecha.id ->  mostrarSeleccionFecha()
            binding.btnZombieRoom.id -> {
                val bundle = Bundle()
                bundle.putString("idExperience", "exp_zombie_2")
                bundle.putString("nombreExperience", "Zombie Room")
                val fragment = ListadoFragment()
                fragment.arguments = bundle
                cambiarFragment(fragment)
            }
            binding.btnEscapeJungle.id -> {
                val bundle = Bundle()
                bundle.putString("idExperience", "exp_jungle_1")
                bundle.putString("nombreExperience", "Escape Jungle")
                val fragment = ListadoFragment()
                fragment.arguments = bundle
                cambiarFragment(fragment)
            }
            binding.tvRecargar.id -> {
                val token = getTokenFromSharedPreferences()
                val fecha = viewModel.fechaActual.value

                if (token != null && fecha != null) {
                    viewModel.cargarSesionesDesdeMock(token, fecha)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo recargar. Token o fecha no disponibles.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    //Muestra calendario en boton seleccionar fecha para elegir cualquier fecha
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NewApi")
    private fun mostrarSeleccionFecha() {
        val hoy = LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val seleccionada = LocalDate.of(y, m + 1, d)
                viewModel.seleccionarFecha(seleccionada)
            },
            hoy.year, hoy.monthValue - 1, hoy.dayOfMonth
        ).show()
    }

    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    private fun cambiarFragment(fragment: Fragment) {
        val transacion = parentFragmentManager.beginTransaction()
        transacion.replace(R.id.fragment_principal, fragment)
        transacion.addToBackStack(null)
        transacion.commit()
    }

    private fun instanciasListeners() {
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        binding.tvMes.setOnClickListener(this)
        binding.btnZombieRoom.setOnClickListener(this)
        binding.btnEscapeJungle.setOnClickListener(this)
        binding.selectFecha.setOnClickListener(this)
        binding.tvHoy.setOnClickListener(this)
        binding.tvDiario.setOnClickListener(this)
        binding.tvRecargar.setOnClickListener(this)
        binding.tvSemana.setOnClickListener(this)
        binding.btnBloquear.setOnClickListener(this)
        binding.btnBloqueoMasivo.setOnClickListener(this)
    }
}