package com.example.gestionreservas.view.fragment

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentConfiguracionBinding
import com.example.gestionreservas.repository.EmailRepository
import com.example.gestionreservas.view.activities.MainActivity
import com.example.gestionreservas.viewModel.Configuracion.ConfiguracionViewModel
import com.example.gestionreservas.viewModel.Configuracion.ConfiguracionViewModelFactory

class ConfiguracionFragment:Fragment(),OnClickListener {
    private lateinit var binding:FragmentConfiguracionBinding
    private lateinit var viewModel: ConfiguracionViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentConfiguracionBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Configuracion"
        //Obtenemos preferencias para configurar botones de notificaciones
        val prefs = requireContext().getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val notificacionesActivas = prefs.getBoolean("notificaciones_activadas", false)
        if (notificacionesActivas) {
            pedirPermisoNotificaciones()
        }
        val mailingRepository = EmailRepository
        val factory = ConfiguracionViewModelFactory(mailingRepository)
        viewModel = ViewModelProvider(this, factory).get(ConfiguracionViewModel::class.java)

        instancias()

        return binding.root
    }

    /**
     * Pide permiso para activar notificaciones
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(requireContext(), "Notificaciones activadas con exito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No tiene permisos para activar notificaciones", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun instancias(){

        // Pedir el email al ViewModel (se almacenará en LiveData)
        viewModel.obtenerEmailUsuario(requireContext())
        viewModel.cargarModoOscuro(requireContext())


        observers()

        //Instancia funciones inicilaes
        configurarNotificaciones()
        btnSpannable()
        crearCanalNotificaciones()

        binding.tvCerrarSesionGlobal.setOnClickListener(this)

    }
    private fun observers(){
        // Observar cambios en el correo y actualizar la UI
        viewModel.email.observe(viewLifecycleOwner) { email ->
            binding.tvCorreoGmail.text = email ?: "No conectado"

            // Solo habilitar botón de cerrar sesión si está conectado
            if (!email.isNullOrEmpty() && email != "No conectado") {
                binding.tvCerrarSesionGmail.setOnClickListener(this)
            } else {
                binding.tvCerrarSesionGmail.setOnClickListener(null)
            }
        }
        // Observar para reflejar en el switch
        viewModel.modoOscuro.observe(viewLifecycleOwner) { activado ->
            binding.switchModoOscuro.isChecked = activado
        }
        // Guardar nuevo valor
        binding.switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            viewModel.cambiarModoOscuro(requireContext(), isChecked)
        }
    }

    /**
     * Botones spannable debajo de edits para mejorar UX
     */
    private fun btnSpannable() {
        val texto = SpannableString("Desconectar")

        val color = ContextCompat.getColor(requireContext(), R.color.cerrar_sesion)

        texto.setSpan(ForegroundColorSpan(color), 0, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        texto.setSpan(UnderlineSpan(), 0, texto.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvCerrarSesionGmail.text = texto

        val textoSesion = SpannableString("Cerrar sesión")

        textoSesion.setSpan(ForegroundColorSpan(color), 0, textoSesion.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textoSesion.setSpan(UnderlineSpan(), 0, textoSesion.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvCerrarSesionGlobal.text = textoSesion
    }

    /**
     * Carga el estado de las notificaciones y controla si se cambian o no a traves del viewmodel
     */
    private fun configurarNotificaciones() {
        viewModel.cargarEstadoNotificaciones(requireContext())

        viewModel.notificacionesActivas.observe(viewLifecycleOwner) { activadas ->
            binding.switchNotificaciones.setOnCheckedChangeListener(null)
            binding.switchNotificaciones.isChecked = activadas

            binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    pedirPermisoNotificaciones()
                    Toast.makeText(requireContext(), "Notificaciones activadas", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
                }
                viewModel.cambiarEstadoNotificaciones(requireContext(), isChecked)
            }
        }
    }

    /**
     * Comprueba la version de android para pedir permisos
     */
    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    /**
     * Funciones on click
     */
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvCerrarSesionGmail.id->{

                EmailRepository.cerrarSesion(requireContext()) {
                    Toast.makeText(requireContext(), "Sesión de Gmail cerrada", Toast.LENGTH_SHORT).show()
                    requireActivity().invalidateOptionsMenu()
                    binding.tvCorreoGmail.text="No conectado"
                }
            }
            binding.tvCerrarSesionGlobal.id->{
                cerrarSesion(requireContext())
            }
        }
    }

    /**
     * Cierra sesión llevandote a login y borra token de shared preferneces
     */
    private fun cerrarSesion(context: Context) {
        val preferencias = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        preferencias.edit().remove("auth_token").apply()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    /**
     * Crea canal de notificaciones para las reservas
     */
    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reservas"
            val descriptionText = "Canal para notificaciones de nuevas reservas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("canal_reservas", name, importance)


            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}