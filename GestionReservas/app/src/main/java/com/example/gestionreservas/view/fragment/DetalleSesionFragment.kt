package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentDetalleSesionBinding
import com.example.gestionreservas.databinding.FragmentHomeBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.viewModel.listado.DetalleSesion.DetalleSesionViewModel
import com.example.gestionreservas.viewModel.listado.DetalleSesion.DetalleSesionViewModelFactory
import java.net.URLEncoder

class DetalleSesionFragment : Fragment(), OnClickListener {
    private lateinit var binding: FragmentDetalleSesionBinding
    private lateinit var viewModel: DetalleSesionViewModel

    private var compra: Compra? = null
    private var reserva: ItemReserva? = null
    private var pago: Pago? = null

    /**
     * Este fragmento muestra los detalles de una sesión vinculada a una compra. Utiliza un ViewModel
     * para separar la lógica de datos (compra, reserva, pago), observar sus cambios y actualizar la vista.
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetalleSesionBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Detalles Sesion"

        val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
        viewModel = ViewModelProvider(this, DetalleSesionViewModelFactory(compraRepository))
            .get(DetalleSesionViewModel::class.java)

        val sesionConCompra = arguments?.getSerializable("sesionConCompra") as? SesionConCompra
        viewModel.cargarSesion(sesionConCompra)

        observarDatos()
        instancias()
        return binding.root
    }


    /**
     * Instancias necesarias del fragment
     */
    @SuppressLint("SetTextI18n")
    private fun instancias() {
        observarDatos()
        configurarBotones()
        detectarScroll()

    }


    /**
     * Observa los datos del ViewModel (compra, reserva y pago) y actualiza la interfaz
     * de usuario cada vez que cambien. Dependiendo del contenido, se muestra un formulario
     * vacío o se cargan los datos correspondientes en la vista.
     */
    private fun observarDatos() {
        viewModel.compra.observe(viewLifecycleOwner) {
            compra = it
            if (compra == null) {
                mostrarFormularioVacio()
            } else {
                cargarDatosCompra()
            }
        }
        viewModel.reserva.observe(viewLifecycleOwner) {
            reserva = it
            cargarDatosCompra()
        }
        viewModel.pago.observe(viewLifecycleOwner) {
            pago = it
            cargarDatosCompra()
        }
    }


    /**
     * Si los datos de la compra no son nulos los carga en los edits para modificarlos.
     */
    @SuppressLint("SetTextI18n")
    private fun cargarDatosCompra() {
        if (compra == null || reserva == null || pago == null) return

        binding.tvNombre.setText("Nombre: ${compra!!.name}")
        binding.tvTelefono.setText("Teléfono: ${compra!!.phone}")
        binding.tvEmail.setText("Email: ${compra!!.mail}")
        binding.tvEstado.setText("Estado: ${reserva!!.status}")
        binding.tvFechaInicio.setText("Inicio: ${reserva!!.start}")
        binding.tvFechaFin.setText("Fin: ${reserva!!.end}")
        binding.tvSala.setText("Sala: ${reserva!!.idCalendario}")
        binding.tvParticipantes.setText("Participantes: ${reserva!!.peopleNumber}")
        binding.tvExperiencia.setText("Experiencia: ${reserva!!.idExperience}")
        binding.tvIdioma.setText("Idioma: ${compra!!.language}")
        binding.tvTotalPagado.setText("Total pagado: ${pago!!.amount} €")
        binding.tvMetodoPago.setText("Método de pago: ${pago!!.method}")
        binding.tvDNI.setText("DNI: ${compra!!.dni}")
    }


    /**
     * Recorre los edits uno a uno activandolos,oculta boton editar y muestra el boton guardar.
     */
    private fun activarEdits() {
        listOf(
            binding.tvNombre, binding.tvTelefono, binding.tvEmail, binding.tvEstado,
            binding.tvFechaInicio, binding.tvFechaFin, binding.tvSala, binding.tvParticipantes,
            binding.tvExperiencia, binding.tvIdioma, binding.tvTotalPagado,
            binding.tvMetodoPago, binding.tvDNI
        ).forEach { it.isEnabled = true }

        binding.tvEditar.visibility = View.GONE
        binding.tvGuardar.visibility = View.VISIBLE
    }

