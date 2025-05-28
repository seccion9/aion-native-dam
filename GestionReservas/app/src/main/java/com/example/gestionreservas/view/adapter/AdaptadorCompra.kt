package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.SesionConCompra

class AdaptadorCompra(
    private val context: Context,
    private var listaSesiones: List<SesionConCompra>,
    //private val onClick: (SesionConCompra) -> Unit
) : RecyclerView.Adapter<AdaptadorCompra.CompraViewHolder>() {
    private var posicionExpandida: Int? = null

    inner class CompraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHora = view.findViewById<TextView>(R.id.tvHoraCard)
        val tvCalendario = view.findViewById<TextView>(R.id.tvCalCard)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreCard)
        val tvParticipantes = view.findViewById<TextView>(R.id.tvParticipantesCard)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalCard)
        val layoutDetalle = view.findViewById<View>(R.id.layoutDetalle)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_home_desplegable, parent, false)
        return CompraViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val sesionConCompra = listaSesiones[position]
        val sesion = sesionConCompra.sesion
        val compra = sesionConCompra.compra

        holder.tvHora.text = sesion.hora
        holder.tvCalendario.text = sesion.calendario
        holder.tvNombre.text = sesion.nombre
        holder.tvParticipantes.text = sesion.participantes.toString()
        holder.tvTotal.text = "%.2f €".format(compra?.priceFinal)

        // Expandir/cerrar lógica
        val expandido = posicionExpandida == position
        holder.layoutDetalle.visibility = if (expandido) View.VISIBLE else View.GONE


        holder.itemView.setOnClickListener {
            val anterior = posicionExpandida
            posicionExpandida = if (expandido) null else position
            anterior?.let { notifyItemChanged(it) }
            notifyItemChanged(position)
            //onClick(sesionConCompra)
        }
    }

    override fun getItemCount(): Int = listaSesiones.size

    fun actualizarLista(nuevaLista: List<SesionConCompra>) {
        listaSesiones = nuevaLista
        notifyDataSetChanged()
    }
}
