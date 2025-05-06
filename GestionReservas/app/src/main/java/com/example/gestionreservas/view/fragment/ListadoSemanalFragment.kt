package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentListadoBinding
import com.example.gestionreservas.databinding.FragmentListadoSemanalBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorListado
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class ListadoSemanalFragment:Fragment(),OnClickListener {
    private lateinit var binding: FragmentListadoSemanalBinding
    private lateinit var adaptadorListado:AdaptadorListado

    @SuppressLint("NewApi")
    private var fechaActual=LocalDate.now()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoSemanalBinding.inflate(layoutInflater)
        instancias()
        return binding.root
    }
    private fun instancias(){
        calcularFechaActual()
        //Instancias click
        binding.tvListadoDia.setOnClickListener(this)
        //Adaptador
        adaptadorListado=AdaptadorListado(requireContext())
        binding.recyclerReservasListado.layoutManager=
            LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.recyclerReservasListado.adapter=adaptadorListado
        //cargamos datos sesiones semana
        obtenerListadoReservasSemanal()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvListadoDia.id->{
                val fragment=ListadoFragment()
                val transaction=parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                transaction.commit()
            }
            binding.tvListadoSemanal.id->{

            }
        }
    }
    private fun obtenerListadoReservasSemanal(){
        val token=getTokenFromSharedPreferences()
        viewLifecycleOwner.lifecycleScope.launch {
            try{
                calcularFechaActual()
                val reservas=RetrofitFakeInstance.apiFake.getPurchases(token.toString())
                val sesiones=transformarComprasASesiones(reservas)
                adaptadorListado.actualizarLista(sesiones)
            }catch (e:Exception){
                Log.e("ListadoFragmentSemanal","Error en la API fake ${e.localizedMessage}")
            }
        }
    }
    /*
    Esta funcion nos devolvera una lista de todas las sesiones de la semana que se mostraran en
    nuestro recyclerview
     */
    @SuppressLint("NewApi")
    private fun transformarComprasASesiones(compras: List<Compra>):List<SesionConCompra> {
        val sesiones = mutableListOf<SesionConCompra>()
        val hoy = LocalDate.now()
        //Obtenemos el lunes y domingo de la semana
        val lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val domingo = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        //Iteramos en la lista de compras que nos devuelve la API y sacamos los datos semanales
        for (compra in compras) {
            for (item in compra.items) {
                val fechaItem = LocalDate.parse(item.start.substring(0, 10))

                if (!fechaItem.isBefore(lunes) && !fechaItem.isAfter(domingo)) {
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
    @SuppressLint("NewApi")
    private fun calcularFechaActual(){
        val hoy=fechaActual
        // Lunes de esta semana
        val lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val domingo = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val texto = "${lunes.dayOfMonth}-${domingo.dayOfMonth}/${hoy.monthValue}"
        binding.tvFechaSemana.text = texto
    }
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }
}