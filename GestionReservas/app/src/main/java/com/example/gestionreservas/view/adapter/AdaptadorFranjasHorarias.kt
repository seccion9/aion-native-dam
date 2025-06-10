package com.example.gestionreservas.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.enums.EstadoSala
import com.example.gestionreservas.models.entity.FranjaHorariaReservas
import com.example.gestionreservas.models.entity.ItemReservaPorSala
import com.example.gestionreservas.models.entity.SalaConEstado

class AdaptadorFranjasHorarias(
    var context: Context,
    var listaFranjas:MutableList<FranjaHorariaReservas>,
    var onClickCrearReserva: () -> Unit,
    var onItemClick: (Compra) -> Unit = {}
) :RecyclerView.Adapter<AdaptadorFranjasHorarias.MyHolder>(){
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var horaInicio=itemView.findViewById<TextView>(R.id.tvHoraInicio)
        var horaFin=itemView.findViewById<TextView>(R.id.tvHoraFin)
        var recylerReservas=itemView.findViewById<RecyclerView>(R.id.recyclerItemsReservas)
        var reservaItem=itemView.findViewById<TextView>(R.id.tvReservaDetalle)
        var tvImagen=itemView.findViewById<ImageView>(R.id.imagenCandado)
        var tvSelectorFecha=itemView.findViewById<ImageView>(R.id.imagenSelector)
        var itemReservasLinear=itemView.findViewById<LinearLayout>(R.id.item_estado_sala_reserva)
        var salasReservadas=itemView.findViewById<TextView>(R.id.tvSalasReservadasPorDefecto)
        var salasReservadasXCandado=itemView.findViewById<TextView>(R.id.tvSalasReservadasCandado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val vista = LayoutInflater.from(context).inflate(R.layout.item_reserva_hora_desplegable, parent, false)
        return MyHolder(vista)
    }

    override fun getItemCount(): Int {
        return listaFranjas.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val franja = listaFranjas[position]
        holder.horaInicio.text = franja.horaInicio
        holder.horaFin.text = franja.horaFin

        val totalSalas = 8
        val salasReservadas = franja.salas.count { it.estado == EstadoSala.RESERVADA }
        val salasBloqueadas = franja.salas.count { it.estado == EstadoSala.BLOQUEADA }
        val salsLibres = franja.salas.count { it.estado == EstadoSala.LIBRE }

        // Adaptador anidado
       holder.recylerReservas.layoutManager = LinearLayoutManager(context)
        val itemsAgrupados = generarItemsParaFranja(franja.salas)
        val adaptadorReservas = AdaptadorSalasPorHora(
            context,
            itemsAgrupados.toMutableList(),
            onClickCrearReserva,
            onItemClick
        )
        holder.recylerReservas.adapter = adaptadorReservas

        holder.recylerReservas.visibility = View.GONE

        //Logica para mostrar items de franjas de una manera u otra dependiendo de las reservas
        when {
            salasBloqueadas == 8 -> {
                holder.reservaItem.text = "BLOQUEADA"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.hora_bloqueada_calendario)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.tvImagen.visibility = View.VISIBLE
                holder.tvImagen.setImageResource(R.drawable.candado)
                holder.salasReservadas.visibility = View.GONE
                holder.salasReservadasXCandado.visibility = View.GONE
            }

            salasReservadas == 0 && salasBloqueadas==0 -> {
                holder.reservaItem.text = "LIBRE"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.item_reserva_libre)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.salasReservadas.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.salasReservadas.text = "0/$totalSalas"
                holder.tvImagen.visibility = View.GONE
                holder.salasReservadas.visibility = View.VISIBLE
                holder.salasReservadasXCandado.visibility = View.GONE
            }

            salasReservadas == totalSalas -> {
                holder.reservaItem.text = "COMPLETA"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.hora_completa_item)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.salasReservadas.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.salasReservadas.text = "$salasReservadas/$totalSalas"
                holder.tvImagen.visibility = View.GONE
                holder.salasReservadas.visibility = View.VISIBLE
                holder.salasReservadasXCandado.visibility = View.GONE
            }
            salasReservadas>0 && salasBloqueadas>0 ->{
                holder.reservaItem.text = "LIBRE"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.item_reserva_con_reservas)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.verdeItemsReservas))
                holder.salasReservadasXCandado.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.tvImagen.setImageResource(R.drawable.candado_gris_oscuro)
                holder.tvImagen.visibility = View.VISIBLE
                holder.salasReservadas.visibility = View.GONE
                holder.salasReservadasXCandado.text="$salasReservadas/$totalSalas"
                holder.salasReservadasXCandado.visibility = View.VISIBLE
            }
            else -> {
                holder.reservaItem.text = "LIBRE"
                holder.itemReservasLinear.setBackgroundResource(R.drawable.item_reserva_con_reservas)
                holder.reservaItem.setTextColor(ContextCompat.getColor(context, R.color.verdeItemsReservas))
                holder.salasReservadas.text = "$salasReservadas/$totalSalas"
                holder.salasReservadas.setTextColor(ContextCompat.getColor(context, R.color.gray))
                holder.tvImagen.visibility = View.GONE
                holder.salasReservadas.visibility = View.VISIBLE
                holder.salasReservadasXCandado.visibility = View.GONE
            }
        }

        //Listeners para mostrar detalles al expandir
        holder.itemView.setOnClickListener {
            val visible = holder.recylerReservas.visibility == View.VISIBLE
            holder.recylerReservas.visibility = if (visible) {
                holder.tvSelectorFecha.setImageResource(R.drawable.selector_derecha)
                View.GONE
            } else {
                holder.tvSelectorFecha.setImageResource(R.drawable.selector_abajo)
                View.VISIBLE
            }
        }
    }

    fun actualizarLista(nuevaLista: List<FranjaHorariaReservas>) {
        listaFranjas.clear()
        listaFranjas.addAll(nuevaLista)
        notifyDataSetChanged()
    }
    private fun generarItemsParaFranja(salas: List<SalaConEstado>): List<ItemReservaPorSala> {
        val items = mutableListOf<ItemReservaPorSala>()

        // Agrupar por compra (reservadas)
        val agrupadasPorCompra = salas
            .filter { it.estado == EstadoSala.RESERVADA && it.reserva != null }
            .groupBy { it.reserva!!.id }

        for ((_, grupo) in agrupadasPorCompra) {
            val compra = grupo.first().reserva!!
            items.add(
                ItemReservaPorSala(
                    estado = EstadoSala.RESERVADA,
                    compra = compra,
                    cantidadSalas = grupo.size
                )
            )
        }

        // Bloqueadas
        val bloqueadas = salas.filter { it.estado == EstadoSala.BLOQUEADA }
        if (bloqueadas.isNotEmpty()) {
            items.add(
                ItemReservaPorSala(
                    estado = EstadoSala.BLOQUEADA,
                    cantidadSalas = bloqueadas.size
                )
            )
        }

        // Libres
        val libres = salas.filter { it.estado == EstadoSala.LIBRE }
        if (libres.isNotEmpty()) {
            items.add(
                ItemReservaPorSala(
                    estado = EstadoSala.LIBRE,
                    cantidadSalas = libres.size
                )
            )
        }

        return items
    }

}