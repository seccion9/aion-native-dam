package com.example.gestionreservas.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.FranjaHorariaReservas

class AdaptadorFranjasHorarias(
    var context: Context,
    var listaFranjas:MutableList<FranjaHorariaReservas>,
    var onItemClick: () -> Unit = {}
) :RecyclerView.Adapter<AdaptadorFranjasHorarias.MyHolder>(){
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var horaInicio=itemView.findViewById<TextView>(R.id.tvHoraInicio)
        var horaFin=itemView.findViewById<TextView>(R.id.tvHoraFin)
        var recylerReservas=itemView.findViewById<RecyclerView>(R.id.recyclerItemsReservas)
        var reservaItem=itemView.findViewById<TextView>(R.id.tvReservaDetalle)
        var tvImagen=itemView.findViewById<ImageView>(R.id.imagenCandado)
        var tvSelectorFecha=itemView.findViewById<ImageView>(R.id.imagenSelector)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista = LayoutInflater.from(context).inflate(R.layout.item_reserva_hora_desplegable, parent, false)
        return MyHolder(vista)
    }

    override fun getItemCount(): Int {
        return listaFranjas.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val franja = listaFranjas[position]

        holder.horaInicio.text = franja.horaInicio
        holder.horaFin.text = franja.horaFin

        holder.recylerReservas.layoutManager = LinearLayoutManager(context)
        val adaptadorReservas = AdaptadorSalasPorHora(context, franja.salas.toMutableList())
        holder.recylerReservas.adapter = adaptadorReservas
        holder.recylerReservas.visibility = View.GONE // cerrado por defecto

        // Toggle mostrar/ocultar
        holder.itemView.setOnClickListener {
            val visible = holder.recylerReservas.visibility == View.VISIBLE
            Log.d("ExpandDebug", "Recycler visibility before: $visible")
            holder.recylerReservas.visibility = if (visible) View.GONE else View.VISIBLE
        }
    }
}