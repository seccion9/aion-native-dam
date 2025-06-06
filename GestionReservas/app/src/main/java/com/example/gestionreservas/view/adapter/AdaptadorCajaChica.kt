package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.view.adapter.AdaptadorPagos.MyHolder

class AdaptadorCajaChica(
    private val context: Context,
    private var listaPagos: List<PagoCaja>,
) :RecyclerView.Adapter<AdaptadorCajaChica.MyHolder>(){

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var concepto=itemView.findViewById<TextView>(R.id.tvConceptoPago)
        var cantidad=itemView.findViewById<TextView>(R.id.tvCantidadPago)
        var fecha=itemView.findViewById<TextView>(R.id.tvFechaPago)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista =
            LayoutInflater.from(context).inflate(R.layout.pagos_caja_chica_item, parent, false)
        return MyHolder(vista)
    }

    override fun getItemCount(): Int {
        return listaPagos.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val item = listaPagos[position]

        holder.concepto.text = item.concepto
        holder.fecha.text = item.fecha

        val cantidadFloat = item.cantidad.toFloatOrNull() ?: 0f
        holder.cantidad.text = "%.2f€".format(cantidadFloat)

        when {
            cantidadFloat < 0 -> holder.cantidad.setTextColor(ContextCompat.getColor(context, R.color.pago_no_pagada))
            cantidadFloat > 0 -> holder.cantidad.setTextColor(ContextCompat.getColor(context, R.color.verdeItemsReservas))
            else -> holder.cantidad.setTextColor(ContextCompat.getColor(context, R.color.texto_secundario))
        }
    }

}