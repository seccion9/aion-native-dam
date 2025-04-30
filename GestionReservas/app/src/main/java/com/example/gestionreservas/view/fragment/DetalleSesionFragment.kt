package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionreservas.databinding.FragmentDetalleSesionBinding
import com.example.gestionreservas.databinding.FragmentHomeBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra

class DetalleSesionFragment: Fragment() {
    private lateinit var binding:FragmentDetalleSesionBinding
    private lateinit var sesion: Sesion
    private lateinit var compraRecuperada: Compra
    private lateinit var reserva:ItemReserva
    private lateinit var pago:Pago
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetalleSesionBinding.inflate(inflater, container, false)
        val sesionConCompra = arguments?.getSerializable("sesionConCompra") as? SesionConCompra
        sesion = sesionConCompra?.sesion!!
        compraRecuperada = sesionConCompra.compra
        reserva=compraRecuperada.items.last()
        pago=compraRecuperada.payments.last()
        // Inflamos el layout del fragmento para que cargue la vista correctamente
        instancias()
        return binding.root
    }
    private fun instancias(){
        cargarDatosCompra()
    }
    @SuppressLint("SetTextI18n")
    private fun cargarDatosCompra(){
        binding.tvNombre.text = "Nombre: ${compraRecuperada.name}"
        binding.tvTelefono.text = "Teléfono: ${compraRecuperada.phone}"
        binding.tvEmail.text = "Email: ${compraRecuperada.mail}"
        binding.tvEstado.text = "Estado: ${reserva.status}"
        binding.tvFechaInicio.text = "Inicio: ${reserva.start}"
        binding.tvFechaFin.text = "Fin: ${reserva.end}"
        binding.tvSala.text = "Sala: ${reserva.idCalendario}"
        binding.tvParticipantes.text = "Participantes: ${reserva.peopleNumber}"
        binding.tvExperiencia.text = "Experiencia: ${reserva.idExperience}"
        binding.tvIdioma.text = "Idioma: ${compraRecuperada.language}"
        binding.tvTotalPagado.text = "Total pagado: ${pago.amount} €"
        binding.tvMetodoPago.text = "Método de pago: ${pago.method}"
        binding.tvDNI.text="DNI: ${compraRecuperada.dni}"

    }
}