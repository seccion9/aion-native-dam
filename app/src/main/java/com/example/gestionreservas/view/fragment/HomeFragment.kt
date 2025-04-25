package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentHomeBinding
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.Sesion
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment: Fragment(),OnClickListener {
    @SuppressLint("NewApi")
    private  var fechaActual: LocalDate = LocalDate.now()
    private lateinit var binding:FragmentHomeBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // // Inflamos el layout del fragmento para que cargue la vista correctamente
        instancias()
        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias(){
        actualizarFecha()
        binding.tvCalendario.setOnClickListener(this)
        binding.tvReservas.setOnClickListener(this)
        binding.selectFecha.setOnClickListener(this)
        binding.tvMailing.setOnClickListener(this)
        binding.tvHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        //Sesiones creadas para mostrarlas hasta que se recogen datos de la API
        val listaSesiones = listOf(
            Sesion("10:00", "Calendario A", "Juan Pérez", 4, 80.0, "Confirmada", "Español"),
            Sesion("12:00", "Calendario B", "Laura López", 2, 40.0, "Pendiente", "Inglés"),
            Sesion("13:10", "Calendario A", "Raul Blanco", 5, 100.0, "Pagado", "Español"),
            Sesion("16:00", "Calendario B", "Maria Gutierrez", 3, 50.0, "Pendiente", "Inglés"),
            Sesion("16:20", "Calendario A", "Juan Pérez", 4, 80.0, "Confirmada", "Español"),
            Sesion("17:30", "Calendario B", "Laura López", 2, 40.0, "Pendiente", "Inglés"),


        )
        val listaPagosCaja = listOf(
            PagoCaja ("25/04/2025", "Calendario A", "80.0", "tarjeta", "40.0"),
            PagoCaja("25/04/2025", "Calendario B", "40.0", "tarjeta", "20.0"),
            PagoCaja("25/04/2025", "Calendario A", "25.0", "bizum", "50.0"),
            PagoCaja("25/04/2025", "Calendario B", "50.0", "tarjeta", "25.0"),
            PagoCaja("25/04/2025", "Calendario A", "80.0", "metálico", "40.0"),
            PagoCaja("25/04/2025", "Calendario B", "40.0", "tarjeta", "20.0"),


            )
        cargarPagosCajaChica(binding.tablaCajaChica,listaPagosCaja,requireContext())
        cargarSesiones(binding.tablaSesiones,listaSesiones,requireContext())
    }
    private fun cargarPagosCajaChica(tabla:TableLayout, pagos:List<PagoCaja>, context:Context) {
        tabla.removeAllViews()

        val filaCabecera = TableRow(context)

        val titulos = listOf("Fecha", "Concepto", "Cantidad", "Tipo", "Total Parcial")
        for (titulo in titulos) {
            filaCabecera.addView(crearCelda(context, titulo))
        }
        tabla.addView(filaCabecera)
        for (pago in pagos) {
            val fila = TableRow(context)

            fila.addView(crearCelda(context, pago.fecha))
            fila.addView(crearCelda(context,pago.concepto))
            fila.addView(crearCelda(context, pago.cantidad))
            fila.addView(crearCelda(context, pago.tipo))
            fila.addView(crearCelda(context, pago.parcial))

            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            fila.foreground = ContextCompat.getDrawable(context, outValue.resourceId)

            fila.isClickable = true
            fila.isFocusable = true
            tabla.addView(fila)

        }
    }
    private fun cargarSesiones(tabla:TableLayout, sesiones:List<Sesion>, context:Context){
        tabla.removeAllViews() // Limpia la tabla antes de llenarla

        // Primero crear la cabecera
        val filaCabecera = TableRow(context)

        val titulos = listOf("Hora", "Calendario", "Nombre", "Participantes", "Total Pagado", "Estado", "Idiomas")
        for (titulo in titulos) {
            filaCabecera.addView(crearCelda(context, titulo))
        }

        tabla.addView(filaCabecera)

        for (sesion in sesiones) {
            val fila = TableRow(context)

            fila.addView(crearCelda(context, sesion.hora))
            fila.addView(crearCelda(context, sesion.calendario))
            fila.addView(crearCelda(context, sesion.nombre))
            fila.addView(crearCelda(context, sesion.participantes.toString()))
            fila.addView(crearCelda(context, "%.2f€".format(sesion.totalPagado)))
            fila.addView(crearCelda(context, sesion.estado))
            fila.addView(crearCelda(context, sesion.idiomas))

            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            fila.foreground = ContextCompat.getDrawable(context, outValue.resourceId)

            fila.isClickable = true
            fila.isFocusable = true

            /*fila.setOnClickListener {
                irADetalleDeSesion(sesion)
            }*/

            tabla.addView(fila)
        }
    }
    private fun crearCelda(context: Context, texto: String): TextView {
        return TextView(context).apply {
            text = texto
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            minimumHeight=96
            setBackgroundResource(R.drawable.tabla_home)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvCalendario.id->{
                val fragment=CalendarioFragmentDiario()
                val transaction=parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                transaction.commit()
            }
            binding.tvReservas.id->{
                val fragment=ListadoFragment()
                val transaction=parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                transaction.commit()
            }
            binding.selectFecha.id->{
                mostrarDialogoFecha()
            }
            binding.tvMailing.id->{
                val fragment=MailingFragment()
                val transaction=parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                transaction.commit()
            }
            binding.tvHoy.id->{
                volverDiaActual()
            }
            binding.tvFlechaIzquierdaHoy.id->{
                fechaActual=fechaActual.minusDays(1)
                actualizarFecha()

            }
            binding.tvFlechaDerechaHoy.id->{
                fechaActual=fechaActual.plusDays(1)
                actualizarFecha()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun volverDiaActual(){
        fechaActual = LocalDate.now()
        actualizarFecha()
    }
    @SuppressLint("NewApi")
    private fun mostrarDialogoFecha(){
        //Obtenemos fecha actual
        val fechaHoy=LocalDate.now()
        //Creamos dialogo del calendario
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val nuevaFecha = LocalDate.of(year, month + 1, dayOfMonth)
                fechaActual = nuevaFecha
                actualizarFecha()

            },
            fechaHoy.year,
            fechaHoy.monthValue - 1,
            fechaHoy.dayOfMonth
        )

        datePicker.show()
    }
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun actualizarFecha(){
        val diaActualNumero=fechaActual.dayOfMonth.toLong()
        val mesActualNumero=fechaActual.monthValue+1
        binding.tvFecha.text="${diaActualNumero}/${mesActualNumero}"
    }
}