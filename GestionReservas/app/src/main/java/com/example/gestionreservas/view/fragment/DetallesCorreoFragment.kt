package com.example.gestionreservas.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gestionreservas.databinding.FragmentDetallesCorreoBinding
import com.example.gestionreservas.model.CorreoItem

class DetallesCorreoFragment:Fragment(),OnClickListener {
    private lateinit var binding:FragmentDetallesCorreoBinding
    private lateinit var mensajeRecibido:CorreoItem
    private lateinit var correoCliente:String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentDetallesCorreoBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Confirmación Reserva"
        mensajeRecibido=arguments?.getSerializable("mensaje") as CorreoItem
        Log.e("Mensaje recibido","Mensaje: $mensajeRecibido")
        instancias()
        return binding.root
    }
    private fun instancias(){
        //Asignacion items en vista
        asignarDetallesMensajeItems()
        //Instancias de botones
        binding.btnReply.setOnClickListener(this)
        //Obtener correo limpio
        correoCliente=obtenerCorreo(mensajeRecibido.remitente)

    }
    private fun asignarDetallesMensajeItems(){
        binding.tvCuerpoMensaje.text=mensajeRecibido.cuerpoPreview
        binding.tvNombre.text=mensajeRecibido.asunto.take(30)
        binding.tvCorreo.text=mensajeRecibido.remitente.take(50)
        binding.tvIcono.text=inicialParaIcono(mensajeRecibido.remitente.first().uppercase())
        binding.tvFecha.text=mensajeRecibido.fecha


    }
    private fun inicialParaIcono(texto: String): String {
        val letra = texto.firstOrNull()?.uppercaseChar()
        return if (letra != null && letra in 'A'..'Z') letra.toString() else "G" // emoji usuario
    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnReply.id -> {
                redirigirRespuestaGmail()
            }
        }
    }
    private fun redirigirRespuestaGmail(){
        val uri= Uri.parse("mailto:$correoCliente")
        //Intentamos mandar el correo
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(correoCliente))
        }

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(Intent.createChooser(intent, "Enviar correo con..."))
        } else {
            Toast.makeText(requireContext(), "No hay apps de correo instaladas", Toast.LENGTH_SHORT).show()
        }
    }
    private fun obtenerCorreo(texto:String):String{
        val correo=texto.substringAfter("<").substringBefore(">")
        return correo
    }
}