package com.example.gestionreservas.view.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.FotoSesion
import com.example.gestionreservas.models.entity.Jugador

class AdaptadorImagenes(
    private var context: Context,
    private var listaImagenes: List<String>,
) : RecyclerView.Adapter<AdaptadorImagenes.MyHolder>() {

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imagenItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista = LayoutInflater.from(context).inflate(R.layout.item_imagen, parent, false)
        return MyHolder(vista)
    }

    override fun getItemCount(): Int = listaImagenes.size

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val base64 = listaImagenes[position]
        try {
            val imageBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.imagen.setImageBitmap(bitmap)
        } catch (e: Exception) {
            holder.imagen.setImageResource(R.drawable.imagen_camara)
        }
    }
}
