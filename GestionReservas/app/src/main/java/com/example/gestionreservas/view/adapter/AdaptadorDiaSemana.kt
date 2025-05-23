package com.example.gestionreservas.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.DiaSemana

class AdaptadorDiaSemana(var context: Context,var listaDiasSemana:ArrayList<DiaSemana>,val listener: OnDiaSemanaClickListener) :RecyclerView.Adapter<AdaptadorDiaSemana.MyHolder>(){

    class MyHolder(itemView: View):ViewHolder(itemView){
        //saco cada uno de los elementos que hay en el xml(patron de la fila)
        var dia=itemView.findViewById<TextView>(R.id.diaSemana)
        var numReservas=itemView.findViewById<TextView>(R.id.numeroReservas)
        var sesiones=itemView.findViewById<TextView>(R.id.numeroSesiones)
        var infoDia=itemView.findViewById<TextView>(R.id.infoDia)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        //Inflamos la vista de nuestro item dia semana
        val vista=LayoutInflater.from(context).inflate(R.layout.item_dia_semana,parent,false)
        val holder:MyHolder=MyHolder(vista)
        return holder
    }

    override fun getItemCount(): Int {
        return listaDiasSemana.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        //Aqui va nuestra logica del card:cambiar dia semana por letra,añadir textos,etc
        val dia=listaDiasSemana[position]
        holder.numReservas.text="Reservas: ${dia.reservas}"
        holder.sesiones.text="Sesiones: ${dia.sesiones}"
        holder.dia.text = dia.fecha.dayOfMonth.toString()
        val capitalizado = dia.nombreDia.replaceFirstChar { it.uppercase() }
        holder.infoDia.text=capitalizado
        holder.itemView.setOnClickListener{
            listener.onDiaClick(dia)
        }

    }
    fun actualizarLista(nuevaLista: List<DiaSemana>) {
        this.listaDiasSemana = nuevaLista as ArrayList<DiaSemana>
        notifyDataSetChanged()
    }
}
interface OnDiaSemanaClickListener {
    fun onDiaClick(dia: DiaSemana)
}