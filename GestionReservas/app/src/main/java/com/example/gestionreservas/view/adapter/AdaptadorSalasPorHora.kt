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
import com.example.gestionreservas.models.entity.EstadoSala
import com.example.gestionreservas.models.entity.SalaConEstado

class AdaptadorSalasPorHora(
    var context: Context,
    var listaSalasEstados:MutableList<SalaConEstado>,
    var onItemClick: () -> Unit = {}
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
        val sala = listaSalasEstados[position]
        Log.d("AdaptadorSalas", "Total salas: ${listaSalasEstados.size}")

        when (sala.estado) {
            EstadoSala.LIBRE -> {
                holder.tvNombre.text = sala.idSala

            }
            EstadoSala.BLOQUEADA -> {
                holder.tvNombre.text = sala.idSala

            }
            EstadoSala.RESERVADA -> {
                val compra = sala.reservas?.firstOrNull()
                holder.tvNombre.text = compra?.name ?: "Sin nombre"
                holder.flechaRedireccionarAdetalles.visibility = View.VISIBLE

                holder.itemView.setOnClickListener {
                    if (compra != null) {
                        onItemClick()
                    } else {
                        Toast.makeText(context, "Reserva no disponible", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return listaSalasEstados.size
    }


}