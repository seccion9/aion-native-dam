package com.example.gestionreservas.view.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.HoraReserva
import com.example.gestionreservas.models.entity.SesionConCompra

class AdaptadorHoraReserva(
    var context: Context,
    var listaReservas: ArrayList<HoraReserva>,
    var onItemClick: (HoraReserva, String) -> Unit = { _, _ -> }
) :
    RecyclerView.Adapter<AdaptadorHoraReserva.MyHolder>() {
    class MyHolder(itemView: View) : ViewHolder(itemView) {
        //saco cada uno de los elementos que hay en el xml(patron de la fila)
        var horaInicio = itemView.findViewById<TextView>(R.id.tvHoraInicio)
        var horaFinal = itemView.findViewById<TextView>(R.id.tvHoraFin)
        var sala1 = itemView.findViewById<TextView>(R.id.tvSala1)
        var sala2 = itemView.findViewById<TextView>(R.id.tvSala2)
        val sala1Layout = itemView.findViewById<FrameLayout>(R.id.sala1)
        val sala2Layout = itemView.findViewById<FrameLayout>(R.id.sala2)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdaptadorHoraReserva.MyHolder {
        //crea el patron de las filas
        val vista = LayoutInflater.from(context).inflate(R.layout.item_reserva_hora, parent, false)

        val holder: MyHolder = MyHolder(vista)
        return holder
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val reserva = listaReservas[position]

        holder.horaInicio.text = reserva.horaInicio
        holder.horaFinal.text = reserva.horaFin

        if (reserva.sala1Bloqueada == true) {
            holder.sala1.text = "Bloqueado"
            holder.sala1.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#FFDF81"))
            holder.sala1Layout.setOnClickListener(null)
        } else if (reserva.sala1Libre == true) {
            holder.sala1.text = "Libre"
            holder.sala1.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#8032CD32"))
            holder.sala1Layout.setOnClickListener {
                onItemClick(reserva, "cal1")
            }
        } else {
            holder.sala1.text = "Reservado"
            holder.sala1.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#80FF0000"))
            holder.sala1Layout.setOnClickListener {
                onItemClick(reserva, "cal1")
            }
        }


        if (reserva.sala2Bloqueada == true) {
            holder.sala2.text = "Bloqueado"
            holder.sala2.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#FFDF81"))
            holder.sala2Layout.setOnClickListener(null)
        } else if (reserva.sala2Libre == true) {
            holder.sala2.text = "Libre"
            holder.sala2.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#8032CD32"))
            holder.sala2Layout.setOnClickListener {
                onItemClick(reserva, "cal2")
            }
        } else {
            holder.sala2.text = "Reservado"
            holder.sala2.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#80FF0000"))
            holder.sala2Layout.setOnClickListener {
                onItemClick(reserva, "cal2")
            }
        }
    }


    override fun getItemCount(): Int {
        return listaReservas.size
    }

    fun actualizarLista(nuevaLista: List<HoraReserva>) {
        listaReservas = nuevaLista.toMutableList() as ArrayList<HoraReserva>
        notifyDataSetChanged()
    }

}