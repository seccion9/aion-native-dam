package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.databinding.FragmentCajaChicaBinding
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.view.adapter.AdaptadorCajaChica

class CajaChicaFragment : Fragment() {
    private lateinit var binding: FragmentCajaChicaBinding
    private lateinit var adaptadorCajaChica: AdaptadorCajaChica
    private lateinit var listaPagos: List<PagoCaja>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCajaChicaBinding.inflate(layoutInflater)
        instancias()
        return binding.root
    }

    private fun instancias() {
        var total = 0.0
        val listaPagosSimulados = listOf(
            PagoCaja("2025-06-06", "Reserva de Javier López", "75.00", "Efectivo", null),
            PagoCaja("2025-06-06", "Compra de snacks", "-12.00", "Manual", null),
            PagoCaja("2025-06-06", "Reserva de Lucía Fernández", "90.00", "Efectivo", null),
            PagoCaja("2025-06-06", "Reposición de folletos", "-6.50", "Manual", null),
            PagoCaja("2025-06-05", "Reserva de Sergio Martín", "120.00", "Tarjeta", null),
            PagoCaja("2025-06-05", "Gasto en limpieza", "-18.00", "Manual", null),
            PagoCaja("2025-06-05", "Reserva de Andrea Gómez", "50.00", "Efectivo", "true"),
            PagoCaja("2025-06-05", "Reembolso parcial", "-25.00", "Manual", "true"),
            PagoCaja("2025-06-06", "Reserva de Carlos López", "60.00", "Efectivo", null),
            PagoCaja("2025-06-06", "Impresión carteles", "-10.00", "Manual", null),
            PagoCaja("2025-06-06", "Reserva de Laura Díaz", "80.00", "Tarjeta", null),
            PagoCaja("2025-06-06", "Pago monitor extra", "-30.00", "Manual", null),
            PagoCaja("2025-06-06", "Reserva de Marco Ruiz", "100.00", "Efectivo", null),
            PagoCaja("2025-06-06", "Suministros varios", "-8.75", "Manual", null),
            PagoCaja("2025-06-06", "Reserva de Alicia Romero", "95.00", "Efectivo", null),
            PagoCaja("2025-06-06", "Reserva cancelada", "-50.00", "Manual", null),
            PagoCaja("2025-06-06", "Reserva de Enrique Navarro", "70.00", "Tarjeta", null),
            PagoCaja("2025-06-06", "Reembolso tarjeta", "-35.00", "Manual", "true"),
            PagoCaja("2025-06-06", "Reserva de Elena Mora", "85.00", "Efectivo", null),
            PagoCaja("2025-06-06", "Reposición de llaves", "-15.00", "Manual", null)
        )

        // Calculamos el total de caja
        listaPagosSimulados.forEach { pago ->
            total += pago.cantidad.toDoubleOrNull() ?: 0.0
        }

        // Mostramos el total formateado
        binding.cajaChicaItem.tvCajaTotal.text = String.format("%.2f€", total)

        adaptadorCajaChica = AdaptadorCajaChica(requireContext(), listaPagosSimulados)
        binding.recyclerPagosCaja.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerPagosCaja.adapter = adaptadorCajaChica
    }

}