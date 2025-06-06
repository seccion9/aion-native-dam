package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.PagoCajaChica
import com.example.gestionreservas.models.entity.PagoReserva

class AdaptadorPagos(
    private var context: Context,
    private var listaPagos: List<PagoReserva>,
    private val mapaPagosACompras: Map<String, Compra>
) : RecyclerView.Adapter<AdaptadorPagos.MyHolder>() {
    private val itemsDesplegados = mutableSetOf<Int>()

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var concepto = itemView.findViewById<TextView>(R.id.tvConcepto)
        var fecha = itemView.findViewById<TextView>(R.id.tvFecha)
        var pago = itemView.findViewById<TextView>(R.id.tvCantidadPago)
        var metodoPago = itemView.findViewById<TextView>(R.id.tvTipoPago)
        var linearContenedor =
            itemView.findViewById<LinearLayout>(R.id.linearContenedorItemsDesplegables)

        var tvNombre = itemView.findViewById<TextView>(R.id.tvDesplegableNombreCliente)
        var tvTelefono = itemView.findViewById<TextView>(R.id.tvDesplegableTelefono)
        var tvExperiencia = itemView.findViewById<TextView>(R.id.tvDesplegableExperiencia)
        var tvFechaSesion = itemView.findViewById<TextView>(R.id.tvDesplegableFechaSesion)
        var tvEstado = itemView.findViewById<TextView>(R.id.tvDesplegableEstadoSesion)
        var tvIidoma = itemView.findViewById<TextView>(R.id.tvDesplegableIdiomaSesion)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista =
            LayoutInflater.from(context).inflate(R.layout.item_recycler_caja_chica, parent, false)
        return MyHolder(vista)
    }

    override fun getItemCount(): Int {
        return listaPagos.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val item = listaPagos[position]
        holder.concepto.text = item.concepto
        holder.fecha.text = item.fecha
        holder.pago.text = "${item.cantidad}€"
        holder.metodoPago.text = item.tipo

        val compra = mapaPagosACompras[item.id]

        // Mostrar info si está desplegado
        val desplegado = itemsDesplegados.contains(position)
        holder.linearContenedor.visibility = if (desplegado) View.VISIBLE else View.GONE

        //Si la compra no es nula le asigna los datos a los textview
        holder.tvNombre.text = compra?.name ?: "No Asignada"
        holder.tvTelefono.text = compra?.phone ?: "No Asignada"
        holder.tvExperiencia.text =
            compra?.resumenItems?.lastOrNull()?.id_experience ?: "No Asignada"
        holder.tvFechaSesion.text = compra?.fechaCompra ?: "No Asignada"
        holder.tvEstado.text = compra?.status ?: "No Asignada"
        holder.tvIidoma.text = compra?.language ?: "No Asignada"


        // Listener para mostrar detalles
        holder.itemView.setOnClickListener {
            if (desplegado) {
                itemsDesplegados.remove(position)
            } else {
                itemsDesplegados.add(position)
            }
            notifyItemChanged(position)
        }
    }

    fun actualizarLista(nuevaLista: List<PagoReserva>) {
        listaPagos = nuevaLista
        itemsDesplegados.clear()
        notifyDataSetChanged()
    }

}