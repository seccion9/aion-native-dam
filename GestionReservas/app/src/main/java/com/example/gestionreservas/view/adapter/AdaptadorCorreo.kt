package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.model.CorreoItem

class AdaptadorCorreo(
    var context: Context,
    var listaCorreos: List<CorreoItem>
) : RecyclerView.Adapter<AdaptadorCorreo.MyHolder>() {

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var asunto: TextView = itemView.findViewById(R.id.tvAsuntoCorreo)
        var remitente: TextView = itemView.findViewById(R.id.tvRemitente)
        var cuerpoPreview: TextView = itemView.findViewById(R.id.tvCuerpoMensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista = LayoutInflater.from(context).inflate(R.layout.item_mensaje, parent, false)
        return MyHolder(vista)
    }

    override fun getItemCount(): Int {
       return listaCorreos.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val mensaje=listaCorreos[position]
        holder.asunto.text=mensaje.asunto
        holder.remitente.text=mensaje.remitente
        holder.cuerpoPreview.text=mensaje.cuerpoPreview
    }
}