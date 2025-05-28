package com.example.gestionreservas.utils

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.SesionConCompra

object TablaBuilder {

    fun crearCelda(context: Context, texto: String): TextView {
        return TextView(context).apply {
            text = texto
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            minimumHeight = 96
            setBackgroundResource(R.drawable.tabla_home)
        }
    }
    fun construirTablaComentarios(tabla: TableLayout, comentarios: List<Comentario>, context: Context) {
        tabla.removeAllViews()

        // Cabecera
        val filaCabecera = TableRow(context)
        val titulos = listOf("Comentario", "Fecha", "Nombre")
        titulos.forEach { titulo ->
            filaCabecera.addView(crearCelda(context, titulo))
        }
        tabla.addView(filaCabecera)

        // Contenido
        comentarios.forEach { comentario ->
            val fila = TableRow(context)
            fila.addView(crearCelda(context, comentario.descripcion))
            val soloFecha = comentario.fecha.substringBefore(" ")
            fila.addView(crearCelda(context, soloFecha))
            fila.addView(crearCelda(context, comentario.nombreUsuario))

            aplicarFondoClickable(context, fila)
            tabla.addView(fila)
        }
    }

    fun construirTablaPagos(tabla: TableLayout, pagos: List<PagoCaja>, context: Context) {
        tabla.removeAllViews()
        var totalParcial = 0
        val filaCabecera = TableRow(context)

        val titulos = listOf("Fecha", "Concepto", "Cantidad", "Tipo", "Total Parcial")
        titulos.forEach { titulo ->
            filaCabecera.addView(crearCelda(context, titulo))
        }
        tabla.addView(filaCabecera)

        pagos.forEach { pago ->
            val fila = TableRow(context)
            val cantidadNum = pago.cantidad.replace("€", "").replace(",", ".").trim().toDouble()
            totalParcial += cantidadNum.toInt()
            val totalString = "$totalParcial €"

            fila.addView(crearCelda(context, pago.fecha))
            fila.addView(crearCelda(context, pago.concepto))
            fila.addView(crearCelda(context, pago.cantidad))
            fila.addView(crearCelda(context, pago.tipo))
            fila.addView(crearCelda(context, totalString))

            aplicarFondoClickable(context, fila)
            tabla.addView(fila)
        }
    }

    fun construirTablaSesiones(
        tabla: TableLayout,
        sesiones: List<SesionConCompra>,
        context: Context,
        onClick: (SesionConCompra) -> Unit
    ) {
        tabla.removeAllViews()

        val filaCabecera = TableRow(context)
        val titulos = listOf("Hora", "Calendario", "Nombre", "Participantes", "Total Pagado", "Estado", "Idiomas")
        titulos.forEach { titulo ->
            filaCabecera.addView(crearCelda(context, titulo))
        }
        tabla.addView(filaCabecera)

        sesiones.forEach { sesionConCompra ->
            val sesion = sesionConCompra.sesion
            val fila = TableRow(context)

            fila.addView(crearCelda(context, sesion.hora))
            fila.addView(crearCelda(context, sesion.calendario))
            fila.addView(crearCelda(context, sesion.nombre))
            fila.addView(crearCelda(context, sesion.participantes.toString()))
            fila.addView(crearCelda(context, "%.2f€".format(sesion.totalPagado)))
            fila.addView(crearCelda(context, sesion.estado))
            fila.addView(crearCelda(context, sesion.idiomas))

            aplicarFondoClickable(context, fila)
            fila.setOnClickListener { onClick(sesionConCompra) }

            tabla.addView(fila)
        }
    }

    private fun aplicarFondoClickable(context: Context, fila: TableRow) {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        fila.foreground = ContextCompat.getDrawable(context, outValue.resourceId)
        fila.isClickable = true
        fila.isFocusable = true
    }
}
