package com.example.gestionreservas.view.adapter

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.ExperienciaCompleta
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.view.fragment.DetalleSesionFragment


class AdaptadorListado(
    private val context: Context,
    private var listaSesiones: List<SesionConCompra>,
    private var listaExperiencias: List<ExperienciaCompleta>,
    private val onClick: (SesionConCompra) -> Unit
) : RecyclerView.Adapter<AdaptadorListado.CompraViewHolder>() {
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
        //Limpiamos hora para mostrarla en el tv
        holder.tvHora.text =
            "${compra.items.last().start.split(" ")[0]}\n" +
                    "${compra.items.last().start.split(" ")[1].substring(0,5)}"

        holder.tvCalendario.text = sesion.calendario
        holder.tvNombre.text = sesion.nombre
        holder.tvParticipantes.text = sesion.participantes.toString()

        holder.tvTotal.text = Html.fromHtml("Total <b>%.2f€</b>".format(compra.priceFinal))
        holder.tvPagadoCliente.text = Html.fromHtml("Pagado <b>%.2f€</b>".format(totalPagado))
        holder.tvFaltaPagar.text = Html.fromHtml("Restante <b>%.2f€</b>".format(restante))
        //Lógica para mostrar estado pago y sus colores
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

        //Busca las experiencias coincidentes con los id de compras para mostrar el nombre de la exp
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
        //Comprobamos estado del pago para mostrar diferentes colores en el recycler
        val estado = compra.status.lowercase()

        val colorEstado = when (estado) {
            "confirmada" -> R.color.estado_confirmada
            "pendiente" -> R.color.estado_pendiente
            "no_finalizada" -> R.color.estado_no_finalizada
            "cancelada" -> R.color.estado_cancelada
            else -> R.color.black
        }

        val colorRelleno = when (estado) {
            "confirmada" -> R.color.relleno_confirmada
            "pendiente" -> R.color.relleno_pendiente
            "no_finalizada" -> R.color.relleno_no_finalizada
            "cancelada" -> R.color.relleno_cancelada
            else -> R.color.white
        }

        with(holder) {
            // Columna de estado: color fuerte
            tvHora.setBackgroundResource(colorEstado)

            // Resto de columnas: color suave
            tvCalendario.setBackgroundResource(colorRelleno)
            tvNombre.setBackgroundResource(colorRelleno)
            tvParticipantes.setBackgroundResource(colorRelleno)
        }


        holder.tvHora.setBackgroundColor(ContextCompat.getColor(context, colorEstado))


        val fechaHora = compra.fechaCompra
        val fecha = fechaHora.substring(0, 10)
        val hora = fechaHora.substring(11, 16)
        holder.tvHora.text = "$fecha\n$hora"



        holder.tvIdioma.text = fields.firstOrNull { it.title == "Idioma" }?.value ?: "No indicado"
        holder.tvMonitor.text = fields.firstOrNull { it.title == "Monitor" }?.name ?: "No indicado"

        // Expandir/cerrar lógica
        val expandido = posicionExpandida == position
        holder.layoutDetalle.visibility = if (expandido) View.VISIBLE else View.GONE

        //Listeners de expandir y de ir a detalles
        holder.itemView.setOnClickListener {
            val visible = holder.layoutDetalle.visibility == View.VISIBLE
            holder.layoutDetalle.visibility = if (visible) View.GONE else View.VISIBLE
        }
        holder.botonOpciones.setBackgroundResource(R.drawable.boton_suave_listado)
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
