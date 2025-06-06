package com.example.gestionreservas.view.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.gestionreservas.R
import com.example.gestionreservas.viewModel.Pagos.PagosViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FiltroPagosBottomSheetFragment(
    private val viewModel: PagosViewModel
) : BottomSheetDialogFragment() {

    private lateinit var spinnerTipoPago: Spinner
    private lateinit var spinnerEstadoPago: Spinner
    private lateinit var btnAplicar: TextView
    private lateinit var btnCancelar: TextView

    private val tiposPago = listOf("-- Todos --", "Web", "Local", "Efectivo")
    private val estadosPago = listOf("-- Todos --", "confirmado", "pendiente")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottomsheet_filtro_pagos, container, false)
        instancias(view)
        return view
    }

    private fun instancias(view: View) {
        spinnerTipoPago = view.findViewById(R.id.spinnerTipoPago)
        spinnerEstadoPago = view.findViewById(R.id.spinnerEstadoPago)
        btnAplicar = view.findViewById(R.id.btnAplicarFiltros)
        btnCancelar = view.findViewById(R.id.btnCancelarFiltros)

        // Adaptadores para los spinners
        val adapterTipos = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tiposPago)
        adapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoPago.adapter = adapterTipos

        val adapterEstados = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estadosPago)
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstadoPago.adapter = adapterEstados

        // Acción botón aplicar
        btnAplicar.setOnClickListener {
            val tipoSeleccionado = spinnerTipoPago.selectedItem.toString()
            val estadoSeleccionado = spinnerEstadoPago.selectedItem.toString()
            viewModel.actualizarFiltros(tipo = tipoSeleccionado, estado = estadoSeleccionado)

            dismiss()
        }

        // Acción botón cancelar
        btnCancelar.setOnClickListener {
            dismiss()
        }
    }
}
