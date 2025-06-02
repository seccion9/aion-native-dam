package com.example.gestionreservas.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.EstadoSala
import com.example.gestionreservas.models.entity.FranjaHorariaReservas

class AdaptadorFranjasHorarias(
    var context: Context,
    var listaFranjas:MutableList<FranjaHorariaReservas>,
    var onItemClick: (Compra) -> Unit = {}
) :RecyclerView.Adapter<AdaptadorFranjasHorarias.MyHolder>(){
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var horaInicio=itemView.findViewById<TextView>(R.id.tvHoraInicio)
        var horaFin=itemView.findViewById<TextView>(R.id.tvHoraFin)
        var recylerReservas=itemView.findViewById<RecyclerView>(R.id.recyclerItemsReservas)
        var reservaItem=itemView.findViewById<TextView>(R.id.tvReservaDetalle)
        var tvImagen=itemView.findViewById<ImageView>(R.id.imagenCandado)
        var tvSelectorFecha=itemView.findViewById<ImageView>(R.id.imagenSelector)
        var itemReservasLinear=itemView.findViewById<LinearLayout>(R.id.item_estado_sala_reserva)
        var salasReservadas=itemView.findViewById<TextView>(R.id.tvSalasReservadasPorDefecto)
        var salasReservadasXCandado=itemView.findViewById<TextView>(R.id.tvSalasReservadasCandado)
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

        val totalSalas = 8
        val reservas = franja.reservas
        val salasOcupadas = contarSalasOcupadas(reservas)
        val textoOcupadas = "$salasOcupadas/$totalSalas"

        // Adaptador anidado
        holder.recylerReservas.layoutManager = LinearLayoutManager(context)
        val adaptadorReservas = AdaptadorSalasPorHora(context, franja.reservas.toMutableList(), onItemClick)
        holder.recylerReservas.adapter = adaptadorReservas
        holder.recylerReservas.visibility = View.GONE

        when {
            franja.bloqueada -> {
                holder.reservaItem.text = "BLOQUEADA"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.hora_bloqueada_calendario)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.tvImagen.visibility = View.VISIBLE
                holder.tvImagen.setImageResource(R.drawable.candado)
                holder.salasReservadas.visibility = View.GONE
                holder.salasReservadasXCandado.visibility = View.GONE
            }

            reservas.isEmpty() -> {
                holder.reservaItem.text = "LIBRE"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.item_reserva_libre)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.salasReservadas.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.salasReservadas.text = "0/$totalSalas"
                holder.tvImagen.visibility = View.GONE
                holder.salasReservadas.visibility = View.VISIBLE
                holder.salasReservadasXCandado.visibility = View.GONE
            }

            salasOcupadas == totalSalas -> {
                holder.reservaItem.text = "COMPLETA"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.hora_completa_item)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.salasReservadas.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.salasReservadas.text = textoOcupadas
                holder.tvImagen.visibility = View.GONE
                holder.salasReservadas.visibility = View.VISIBLE
                holder.salasReservadasXCandado.visibility = View.GONE
            }

            else -> {
                holder.reservaItem.text = "LIBRE"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.item_reserva_con_reservas)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.verdeItemsReservas))
                holder.salasReservadas.text = textoOcupadas
                holder.salasReservadas.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.tvImagen.visibility = View.GONE
                holder.salasReservadas.visibility = View.VISIBLE
                holder.salasReservadasXCandado.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            val visible = holder.recylerReservas.visibility == View.VISIBLE
            holder.recylerReservas.visibility = if (visible) {
                holder.tvSelectorFecha.setImageResource(R.drawable.selector_derecha)
                View.GONE
            } else {
                holder.tvSelectorFecha.setImageResource(R.drawable.selector_abajo)
                View.VISIBLE
            }
        }
    }

    fun actualizarLista(nuevaLista: List<FranjaHorariaReservas>) {
        listaFranjas.clear()
        listaFranjas.addAll(nuevaLista)
        notifyDataSetChanged()
    }
    fun contarSalasOcupadas(reservas: List<Compra>): Int {
        return reservas.sumOf { compra ->
            compra.items.flatMap { it.salas ?: emptyList() }.distinct().size
        }
    }
}