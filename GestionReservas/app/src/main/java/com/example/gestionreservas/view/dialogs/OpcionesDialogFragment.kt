package com.example.gestionreservas.view.dialogs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.gestionreservas.R
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.view.fragment.DetalleSesionFragment
import java.net.URLEncoder

class OpcionesDialogFragment : DialogFragment() {
    private var sesionConCompra: SesionConCompra? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        sesionConCompra = arguments?.getSerializable("arg_sesion") as? SesionConCompra
        return inflater.inflate(R.layout.dialog_opciones_sesion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<CardView>(R.id.cardLlamarCliente).setOnClickListener {
            redirigirALlamada()
            dismiss()
        }

        view.findViewById<CardView>(R.id.cardEnviarWhatsapp).setOnClickListener {
            enviarMensaje()
            dismiss()
        }
        view.findViewById<CardView>(R.id.cardEnviarCorreo).setOnClickListener {
            enviarMensajeGmail()
            dismiss()
        }
        view.findViewById<CardView>(R.id.cardEditarReserva).setOnClickListener {
            sesionConCompra?.let {
                irADetalleDeSesion(it)
            }
            dismiss()
        }


    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    companion object {
        fun newInstance(sesionConCompra: SesionConCompra): OpcionesDialogFragment {
            val fragment = OpcionesDialogFragment()
            val args = Bundle()
            args.putSerializable("arg_sesion", sesionConCompra)
            fragment.arguments = args
            return fragment
        }
    }

    private fun irADetalleDeSesion(sesionConCompra:SesionConCompra){
        val fragment= DetalleSesionFragment()
        val bundle=Bundle()
        bundle.putSerializable("sesionConCompra",sesionConCompra)
        fragment.arguments=bundle
        cambiarFragmento(fragment)
    }
    private fun cambiarFragmento(fragment: Fragment){
        val transaction=parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_principal,fragment)
            .addToBackStack(null)
        transaction.commit()
    }
    /**
     * Redirige al GMAIL para enviar correo al usuario.
     */
    private fun enviarMensajeGmail() {
        val destinatario = sesionConCompra?.compra?.mail ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
            putExtra(Intent.EXTRA_SUBJECT, "Mensaje de prueba")
            putExtra(Intent.EXTRA_TEXT, "Hola, este es un mensaje de prueba")
        }
        startActivity(Intent.createChooser(intent, "Enviar correo con..."))
    }
    /**
     * Redirige a whatsapp para enviar mensaje al usuario.
     */
    private fun enviarMensaje() {
        val numero = "34${sesionConCompra?.compra?.phone}"
        val mensaje = "Hola! Esto es un mensaje de prueba"
        val uri = Uri.parse("https://wa.me/$numero?text=${URLEncoder.encode(mensaje, "UTF-8")}")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
    /**
     * Redirige a llamar al usuario.
     */
    private fun redirigirALlamada() {
        val numero = sesionConCompra?.compra?.phone ?: return
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$numero")
        }
        startActivity(intent)
    }

}
