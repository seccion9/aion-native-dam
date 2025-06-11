package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.enums.AccionComentario

class AdaptadorComentarios(
    private val context: Context,
    private var listaComentarios:List<Comentario>,
    private var onAccionClick: ((Comentario, AccionComentario) -> Unit)? =null
):RecyclerView.Adapter<AdaptadorComentarios.MyHolder>(){
    /**
     * Instancias de clase MyHolder
     */
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nombreUsuario=itemView.findViewById<TextView>(R.id.nombreUsuario)
        var fecha=itemView.findViewById<TextView>(R.id.tvHoraItemComentarios)
        var descripcion=itemView.findViewById<TextView>(R.id.tvDescripcionItemComentarios)
        var delete=itemView.findViewById<ImageView>(R.id.imageDelete)
        var editar=itemView.findViewById<ImageView>(R.id.imageEdit)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaptadorComentarios.MyHolder {
        val vista =
            LayoutInflater.from(context).inflate(R.layout.item_comentarios, parent, false)
        return MyHolder(vista)
    }

    override fun onBindViewHolder(holder: AdaptadorComentarios.MyHolder, position: Int) {
       val item=listaComentarios[position]

        holder.nombreUsuario.text=item.nombreUsuario
        holder.fecha.text=item.fecha
        holder.descripcion.text=item.descripcion
        //Listeners para realizar una opción u otra según se pinche en un icono u otro.
        holder.editar.setOnClickListener {
            onAccionClick?.invoke(item, AccionComentario.EDITAR)
        }

        holder.delete.setOnClickListener {
            onAccionClick?.invoke(item, AccionComentario.ELIMINAR)
        }

    }

    override fun getItemCount(): Int {
        return listaComentarios.size
    }
    fun actualizarLista(nuevaLista: List<Comentario>) {
        listaComentarios = nuevaLista
        notifyDataSetChanged()
    }

}