package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context.MODE_PRIVATE
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentCalendarioDiarioBinding
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.models.entity.Bloqueo
import com.example.gestionreservas.models.entity.FranjaHorariaReservas
import com.example.gestionreservas.models.repository.CalendarioRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorFranjasHorarias
import com.example.gestionreservas.view.adapter.AdaptadorSalasPorHora
import com.example.gestionreservas.viewModel.CalendarioDiario.CalendarioDiarioViewModel
import com.example.gestionreservas.viewModel.CalendarioDiario.CalendarioDiarioViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID


class CalendarioFragmentDiario : Fragment(), OnClickListener {
    private lateinit var binding: FragmentCalendarioDiarioBinding
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private val calendarioRepository = CalendarioRepository(RetrofitFakeInstance.apiFake)
    private val fechasSeleccionadas = mutableListOf<LocalDate>()
    private lateinit var viewModel: CalendarioDiarioViewModel
    private var listaFranjas = mutableListOf<FranjaHorariaReservas>()
    private lateinit var adaptadorFranjasHorarias: AdaptadorFranjasHorarias
    private lateinit var adaptadorSalasPorHora: AdaptadorSalasPorHora


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

    /**
     * Carga los observers del viewmodel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarObservers() {
        //Actualiza la fecha y carga sesiones y bloqueos.
        viewModel.fechaActual.observe(viewLifecycleOwner) { fecha ->
            actualizarFecha(fecha)
            val token = getTokenFromSharedPreferences() ?: return@observe
            viewModel.cargarSesionesDesdeMock(token, fecha)
            viewModel.obtenerBloqueos(token)
        }
        //Obtiene token crea el bloqueo y refresca los datos de la vista
        viewModel.bloqueoExitoso.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Bloqueo creado con éxito", Toast.LENGTH_SHORT)
                    .show()
                val token = getTokenFromSharedPreferences()
                val fecha = viewModel.fechaActual.value
                if (token != null && fecha != null) {
                    viewModel.obtenerBloqueos(token)
                    viewModel.cargarSesionesDesdeMock(token, fecha)
                }

            } else {
                Toast.makeText(requireContext(), "Error al crear el bloqueo", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        //Actualiza la lista del adaptador y usa la animación
        viewModel.franjasHorarias.observe(viewLifecycleOwner) { lista ->
            Log.d("FRAGMENT", "Recibidas ${lista.size} franjas horarias")
            adaptadorFranjasHorarias.actualizarLista(lista)
            binding.recyclerHorasSalas.scheduleLayoutAnimation()
            binding.swipeRefreshLayout.isRefreshing = false

        }

        viewModel.bloqueos.observe(viewLifecycleOwner) { bloqueos ->
            Log.d("FRAGMENT_BLOQUEOS", "Bloqueos recibidos: ${bloqueos?.size}")
            bloqueos?.forEach { bloqueo ->
                Log.d("FRAGMENT_BLOQUEOS", "Bloqueo -> id: ${bloqueo.id}, tipo: ${bloqueo.tipo}, salas: ${bloqueo.salas}, inicio: ${bloqueo.inicio}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun instancias() {
        //Instancias click
        instanciasListeners()
        listaFranjas = mutableListOf()
        Log.d("CALENDARIO_FRAGMENT", "Recibidas ${listaFranjas.size} franjas horarias")

        //Adaptador franjas
        adaptadorFranjasHorarias = AdaptadorFranjasHorarias(
            context = requireContext(),
            listaFranjas = listaFranjas,
            onClickCrearReserva = {//Si das a crear reserva te lleva a esa vista
                val fragment = PostPurchaseFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onItemClick = {compraSeleccionada ->
                val bundle = Bundle().apply {//Te lleva a la vista reserva al pinchar en el
                    putSerializable("compra", compraSeleccionada)
                }
                val fragment = DetalleSesionFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal, fragment)
                    .addToBackStack(null)
                    .commit()
            })

        binding.recyclerHorasSalas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHorasSalas.adapter = adaptadorFranjasHorarias
        //Animación del recycler
        val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fade_in)
        binding.recyclerHorasSalas.layoutAnimation = controller
        //Refresca pantalla al inicio.
        binding.swipeRefreshLayout.setOnRefreshListener {
            refrescarTodaLaPantalla()
        }

    }
    //Actualiza la fecha del textview según nos movamos por la vista
    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarFecha(fecha: LocalDate) {
        val diaSemana = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        val dia = fecha.dayOfMonth
        val mes = fecha.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        val anio = fecha.year
        val fechaFormateada =
            "${diaSemana.replaceFirstChar { it.titlecase(Locale("es", "ES")) }} $dia $mes $anio"
        binding.tvFecha.text = fechaFormateada

    }

    //Funciones on click de nuestro fragment
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvFlechaIzquierdaHoy.id -> viewModel.retrocederDia()
            binding.tvFlechaDerechaHoy.id -> viewModel.avanzarDia()
            //binding.btnBloquear.id -> cambiarFragment(PostPurchaseFragment())
            binding.btnBloqueoMasivo.id -> mostrarDialogoBloqueo()
            binding.selectFecha.id -> mostrarSeleccionFecha()
            binding.btnZombieRoom.id -> {
                val bundle = Bundle()
                bundle.putString("idExperience", "exp_zombie_2")
                bundle.putString("nombreExperience", "Zombie Room")
                val fragment = ListadoFragment()
                fragment.arguments = bundle
                cambiarFragment(fragment)
            }
            binding.tvHoy.id ->{
                viewModel.irAHoy()
            }
            binding.btnEscapeJungle.id -> {
                val bundle = Bundle()
                bundle.putString("idExperience", "exp_jungle_1")
                bundle.putString("nombreExperience", "Escape Jungle")
                val fragment = ListadoFragment()
                fragment.arguments = bundle
                cambiarFragment(fragment)
            }
        }
    }

    /**
     * Muestra el dialogo de bloqueo para seleccionar las salas,obtiene info de el(motivo,fecha,etc)
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarDialogoBloqueo() {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialogo_bloqueo, null)

        seleccionSalas(view)

        val radioFranja: RadioButton = view.findViewById(R.id.radioFranjaHoraria)
        val radioDiaEntero: RadioButton = view.findViewById(R.id.radioDiaEntero)
        val editMotivo: EditText = view.findViewById(R.id.EditMotivo)
        val editFechaInicio: EditText = view.findViewById(R.id.editFechaInicio)
        val editFechaFin: EditText = view.findViewById(R.id.editFechaFin)
        val contenedorCheckbox = view.findViewById<LinearLayout>(R.id.contenedorCheckboxSalas)

        editFechaInicio.setOnClickListener {
            mostrarDateTimePicker(radioFranja) { fechaHora ->
                Log.e("DATE PICKER", "Fecha: $fechaHora")
                editFechaInicio.text.clear()
                editFechaInicio.setText(fechaHora)
            }
        }

        editFechaFin.setOnClickListener {
            mostrarDateTimePicker(radioFranja) { fechaHora ->
                Log.e("DATE PICKER", "Fecha: $fechaHora")
                editFechaFin.text.clear()
                editFechaFin.setText(fechaHora)
            }
        }
        //Si se rellena todo creamos el bloqueo y lo registramos a través del viewmodel.
        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Aceptar") { _, _ ->
                val tipo = if (radioFranja.isChecked) "franja" else "dia_entero"
                val motivo = editMotivo.text.toString().takeIf { it.isNotBlank() }
                val inicio = editFechaInicio.text.toString()
                val fin = editFechaFin.text.toString()

                // Obtener salas seleccionadas
                val salasSeleccionadas = mutableListOf<String>()
                for (i in 0 until contenedorCheckbox.childCount) {
                    val cb = contenedorCheckbox.getChildAt(i)
                    if (cb is CheckBox && cb.text != "Seleccionar todas" && cb.isChecked) {
                        salasSeleccionadas.add(cb.text.toString())
                    }
                }

                if (salasSeleccionadas.isEmpty()) {
                    Toast.makeText(requireContext(), "Debes seleccionar al menos una sala", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val bloqueo = Bloqueo(
                    id = UUID.randomUUID().toString(),
                    salas = salasSeleccionadas,
                    tipo = tipo,
                    inicio = inicio,
                    fin = fin,
                    motivo = motivo
                )

                val token = getTokenFromSharedPreferences()
                if (token != null) {
                    Log.d("BLOQUEAR_FECHA", "Bloqueo: $bloqueo")
                    viewModel.registrarBloqueo(token, bloqueo)
                } else {
                    Toast.makeText(requireContext(), "Token no disponible", Toast.LENGTH_SHORT).show()
                }

            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    /**
     * DatePicker para seleccionar día y hora de los bloqueos
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarDateTimePicker(
        radio: RadioButton,
        onDateTimeSelected: (String) -> Unit
    ) {
        val hoy = java.util.Calendar.getInstance()

        DatePickerDialog(requireContext(), { _, y, m, d ->
            val fechaSeleccionada = LocalDate.of(y, m + 1, d)

            if (radio.isChecked) {

                TimePickerDialog(requireContext(), { _, h, min ->
                    val fechaHora = String.format("%04d-%02d-%02d %02d:%02d", y, m + 1, d, h, min)
                    onDateTimeSelected(fechaHora)
                }, hoy.get(Calendar.HOUR_OF_DAY), hoy.get(Calendar.MINUTE), true).show()
            } else {

                val soloFecha = fechaSeleccionada.toString()
                onDateTimeSelected(soloFecha)
            }

        }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)).show()
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

    /**
     * Cambia de fragment a otro
     */
    private fun cambiarFragment(fragment: Fragment) {
        val transacion = parentFragmentManager.beginTransaction()
        transacion.replace(R.id.fragment_principal, fragment)
        transacion.addToBackStack(null)
        transacion.commit()
    }
    //Instancias listeners
    private fun instanciasListeners() {
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        binding.btnZombieRoom.setOnClickListener(this)
        binding.btnEscapeJungle.setOnClickListener(this)
        binding.selectFecha.setOnClickListener(this)
        binding.tvHoy.setOnClickListener(this)
        //binding.btnBloquear.setOnClickListener(this)
        binding.btnBloqueoMasivo.setOnClickListener(this)
    }