    /**
     * Valida que los campos no esten vacios y actualiza la compra y sesion..
     */
    private fun desactivarEdits() {
        if (!validarCamposObligatorios()) {
            Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        actualizarCompra()
        //Actualiza los campos en el viewModel para mostrarlos en la vista(compra,reserva,sesion)
        viewModel.actualizarSesion(compra, reserva, pago)
        actualizarEditsAFalse()

    }

    /**
     * Recorre los edits uno a uno desactivandolos,oculta boton guardar y muestra el boton editar
     */
    private fun actualizarEditsAFalse() {
        listOf(
            binding.tvNombre, binding.tvTelefono, binding.tvEmail, binding.tvEstado,
            binding.tvFechaInicio, binding.tvFechaFin, binding.tvSala, binding.tvParticipantes,
            binding.tvExperiencia, binding.tvIdioma, binding.tvTotalPagado,
            binding.tvMetodoPago, binding.tvDNI
        ).forEach { it.isEnabled = false }

        binding.tvGuardar.visibility = View.GONE
        binding.tvEditar.visibility = View.VISIBLE
    }

    /**
     * Valida que todos llos campos no sean vacios.
     */
    private fun validarCamposObligatorios(): Boolean {
        return listOf(
            binding.tvNombre, binding.tvTelefono, binding.tvEmail, binding.tvEstado,
            binding.tvFechaInicio, binding.tvFechaFin, binding.tvSala, binding.tvParticipantes,
            binding.tvExperiencia, binding.tvIdioma, binding.tvTotalPagado,
            binding.tvMetodoPago, binding.tvDNI
        ).all { it.text.toString().substringAfter(":").trim().isNotEmpty() }
    }

    /**
     * Actualiza los datos de nuestra compra una vez editados quitandole prefijos y espacios.
     * Después modifica la compra a través de nuestro viewModel.
     */
    private fun actualizarCompra() {
        compra?.apply {
            name = binding.tvNombre.text.toString().removePrefix("Nombre: ").trim()
            phone = binding.tvTelefono.text.toString().removePrefix("Teléfono: ").trim()
            mail = binding.tvEmail.text.toString().removePrefix("Email: ").trim()
            language = binding.tvIdioma.text.toString().removePrefix("Idioma: ").trim()
            dni = binding.tvDNI.text.toString().removePrefix("DNI: ").trim()
        }

        reserva?.apply {
            status = binding.tvEstado.text.toString().removePrefix("Estado: ").trim()
            start = binding.tvFechaInicio.text.toString().removePrefix("Inicio: ").trim()
            end = binding.tvFechaFin.text.toString().removePrefix("Fin: ").trim()
            idCalendario = binding.tvSala.text.toString().removePrefix("Sala: ").trim()
            peopleNumber =
                binding.tvParticipantes.text.toString().removePrefix("Participantes: ").trim()
                    .toInt()
            idExperience =
                binding.tvExperiencia.text.toString().removePrefix("Experiencia: ").trim()
        }

        pago?.apply {
            amount = binding.tvTotalPagado.text.toString().removePrefix("Total pagado: ")
                .removeSuffix(" €").trim().toDouble()
            method = binding.tvMetodoPago.text.toString().removePrefix("Método de pago: ").trim()
        }

        val token = getTokenFromSharedPreferences() ?: return
        Log.e("TOKEN", token)
        //Modifica la compra en la API al pinchar en guardar
        compra?.let {
            viewModel.modificarCompra(token, it,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        "Guardado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    /**
     * Obtiene el token,comprueba si la compra es nula y si no lo es llama al viewModel para cancelar la reserva,
     * mostrará un mensaje de confirmación o error al realizarlo.
     */
    private fun cancelarReserva() {
        val token = getTokenFromSharedPreferences() ?: return
        compra?.let {
            viewModel.cancelarReserva(token, it.id,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        "Reserva eliminada correctamente",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onError = { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    /**
     * Redirige al GMAIL para enviar correo de confirmación al usuario.
     */
    private fun enviarConfirmacion() {
        val destinatario = compra?.mail ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
            putExtra(Intent.EXTRA_SUBJECT, "Confirmación reservas")
            putExtra(Intent.EXTRA_TEXT, "Hola, esta es la confirmación de su reserva.")
        }
        startActivity(Intent.createChooser(intent, "Enviar correo con..."))
    }

    /**
     * Redirige a whatsapp para enviar mensaje al usuario.
     */
    private fun enviarMensaje() {
        val numero = "34${compra?.phone}"
        val mensaje = "Hola! Esto es un mensaje de confirmación"
        val uri = Uri.parse("https://wa.me/$numero?text=${URLEncoder.encode(mensaje, "UTF-8")}")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    /**
     * Métodos on click del fragment
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvEditar.id -> activarEdits()
            binding.tvGuardar.id -> desactivarEdits()
            binding.tvConfirmacion.id -> enviarConfirmacion()
            binding.tvWhatsapp.id -> enviarMensaje()
            binding.tvCancelar.id -> mostrarDialogoCancelar()
        }
    }


    /**
     * Mostrar dialogo antes de cancelar reserva para asegurar más el borrado de datos de la API.
     */
    private fun mostrarDialogoCancelar() {

        if (compra == null || reserva == null) {
            Toast.makeText(requireContext(), "Error: No se puede cancelar la reserva. Datos incompletos.", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación de cancelación de reserva")
        builder.setMessage(
            """¿Está seguro de que quiere cancelar la reserva? Se borrará el registro de la base de datos y no podrá recuperarlo.
        
        Nombre: ${compra?.name}
        Teléfono: ${compra?.phone}
        Email: ${compra?.mail}
        DNI: ${compra?.dni}
        Fecha Inicio: ${reserva?.start}
        Fecha Fin: ${reserva?.end}
        Id Compra: ${compra?.id}
        """
        )


        builder.setPositiveButton("Sí") { _, _ ->
            cancelarReserva()
            volverHome()
        }


        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }


    /**
     * Obtiene el token de preferencias locales.
     */
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            requireActivity().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        return token?.let { "Bearer $it" }
    }

    @SuppressLint("SetTextI18n")
    /**
     * Si no hay compra se muestra este formulario vacio
     */
    private fun mostrarFormularioVacio() {
        binding.tvNombre.setText("Nombre: ")
        binding.tvTelefono.setText("Teléfono: ")
        binding.tvEmail.setText("Email: ")
        binding.tvEstado.setText("Estado: ")
        binding.tvFechaInicio.setText("Inicio: ")
        binding.tvFechaFin.setText("Fin: ")
        binding.tvSala.setText("Sala:")
        binding.tvParticipantes.setText("Participantes: ")
        binding.tvExperiencia.setText("Experiencia: ")
        binding.tvIdioma.setText("Idioma: ")
        binding.tvTotalPagado.setText("Total pagado: ")
        binding.tvMetodoPago.setText("Método de pago: ")
        binding.tvDNI.setText("DNI: ")

    }

    /**
     * Configuracion de listeners de los botones
     */
    private fun configurarBotones() {
        binding.tvEditar.setOnClickListener(this)
        binding.tvGuardar.setOnClickListener(this)
        binding.tvConfirmacion.setOnClickListener(this)
        binding.tvWhatsapp.setOnClickListener(this)
        binding.btnScrollSubir?.setOnClickListener(this)
        binding.tvCancelar.setOnClickListener(this)
    }

    /**
     * Detecta si se hace scroll en la pantalla para mostrar botón de subir
     */
    private fun detectarScroll() {
        binding.nestedScroll?.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 200) {
                binding.btnScrollSubir?.visibility = View.VISIBLE
            } else {
                binding.btnScrollSubir?.visibility = View.GONE
            }
        }
    }

    /**
     * Vuelve al fragment home despues de eliminar la reserva
     */
    private fun volverHome(){
        val fragment=HomeFragment()
        val transaccion=parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_principal,fragment)
        transaccion.commit()

    }
}