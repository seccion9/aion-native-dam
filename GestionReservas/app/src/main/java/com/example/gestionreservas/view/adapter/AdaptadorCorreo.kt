package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.model.CorreoItem
import com.example.gestionreservas.models.entity.SesionConCompra

class AdaptadorCorreo(
    var context: Context,
    var listaCorreos: MutableList<CorreoItem>,
    var onItemClick: (CorreoItem) -> Unit = {}
) : RecyclerView.Adapter<AdaptadorCorreo.MyHolder>() {
    private val listaOriginal = mutableListOf<CorreoItem>()

    init {
        listaOriginal.addAll(listaCorreos)
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var asunto: TextView = itemView.findViewById(R.id.tvAsuntoCorreo)
        var remitente: TextView = itemView.findViewById(R.id.tvRemitente)
        var cuerpoPreview: TextView = itemView.findViewById(R.id.tvCuerpoMensaje)
        val contenedor = itemView.findViewById<CardView>(R.id.contenedorMensaje)
        val fecha: TextView = itemView.findViewById(R.id.tvFecha)
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
        holder.fecha.text=mensaje.fecha
        holder.contenedor.setOnClickListener{
            onItemClick(mensaje)
        }
    }
    fun actualizarListaCompleta(nuevaLista: List<CorreoItem>) {
        listaCorreos.clear()
        listaCorreos.addAll(nuevaLista)

        listaOriginal.clear()
        listaOriginal.addAll(nuevaLista)
    }

    fun filtrar(texto:String){
        val textoFiltrado=texto.lowercase().trim()
        listaCorreos.clear()

        if(textoFiltrado.isEmpty()){
            listaCorreos.addAll(listaOriginal)
        }else{
            listaCorreos.addAll(
                listaOriginal.filter {
                    it.asunto.lowercase().contains(textoFiltrado) ||
                            it.remitente.lowercase().contains(textoFiltrado) ||
                            it.cuerpoPreview.lowercase().contains(textoFiltrado)
                }
            )
        }
        notifyDataSetChanged()
    }
}