    private fun seleccionSalas(view: View) {
        val listaSalas = listOf("Sala 1", "Sala 2", "Sala 3", "Sala 4", "Sala 5", "Sala 6", "Sala 7", "Sala 8")
        val contenedorCheckbox = view.findViewById<LinearLayout>(R.id.contenedorCheckboxSalas)
        val checkboxes = mutableListOf<CheckBox>()

        val checkboxTodas = CheckBox(requireContext()).apply {
            text = "Seleccionar todas"
            setTypeface(null, Typeface.BOLD)
        }
        contenedorCheckbox.addView(checkboxTodas)

        // Añadir checkboxes individuales
        listaSalas.forEach { sala ->
            val cb = CheckBox(requireContext()).apply { text = sala }
            contenedorCheckbox.addView(cb)
            checkboxes.add(cb)
        }

        // Lógica de seleccionar todas
        checkboxTodas.setOnCheckedChangeListener { _, isChecked ->
            checkboxes.forEach { it.isChecked = isChecked }
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun refrescarTodaLaPantalla() {
        binding.swipeRefreshLayout.isRefreshing = true


        viewLifecycleOwner.lifecycleScope.launch {
            val token = getTokenFromSharedPreferences()
            val fecha = viewModel.fechaActual.value
            if (token != null && fecha != null) {
                viewModel.cargarSesionesDesdeMock(token, fecha)
                viewModel.obtenerBloqueos(token)
            }
            kotlinx.coroutines.delay(1000)
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show()
        }
    }
}