package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.PagoReserva
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.view.adapter.AdaptadorPagos.MyHolder

class AdaptadorCajaChica(
    private val context: Context,
    private var listaPagos: List<PagoCaja>,
    private val onPagoSeleccionado: ((PagoCaja) -> Unit)? = null

) :RecyclerView.Adapter<AdaptadorCajaChica.MyHolder>(){
    private var mostrarCheckboxes = false
    private var seleccionado: PagoCaja? = null
    //Obtenemos los campos a rellenar de nuestro item
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var concepto=itemView.findViewById<TextView>(R.id.tvConceptoPago)
        var cantidad=itemView.findViewById<TextView>(R.id.tvCantidadPago)
        var fecha=itemView.findViewById<TextView>(R.id.tvFechaPago)
        var checkBox=itemView.findViewById<CheckBox>(R.id.checkboxSeleccion)

    }
    //Cargamos la vista del item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista =
            LayoutInflater.from(context).inflate(R.layout.pagos_caja_chica_item, parent, false)
        return MyHolder(vista)
    }

    override fun getItemCount(): Int {
        return listaPagos.size
    }

    /**
     * Cargamos en los items los datos del objeto en esa posición y formatemos la cantidad.Dependiendo de si el pago
     * es negativo o no aparece de un color u otro
     */
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val item = listaPagos[position]
        holder.checkBox.visibility = if (mostrarCheckboxes) View.VISIBLE else View.GONE


        holder.concepto.text = item.concepto
        holder.fecha.text = item.fecha

        val cantidadFloat = item.cantidad.toFloatOrNull() ?: 0f
        holder.cantidad.text = "%.2f€".format(cantidadFloat)

        when {
            cantidadFloat < 0 -> holder.cantidad.setTextColor(ContextCompat.getColor(context, R.color.pago_no_pagada))
            cantidadFloat > 0 -> holder.cantidad.setTextColor(ContextCompat.getColor(context, R.color.verdeItemsReservas))
            else -> holder.cantidad.setTextColor(ContextCompat.getColor(context, R.color.texto_secundario))
        }
        //Mira si mostrar checkbox esta visible o no para mostrar los checkbox
        holder.checkBox.visibility = if (mostrarCheckboxes) View.VISIBLE else View.GONE
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item == seleccionado
        //Maneja el listener para solo dejar seleccionar un item
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                seleccionado = item
                notifyDataSetChanged()
            } else if (seleccionado == item) {
                seleccionado = null
            }
        }

        holder.itemView.setOnLongClickListener {
            onPagoSeleccionado?.let { it1 -> it1(item) }
            true
        }
    }
    fun actualizarLista(nuevaLista: List<PagoCaja>) {
        listaPagos = nuevaLista
        notifyDataSetChanged()
    }

    fun mostrarCheckboxes(mostrar: Boolean) {
        mostrarCheckboxes = mostrar
        notifyDataSetChanged()
    }
    fun obtenerSeleccionado(): PagoCaja? = seleccionado

}