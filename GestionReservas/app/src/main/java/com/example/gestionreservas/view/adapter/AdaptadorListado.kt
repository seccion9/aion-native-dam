package com.example.gestionreservas.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.SesionConCompra


class AdaptadorListado(var context: Context,var listaReservas: MutableList<SesionConCompra> = mutableListOf()):
    RecyclerView.Adapter<AdaptadorListado.MyHolder>() {
    class MyHolder(itemView: View) : ViewHolder(itemView) {
        var statusItem=itemView.findViewById<TextView>(R.id.tvStatusCard)
        var pagoItem=itemView.findViewById<TextView>(R.id.tvPagoCard)
        var horaJuegoItem=itemView.findViewById<TextView>(R.id.tvFechaCard)
        var experienciaItem=itemView.findViewById<TextView>(R.id.tvExpCard)
        var calendarioItem=itemView.findViewById<TextView>(R.id.tvCalendarioCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista=LayoutInflater.from(context).inflate(R.layout.item_listado,parent,false)
        val holder:MyHolder=MyHolder(vista)
        return holder
    }

    override fun getItemCount(): Int {
       return listaReservas.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        Log.d("AdaptadorListado", "Mostrando: ${listaReservas[position].compra.name}")
        val sesionConCompra = listaReservas[position]
        val compra = sesionConCompra.compra
        val sesion = sesionConCompra.sesion
        val item = compra.items.first()


        holder.statusItem.text=item.status
        holder.horaJuegoItem.text=item.start.substring(0, 16)
        holder.experienciaItem.text=item.idExperience
        holder.calendarioItem.text=item.idCalendario
        holder.pagoItem.text=compra.status
        // Aplicar colores según estado
        if (item.status.equals("confirmada")) {
            holder.statusItem.setBackgroundColor(ContextCompat.getColor(context, R.color.verde_confirmado))
            holder.statusItem.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.horaJuegoItem.setBackgroundColor(ContextCompat.getColor(context, R.color.verde_suave))
            holder.experienciaItem.setBackgroundColor(ContextCompat.getColor(context, R.color.verde_suave))
            holder.calendarioItem.setBackgroundColor(ContextCompat.getColor(context, R.color.verde_suave))
            holder.pagoItem.setBackgroundColor(ContextCompat.getColor(context, R.color.casilla_pagado))
            holder.pagoItem.setTextColor(ContextCompat.getColor(context, R.color.white))
        }else if (item.status.equals("pendiente", ignoreCase = true)) {
            holder.statusItem.setBackgroundColor(ContextCompat.getColor(context, R.color.naranja_pendiente))
            holder.statusItem.setTextColor(ContextCompat.getColor(context, R.color.black))

            holder.horaJuegoItem.setBackgroundColor(ContextCompat.getColor(context, R.color.naranja_recycler))
            holder.horaJuegoItem.setTextColor(ContextCompat.getColor(context, R.color.black))

            holder.experienciaItem.setBackgroundColor(ContextCompat.getColor(context, R.color.naranja_recycler))
            holder.experienciaItem.setTextColor(ContextCompat.getColor(context, R.color.black))

            holder.calendarioItem.setBackgroundColor(ContextCompat.getColor(context, R.color.naranja_recycler))
            holder.calendarioItem.setTextColor(ContextCompat.getColor(context, R.color.black))

            holder.pagoItem.setBackgroundColor(ContextCompat.getColor(context, R.color.naranja_pendiente))
            holder.pagoItem.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

    }

    fun actualizarLista(nuevaLista: List<SesionConCompra>) {
        listaReservas.clear()
        listaReservas.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}