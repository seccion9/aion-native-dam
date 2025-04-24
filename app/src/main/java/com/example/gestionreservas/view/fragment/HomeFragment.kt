package com.example.gestionreservas.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gestionreservas.databinding.FragmentHomeBinding
import com.example.gestionreservas.models.entity.Sesion

class HomeFragment: Fragment() {
    private lateinit var binding:FragmentHomeBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // // Inflamos el layout del fragmento para que cargue la vista correctamente
        instancias()
        return binding.root
    }
    private fun instancias(){
        val listaSesiones = listOf(
            Sesion("10:00", "Calendario A", "Juan Pérez", 4, 80.0, "Confirmada", "Español"),
            Sesion("12:00", "Calendario B", "Laura López", 2, 40.0, "Pendiente", "Inglés"),
        )
        cargarSesiones(binding.tablaSesiones,listaSesiones,requireContext())
    }
    private fun cargarSesiones(tabla:TableLayout, sesiones:List<Sesion>, context:Context){
        for (sesion in sesiones) {
            val fila = TableRow(context)

            fila.addView(crearCelda(context, sesion.hora))
            fila.addView(crearCelda(context, sesion.calendario))
            fila.addView(crearCelda(context, sesion.nombre))
            fila.addView(crearCelda(context, sesion.participantes.toString()))
            fila.addView(crearCelda(context, "%.2f€".format(sesion.totalPagado)))
            fila.addView(crearCelda(context, sesion.estado))
            fila.addView(crearCelda(context, sesion.idiomas))

            tabla.addView(fila)
        }
    }
    private fun crearCelda(context: Context, texto: String): TextView {
        return TextView(context).apply {
            text = texto
            textSize = 10f
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }
    }

}