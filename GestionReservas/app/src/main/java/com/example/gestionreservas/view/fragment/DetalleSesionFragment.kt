package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gestionreservas.databinding.FragmentDetalleSesionBinding
import com.example.gestionreservas.databinding.FragmentHomeBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.network.RetrofitFakeInstance
import kotlinx.coroutines.launch

class DetalleSesionFragment: Fragment(),OnClickListener {
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
    @SuppressLint("SetTextI18n")
    private fun instancias(){
        cargarDatosCompra()
        //Instancias botones
        binding.tvEditar.setOnClickListener(this)
        binding.tvGuardar.setOnClickListener(this)
        //Instancias edits
        configurarEditConEtiqueta(binding.tvNombre, "Nombre")
        configurarEditConEtiqueta(binding.tvTelefono, "Teléfono")
        configurarEditConEtiqueta(binding.tvEmail, "Email")
        configurarEditConEtiqueta(binding.tvEstado, "Estado")
        configurarEditConEtiqueta(binding.tvFechaInicio, "Inicio")
        configurarEditConEtiqueta(binding.tvFechaFin, "Fin")
        configurarEditConEtiqueta(binding.tvSala, "Sala")
        configurarEditConEtiqueta(binding.tvParticipantes, "Participantes")
        configurarEditConEtiqueta(binding.tvExperiencia, "Experiencia")
        configurarEditConEtiqueta(binding.tvIdioma, "Idioma")
        configurarEditConEtiqueta(binding.tvTotalPagado, "Total pagado", " €")
        configurarEditConEtiqueta(binding.tvMetodoPago, "Método de pago")
        configurarEditConEtiqueta(binding.tvDNI, "DNI")

    }
    /*Obtiene el foco del edit y con split sacamos el valor del edit cuando pierde el foco se guarda
    * obteniendo la etiqueta y el valor nuevo del edit
    * */
    private fun configurarEditConEtiqueta(editText: EditText, etiqueta: String, sufijo: String = "") {
        editText.onFocusChangeListener = View.OnFocusChangeListener { view, editFoco ->
            val campo = view as EditText
            if (editFoco) {
                val partes = campo.text.toString().split(": ", limit = 2)
                if (partes.size == 2) campo.setText(partes[1].removeSuffix(sufijo).trim())
            } else {
                val nuevoTexto = campo.text.toString().trim()
                campo.setText("$etiqueta: $nuevoTexto$sufijo")
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun cargarDatosCompra(){
        binding.tvNombre.setText("Nombre: ${compraRecuperada.name}")
        binding.tvTelefono.setText("Teléfono: ${compraRecuperada.phone}")
        binding.tvEmail.setText("Email: ${compraRecuperada.mail}")
        binding.tvEstado.setText("Estado: ${reserva.status}")
        binding.tvFechaInicio.setText("Inicio: ${reserva.start}")
        binding.tvFechaFin.setText("Fin: ${reserva.end}")
        binding.tvSala.setText("Sala: ${reserva.idCalendario}")
        binding.tvParticipantes.setText("Participantes: ${reserva.peopleNumber}")
        binding.tvExperiencia.setText("Experiencia: ${reserva.idExperience}")
        binding.tvIdioma.setText("Idioma: ${compraRecuperada.language}")
        binding.tvTotalPagado.setText("Total pagado: ${pago.amount} €")
        binding.tvMetodoPago.setText("Método de pago: ${pago.method}")
        binding.tvDNI.setText("DNI: ${compraRecuperada.dni}")

    }
    private fun modificarDatosCompra(){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = getTokenFromSharedPreferences()
                val compraModificada = compraRecuperada
                val id = compraRecuperada.id.trim()

                Log.d("PATCH_COMPRA", "ID a enviar: $id")
                val response=RetrofitFakeInstance.apiFake.patchCompra(token.toString(),id,compraModificada)
                if (response.isSuccessful) {
                    Log.d("DetalleSesionFragment", "Compra modificada correctamente")
                } else {
                    Log.e("DetalleSesionFragment", "Error en respuesta PATCH: ${response.code()} - ${response.message()}")
                }

            }catch (e: Exception) {
                Log.e("DetalleSesionFragment", "Error al modificar compra: ${e.message}")
            }
        }

    }

    private fun activarEdits(){
        binding.tvNombre.isEnabled=true
        binding.tvTelefono.isEnabled=true
        binding.tvEmail.isEnabled=true
        binding.tvEstado.isEnabled=true
        binding.tvFechaInicio.isEnabled=true
        binding.tvFechaFin.isEnabled=true
        binding.tvSala.isEnabled=true
        binding.tvParticipantes.isEnabled=true
        binding.tvExperiencia.isEnabled=true
        binding.tvIdioma.isEnabled=true
        binding.tvTotalPagado.isEnabled=true
        binding.tvMetodoPago.isEnabled=true
        binding.tvDNI.isEnabled=true
        binding.tvEditar.visibility = View.GONE
        binding.tvGuardar.visibility = View.VISIBLE
    }
    private fun desactivarEdits(){
        binding.tvNombre.isEnabled=false
        binding.tvTelefono.isEnabled=false
        binding.tvEmail.isEnabled=false
        binding.tvEstado.isEnabled=false
        binding.tvFechaInicio.isEnabled=false
        binding.tvFechaFin.isEnabled=false
        binding.tvSala.isEnabled=false
        binding.tvParticipantes.isEnabled=false
        binding.tvExperiencia.isEnabled=false
        binding.tvIdioma.isEnabled=false
        binding.tvTotalPagado.isEnabled=false
        binding.tvMetodoPago.isEnabled=false
        binding.tvDNI.isEnabled=false
        binding.tvGuardar.visibility = View.GONE
        binding.tvEditar.visibility = View.VISIBLE
        actualizarCompra()
    }
    private fun actualizarCompra(){
        compraRecuperada.name = binding.tvNombre.text.toString().removePrefix("Nombre: ").trim()
        compraRecuperada.phone = binding.tvTelefono.text.toString().removePrefix("Teléfono: ").trim()
        compraRecuperada.mail = binding.tvEmail.text.toString().removePrefix("Email: ").trim()
        compraRecuperada.language = binding.tvIdioma.text.toString().removePrefix("Idioma: ").trim()
        compraRecuperada.dni = binding.tvDNI.text.toString().removePrefix("DNI: ").trim()

        // Actualizar reserva
        reserva.status = binding.tvEstado.text.toString().removePrefix("Estado: ").trim()
        reserva.start = binding.tvFechaInicio.text.toString().removePrefix("Inicio: ").trim()
        reserva.end = binding.tvFechaFin.text.toString().removePrefix("Fin: ").trim()
        reserva.idCalendario = binding.tvSala.text.toString().removePrefix("Sala: ").trim()
        reserva.peopleNumber = binding.tvParticipantes.text.toString()
            .removePrefix("Participantes: ").trim().toIntOrNull() ?: reserva.peopleNumber
        reserva.idExperience = binding.tvExperiencia.text.toString().removePrefix("Experiencia: ").trim()

        // Actualizar pago
        pago.amount = binding.tvTotalPagado.text.toString()
            .removePrefix("Total pagado: ").removeSuffix(" €")
            .trim().toDoubleOrNull() ?: pago.amount
        pago.method = binding.tvMetodoPago.text.toString().removePrefix("Método de pago: ").trim()
    }
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvEditar.id->{
                activarEdits()
            }
            binding.tvGuardar.id-> {
                desactivarEdits()
                modificarDatosCompra()
            }
        }
    }
}