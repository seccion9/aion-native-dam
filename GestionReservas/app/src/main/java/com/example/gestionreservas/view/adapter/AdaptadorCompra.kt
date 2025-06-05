package com.example.gestionreservas.view.adapter

import android.content.Context
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.ExperienciaCompleta
import com.example.gestionreservas.models.entity.SesionConCompra

class AdaptadorCompra(
    private val context: Context,
    private var listaSesiones: List<SesionConCompra>,
    private var listaExperiencias: List<ExperienciaCompleta>,
    private val onClick: (SesionConCompra) -> Unit
) : RecyclerView.Adapter<AdaptadorCompra.CompraViewHolder>() {
    private var posicionExpandida: Int? = null

    inner class CompraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHora = view.findViewById<TextView>(R.id.tvHoraCard)
        val tvCalendario = view.findViewById<TextView>(R.id.tvCalCard)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreCard)
        val tvParticipantes = view.findViewById<TextView>(R.id.tvParticipantesCard)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalPagadoDetallesSesion)
        val layoutDetalle = view.findViewById<View>(R.id.layoutDetalle)
        val botonOpciones = view.findViewById<TextView>(R.id.tvOpciones)
        val tvFaltaPagar = view.findViewById<TextView>(R.id.tvRestantePagarDetallesSesion)
        val tvExperiencia = view.findViewById<TextView>(R.id.tvNombreExperienciaDetallesSesion)
        val tvIdioma = view.findViewById<TextView>(R.id.tvIdiomaDetallesSesion)
        val tvMonitor = view.findViewById<TextView>(R.id.tvMonitorDetallesSesion)
        val tvPagadoCliente = view.findViewById<TextView>(R.id.tvPagadoDetallesSesion)
        val tvEstadoPago = view.findViewById<TextView>(R.id.tvTotalCard)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_home_desplegable, parent, false)
        return CompraViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val sesionConCompra = listaSesiones[position]
        val sesion = sesionConCompra.sesion
        val compra = sesionConCompra.compra
        val totalPagado = compra!!.payments.sumOf { it.amount }
        val totalFinal = compra.priceFinal
        val restante = totalFinal - totalPagado

        val fields = compra.items.last().fields
        holder.tvHora.text = sesion.hora
        holder.tvCalendario.text = sesion.calendario
        holder.tvNombre.text = sesion.nombre
        holder.tvParticipantes.text = sesion.participantes.toString()

        holder.tvTotal.text = Html.fromHtml("Total <b>%.2f€</b>".format(compra.priceFinal))
        holder.tvPagadoCliente.text = Html.fromHtml("Pagado <b>%.2f€</b>".format(totalPagado))
        holder.tvFaltaPagar.text = Html.fromHtml("Restante <b>%.2f€</b>".format(restante))

        val estadoPago = when {
            totalPagado == 0.0 -> "No pagada"
            restante > 0 -> "Parcial"
            else -> "Pagada"
        }

        val colorPago = when (estadoPago) {
            "Pagada" -> R.color.pago_pagada
            "Parcial" -> R.color.pago_parcial
            "No pagada" -> R.color.pago_no_pagada
            else -> R.color.white
        }

        holder.tvEstadoPago.text = estadoPago
        holder.tvEstadoPago.setBackgroundColor(ContextCompat.getColor(context, colorPago))

        val idExp = compra.items.last().idExperience
        Log.d("DEBUG_EXP", "Buscando experiencia con ID: $idExp")

        val experienciaCoincidente = listaExperiencias.find { it.id == idExp.toIntOrNull() }
        Log.d("DEBUG_EXP", "LISTA experienciaS: ${listaExperiencias}")
        if (experienciaCoincidente != null) {
            holder.tvExperiencia.text = experienciaCoincidente.name
            Log.d("DEBUG_EXP", "Nombre experiencia: ${experienciaCoincidente.name}")
        } else {
            holder.tvExperiencia.text = "Desconocida"
            Log.w("DEBUG_EXP", "No se encontró experiencia para ID: $idExp")
        }
        holder.tvIdioma.text = fields.firstOrNull { it.title == "Idioma" }?.value ?: "No indicado"
        holder.tvMonitor.text = fields.firstOrNull { it.title == "Monitor" }?.name ?: "No indicado"

        // Expandir/cerrar lógica
        val expandido = posicionExpandida == position
        holder.layoutDetalle.visibility = if (expandido) View.VISIBLE else View.GONE

        //listeners para expandir item o llevar a compra
        holder.itemView.setOnClickListener {
            val visible = holder.layoutDetalle.visibility == View.VISIBLE
            holder.layoutDetalle.visibility = if (visible) View.GONE else View.VISIBLE
        }
        holder.botonOpciones.setOnClickListener {
            onClick(sesionConCompra)
        }

    }

    override fun getItemCount(): Int = listaSesiones.size

    fun actualizarLista(nuevaLista: List<SesionConCompra>) {
        listaSesiones = nuevaLista
        notifyDataSetChanged()
    }

    fun actualizarExperiencias(nuevaLista: List<ExperienciaCompleta>) {

        listaExperiencias = nuevaLista
        notifyDataSetChanged()
    }
}
