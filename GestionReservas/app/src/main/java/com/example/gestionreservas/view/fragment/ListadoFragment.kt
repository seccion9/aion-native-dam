package com.example.gestionreservas.view.fragment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentListadoBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.network.RetrofitInstance
import com.example.gestionreservas.view.adapter.AdaptadorListado
import kotlinx.coroutines.launch
import java.time.LocalDate

class ListadoFragment: Fragment(),OnClickListener {
    private lateinit var binding:FragmentListadoBinding
    private var listaCompras:ArrayList<SesionConCompra> = arrayListOf()
    private lateinit var adaptadorListado:AdaptadorListado
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaActual: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoBinding.inflate(layoutInflater)
        instancias()
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        binding.tvListadoDia.setOnClickListener(this)
        binding.tvListadoSemanal.setOnClickListener(this)
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        // Crear adaptador vacío
        adaptadorListado = AdaptadorListado(requireContext())
        binding.recyclerReservasListado.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerReservasListado.adapter = adaptadorListado

        // Cargar datos del día
        cargarDatosSesionesHoy()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarDatosSesionesHoy() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val compras = RetrofitFakeInstance.apiFake.getPurchases()
                Log.d("Fake API", "Datos obtenidos: $compras")

                val sesiones = transformarComprasASesiones(compras, fechaActual)
                Log.d("ListadoFragment", "Sesiones transformadas: ${sesiones.size}")

                // Aquí es donde actualizas el adaptador directamente
                adaptadorListado.actualizarLista(sesiones)

            } catch (e: Exception) {
                Log.e("ListadoFragment", "Error en la API fake: ${e.localizedMessage}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun transformarComprasASesiones(
        compras: List<Compra>,
        fechaSeleccionada: LocalDate
    ): List<SesionConCompra> {
        val sesiones = mutableListOf<SesionConCompra>()

        compras.forEach { compra ->
            compra.items.forEach { item ->
                val fechaItem = LocalDate.parse(item.start.substring(0, 10))

                if (fechaItem == fechaSeleccionada) {
                    val sesion = Sesion(
                        hora = item.start.substring(11, 16),
                        calendario = item.idCalendario,
                        nombre = compra.name,
                        participantes = item.peopleNumber,
                        totalPagado = item.priceTotal,
                        estado = compra.status,
                        idiomas = compra.language
                    )
                    sesiones.add(SesionConCompra(sesion, compra))
                }
            }
        }

        return sesiones
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvListadoDia.id->{

            }
            binding.tvListadoSemanal.id->{
                val fragment=ListadoSemanalFragment()
                val transaction=parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                transaction.commit()
            }
            binding.tvFlechaIzquierdaHoy.id->{
                fechaActual=fechaActual.minusDays(1)
                actualizarFecha()
                cargarDatosSesionesHoy()
            }
            binding.tvFlechaDerechaHoy.id->{
                fechaActual=fechaActual.plusDays(1)
                actualizarFecha()
                cargarDatosSesionesHoy()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarFecha(){
        val dia = fechaActual.dayOfMonth
        val mes = fechaActual.month.value

        val fechaFormateada = "$dia/$mes"
        binding.tvFecha.text = fechaFormateada
    }
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }
}