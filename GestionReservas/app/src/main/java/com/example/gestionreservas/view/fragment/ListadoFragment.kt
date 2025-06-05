package com.example.gestionreservas.view.fragment


import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.viewModel.ListadosReservas.ListadoViewModelFactory
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentListadoBinding
import com.example.gestionreservas.models.entity.ExperienciaCompleta
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.models.repository.ExperienciaRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorListado
import com.example.gestionreservas.view.dialogs.OpcionesDialogFragment
import com.example.gestionreservas.viewModel.ListadosReservas.ListadoViewModel

class ListadoFragment: Fragment(),OnClickListener, AdapterView.OnItemSelectedListener {
    private lateinit var binding:FragmentListadoBinding
        private lateinit var adaptadorListado: AdaptadorListado
    private lateinit var listaSesiones: MutableList<SesionConCompra>
    private lateinit var adaptadorSpinner:ArrayAdapter<String>
    private val experienciaRepository= ExperienciaRepository(RetrofitFakeInstance.apiFake)
    private lateinit var listaExperiencias:MutableList<ExperienciaCompleta>
    private var listaEstadosReserva = arrayListOf("Todas", "Confirmada", "Pendiente", "No_finalizada", "Cancelada")
    private lateinit var viewModel: ListadoViewModel
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private lateinit var token:String
    private var refrescoManual = false



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoBinding.inflate(layoutInflater)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Listado Reservas"
        token=getTokenFromSharedPreferences()
        viewModel = ViewModelProvider(this, ListadoViewModelFactory(compraRepository,experienciaRepository))[ListadoViewModel::class.java]

        instancias()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        //Obtenemos compras y experiencias de inicio
        viewModel.obtenerDatosCompras(token)
        viewModel.obtenerExperiencias(token)
        //Instancias listeners
        binding.layoutSpinnerContainer.setOnClickListener(this)
        binding.spinnerEstadoReserva.onItemSelectedListener = this

        listaExperiencias= mutableListOf()

        //Instancias los observers
        observersViewModel()
        //Adaptador para añadir lista al spinner
        adaptadorSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listaEstadosReserva
        )
        adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEstadoReserva.adapter = adaptadorSpinner

        //Adaptador listado
        adaptadorListado = AdaptadorListado(requireContext(), mutableListOf(),
            listaExperiencias
        ) { sesionConCompra ->
            val bundle=Bundle()
            bundle.putSerializable("sesionConCompra",sesionConCompra)
            val dialog = OpcionesDialogFragment.newInstance(sesionConCompra)
            dialog.show(parentFragmentManager, "OpcionesDialog")
        }
        binding.recyclerReservasListado.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.recyclerReservasListado.adapter = adaptadorListado

        //Animacion para cargar datos
        binding.recyclerReservasListado.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fade_in)
        //Actualiza reservas en tiempo real filtrando por nombre
        binding.editTextBuscarNombre.doOnTextChanged { texto, _, _, _ ->
            viewModel.actualizarNombreBusqueda(texto.toString())
        }


        //Refrescar vista
        binding.swipeRefreshLayout.setOnRefreshListener {
            refrescoManual = true
            refrescarListado()
        }

    }

    /**
     * Observers del viewmodel
     */
    private fun observersViewModel(){
        viewModel.sesiones.observe(viewLifecycleOwner){sesiones ->
            listaSesiones = sesiones.toMutableList()
            adaptadorListado.actualizarLista(listaSesiones)
            binding.recyclerReservasListado.visibility = View.VISIBLE
            binding.recyclerReservasListado.scheduleLayoutAnimation()
        }
        viewModel.listaFiltrada.observe(viewLifecycleOwner) { lista ->
            adaptadorListado.actualizarLista(lista)

        }
        viewModel.experiencias.observe(viewLifecycleOwner) { experiencias ->
            listaExperiencias = experiencias.toMutableList()
            adaptadorListado.actualizarExperiencias(listaExperiencias)
        }
        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.swipeRefreshLayout.isRefreshing = cargando
            if (!cargando && refrescoManual) {
                Toast.makeText(requireContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                refrescoManual = false
            }
        }



    }

    /**
     * Métodos on click
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.layoutSpinnerContainer.id->{
                binding.spinnerEstadoReserva.performClick()
            }
        }
    }
    //Obtenemos token de preferencias
    private fun getTokenFromSharedPreferences(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", "") ?: ""
    }

    /**
     * Metodos para filtrar el spinner segun vayamos seleccionando opción.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val estadoSeleccionado = listaEstadosReserva[position]
        viewModel.actualizarEstadoSeleccionado(estadoSeleccionado)
    }

    override fun onNothingSelected(parent: AdapterView<*>?){}

    //Método para refrescar datos de compras a través del viewmodel.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun refrescarListado() {
        viewModel.obtenerDatosCompras(token)
    }



}