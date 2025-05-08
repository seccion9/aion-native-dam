package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
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
import java.net.URLEncoder

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
        /*
        Si los datos no son nulos se cargan en la interfaz y si son nulos no se carga nada y se depura
        por el log.
         */
        if (sesionConCompra != null) {
            sesion = sesionConCompra.sesion

            if (sesionConCompra.compra != null) {
                compraRecuperada = sesionConCompra.compra
                reserva = compraRecuperada.items.lastOrNull()!!
                pago = compraRecuperada.payments.lastOrNull()!!
                instancias()
            } else {
                Log.w("DetalleSesionFragment", "Sesión sin compra: modo nuevo")
                mostrarFormularioVacio()
            }

        } else {
            Log.w("DetalleSesionFragment", "No se recibió sesión con compra")
        }
        configurarBotones()
        return binding.root
    }
    @SuppressLint("SetTextI18n")
    private fun instancias(){
        cargarDatosCompra()
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
        if (!validarCamposObligatorios()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            actualizarEditsAFalse()
            return
        }else{
            actualizarEditsAFalse()
            actualizarCompra()
        }

    }
    private fun actualizarEditsAFalse(){
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
    }
    private fun validarCamposObligatorios(): Boolean {
        val campos = listOf(
            binding.tvNombre,
            binding.tvTelefono,
            binding.tvEmail,
            binding.tvEstado,
            binding.tvFechaInicio,
            binding.tvFechaFin,
            binding.tvSala,
            binding.tvParticipantes,
            binding.tvExperiencia,
            binding.tvIdioma,
            binding.tvTotalPagado,
            binding.tvMetodoPago,
            binding.tvDNI
        )

        return campos.all { editText ->
            val texto = editText.text.toString().substringAfter(":").trim()
            texto.isNotEmpty()
        }
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
    private fun enviarConfirmacion(){
        //Obtenemos mail y rellenamos asunto y cuerpo de prueba
        val destinatario=compraRecuperada.mail
        val asunto="Confirmación reservas"
        val cuerpo= "Hola, esta es la confirmación de su reserva. ¡Gracias por confiar en nosotros!"
        //Uri de apps de correos del movil(nos mostrara nuestras app instaladas)
        val uri= Uri.parse("mailto:$destinatario")
        //Intentamos mandar el correo
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
        }

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(Intent.createChooser(intent, "Enviar correo con..."))
        } else {
            Toast.makeText(requireContext(), "No hay apps de correo instaladas", Toast.LENGTH_SHORT).show()
        }

    }
    private fun enviarMensaje(){
        //Recuperamos telefono y hacemos un mensaje de prueba
        val numero = "34${compraRecuperada.phone}"
        val mensaje = "Hola! Esto es un mensaje de prueba"

        try {//Usamos la uri de Whatsapp y se pasan el numero y mensaje
            val uri = Uri.parse("https://wa.me/$numero?text=${URLEncoder.encode(mensaje, "UTF-8")}")
            //Intentamos lanzar Whatsapp
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
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
            binding.tvConfirmacion.id->{
                enviarConfirmacion()
            }
            binding.tvWhatsapp.id->{
                enviarMensaje()
            }
        }
    }
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }
    @SuppressLint("SetTextI18n")
    private fun mostrarFormularioVacio() {
        binding.tvNombre.setText("Nombre: ")
        binding.tvTelefono.setText("Teléfono: ")
        binding.tvEmail.setText("Email: ")
        binding.tvEstado.setText("Estado: ")
        binding.tvFechaInicio.setText("Inicio: ")
        binding.tvFechaFin.setText("Fin: ")
        binding.tvSala.setText("Sala: ${sesion.calendario}")
        binding.tvParticipantes.setText("Participantes: ")
        binding.tvExperiencia.setText("Experiencia: ")
        binding.tvIdioma.setText("Idioma: ")
        binding.tvTotalPagado.setText("Total pagado: ")
        binding.tvMetodoPago.setText("Método de pago: ")
        binding.tvDNI.setText("DNI: ")

    }
    private fun configurarBotones() {
        binding.tvEditar.setOnClickListener(this)
        binding.tvGuardar.setOnClickListener(this)
        binding.tvConfirmacion.setOnClickListener(this)
        binding.tvWhatsapp.setOnClickListener(this)
    }

}