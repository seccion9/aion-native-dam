package com.example.gestionreservas.view.fragment


import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.viewModel.listado.ListadosReservas.ListadoViewModelFactory
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentListadoBinding
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.utils.SalaHelper
import com.example.gestionreservas.view.adapter.AdaptadorCompra
import com.example.gestionreservas.view.adapter.AdaptadorListado
import com.example.gestionreservas.view.dialogs.OpcionesDialogFragment
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.gestionreservas.viewModel.listado.ListadosReservas.ListadoViewModel

class ListadoFragment: Fragment(),OnClickListener, AdapterView.OnItemSelectedListener {
    private lateinit var binding:FragmentListadoBinding
        private lateinit var adaptadorListado: AdaptadorListado
    private lateinit var listaSesiones: MutableList<SesionConCompra>
    private lateinit var adaptadorSpinner:ArrayAdapter<String>
    private var listaEstadosReserva = arrayListOf("Todas", "Confirmada", "Pendiente", "No finalizada", "Cancelada")
    private lateinit var viewModel: ListadoViewModel
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private lateinit var token:String



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoBinding.inflate(layoutInflater)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Listado Reservas"
        token=getTokenFromSharedPreferences()
        viewModel = ViewModelProvider(this, ListadoViewModelFactory(compraRepository))[ListadoViewModel::class.java]
        viewModel.obtenerDatosCompras(token)
        instancias()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        binding.layoutSpinnerContainer.setOnClickListener(this)
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
            emptyList()
        ) { sesionConCompra ->
            val bundle=Bundle()
            bundle.putSerializable("sesionConCompra",sesionConCompra)
            val dialog = OpcionesDialogFragment.newInstance(sesionConCompra)
            dialog.show(parentFragmentManager, "OpcionesDialog")
        }
        binding.recyclerReservasListado.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.recyclerReservasListado.adapter = adaptadorListado

    }
    private fun observersViewModel(){
        viewModel.sesiones.observe(viewLifecycleOwner){sesiones ->
            listaSesiones = sesiones.toMutableList()
            adaptadorListado.actualizarLista(listaSesiones)
            binding.recyclerReservasListado.visibility = View.VISIBLE

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.layoutSpinnerContainer.id->{
                binding.spinnerEstadoReserva.performClick()
            }
        }
    }
    private fun getTokenFromSharedPreferences(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", "") ?: ""
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?){}


}