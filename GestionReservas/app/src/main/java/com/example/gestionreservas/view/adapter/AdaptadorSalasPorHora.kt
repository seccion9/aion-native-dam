package com.example.gestionreservas.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.EstadoSala
import com.example.gestionreservas.models.entity.SalaConEstado

class AdaptadorSalasPorHora(
    private val context: Context,
    private val listaReservas: MutableList<Compra>,
    private val onItemClick: (Compra) -> Unit = {}
) :RecyclerView.Adapter<AdaptadorSalasPorHora.MyHolder>(){

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvNombre=itemView.findViewById<TextView>(R.id.tvNombreReserva)
        var numeroSalasReservadas=itemView.findViewById<TextView>(R.id.tvSalasOcupadas)
        var flechaRedireccionarAdetalles=itemView.findViewById<ImageView>(R.id.imagenItemReserva)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaptadorSalasPorHora.MyHolder {
        val vista = LayoutInflater.from(context).inflate(R.layout.item_reserva_recycler_anidado, parent, false)
        return MyHolder(vista)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val compra = listaReservas[position]
        val nombreCliente = compra.name
        val salasOcupadas = compra.items.flatMap { it.salas ?: emptyList() }.distinct().size

        holder.tvNombre.text = nombreCliente
        holder.numeroSalasReservadas.text = "$salasOcupadas/8"
        holder.flechaRedireccionarAdetalles.visibility = View.VISIBLE

        holder.itemView.setOnClickListener { onItemClick(compra) }
    }

    override fun getItemCount(): Int = listaReservas.size

}