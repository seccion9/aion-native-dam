package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.Jugador
import com.example.gestionreservas.utils.ImagenUtils

class AdaptadorJugadores(
    private var context: Context,
    private var listaJugadores: List<Jugador>,
    private val onClickAgregarImagen: (String) -> Unit
) : RecyclerView.Adapter<AdaptadorJugadores.MyHolder>() {

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imagenJugador)
        val nombre: TextView = itemView.findViewById(R.id.nombreJugador)

        init {
            imagen.setOnClickListener {
                val jugador = listaJugadores[adapterPosition]
                onClickAgregarImagen(jugador.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista = LayoutInflater.from(context).inflate(R.layout.item_jugador, parent, false)
        return MyHolder(vista)
    }

    override fun getItemCount(): Int = listaJugadores.size

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val jugador = listaJugadores[position]
        holder.nombre.text = jugador.nombre

        val bitmap = ImagenUtils.convertirBase64ABitmap(jugador.imagen)
        if (bitmap != null) {
            holder.imagen.setImageBitmap(bitmap)
        } else {
            holder.imagen.setImageResource(R.drawable.usuarion)
        }

    }

    fun actualizarLista(nuevaLista: List<Jugador>) {
        listaJugadores = nuevaLista
        notifyDataSetChanged()
    }
}
