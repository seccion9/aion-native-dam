package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.enums.EstadoSala
import com.example.gestionreservas.models.entity.ItemReservaPorSala

class AdaptadorSalasPorHora(
    private val context: Context,
    private val listaReservas: MutableList<ItemReservaPorSala>,
    private val onClickCrearReserva: () -> Unit,
    private val onItemClick: (Compra) -> Unit = {}
) :RecyclerView.Adapter<AdaptadorSalasPorHora.MyHolder>(){

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvNombre=itemView.findViewById<TextView>(R.id.tvNombreReserva)
        var numeroSalasReservadas=itemView.findViewById<TextView>(R.id.tvSalasOcupadas)
        var flechaRedireccionarAdetalles=itemView.findViewById<ImageView>(R.id.imagenItemReserva)
        var contenedorItem=itemView.findViewById<LinearLayout>(R.id.contenedorItemAnidado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaptadorSalasPorHora.MyHolder {
        val vista = LayoutInflater.from(context).inflate(R.layout.item_reserva_recycler_anidado, parent, false)
        return MyHolder(vista)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val item = listaReservas[position]
        //Mostramos diferentes estados del item dependiendo de datos del item de las reserva
        when (item.estado) {
            EstadoSala.RESERVADA -> {
                holder.tvNombre.text = item.compra?.name?.uppercase() ?: "RESERVADO"
                holder.numeroSalasReservadas.text = "${item.cantidadSalas}/8"
                holder.itemView.setOnClickListener {
                    item.compra?.let { onItemClick(it) }
                }
            }

            EstadoSala.BLOQUEADA -> {
                holder.contenedorItem.setBackgroundResource(R.drawable.hora_bloqueada_calendario)
                holder.flechaRedireccionarAdetalles.setImageResource(R.drawable.candado)
                holder.tvNombre.text = "BLOQUEADA"
                holder.numeroSalasReservadas.text = "${item.cantidadSalas}/8"
                holder.itemView.setOnClickListener(null)
            }

            EstadoSala.LIBRE -> {
                holder.tvNombre.text = "LIBRE"
                holder.tvNombre.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.contenedorItem.setBackgroundResource(R.drawable.item_reserva_libre)
                holder.flechaRedireccionarAdetalles.setImageResource(R.drawable.circulo_libre)
                holder.numeroSalasReservadas.text = "${item.cantidadSalas}/8"
                holder.numeroSalasReservadas.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.itemView.setOnClickListener {
                    onClickCrearReserva()
                }
            }
        }
    }

    override fun getItemCount(): Int = listaReservas.size

}