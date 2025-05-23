package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentHomeBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.network.RetrofitInstance
import com.example.gestionreservas.utils.TablaBuilder
import com.example.gestionreservas.utils.TablaBuilder.crearCelda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate


class HomeFragment: Fragment(),OnClickListener {
    @SuppressLint("NewApi")
    private var fechaActual: LocalDate = LocalDate.now()
    private var token: String? = null
    private lateinit var binding: FragmentHomeBinding
    private var pagosCajaChicaYReservas: MutableList<PagoCaja> = mutableListOf()
    private val cajaChicaRepository = CajaChicaRepository(RetrofitFakeInstance)
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private var listaReservasSpinner= mutableListOf("- - Seleccione Reserva - -")
    private lateinit var listaCompras : MutableList<Compra>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Home"

        // // Inflamos el layout del fragmento para que cargue la vista correctamente
        instancias()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        actualizarFecha()
        detectarScroll()
        //Instancias Click
        binding.btnEnviarComentarios.setOnClickListener(this)
        binding.tvCalendario.setOnClickListener(this)
        binding.tvReservas.setOnClickListener(this)
        binding.selectFecha.setOnClickListener(this)
        binding.tvMailing.setOnClickListener(this)
        binding.tvHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        binding.btnScrollSubir.setOnClickListener(this)
        binding.btnEnviarCaja.setOnClickListener(this)
        //Metodos para cargar datos

        cargarDatosSesionesHoy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarDatosSesionesHoy() {

        binding.tablaSesiones.visibility = View.GONE
        binding.tablaCajaChica.visibility=View.GONE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = getTokenFromSharedPreferences()
                val fecha = fechaActual.toString()

                val response = RetrofitInstance.api.obtenerReservas(token!!, listOf(1, 2), fecha)

                if (response.isSuccessful && response.body() != null && response.body()!!
                        .isNotEmpty()
                ) {
                    //val sesiones = transformarExperienciasASesiones(response.body()!!)
                    //cargarSesiones(binding.tablaSesiones, sesiones, requireContext())
                } else {
                    cargarDatosDesdeJsonServer()

                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error en la API real: ${e.message}")
                cargarDatosDesdeJsonServer()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun cargarDatosDesdeJsonServer() {
        val token = getTokenFromSharedPreferences()
        try {
            val compras = cajaChicaRepository.obtenerCompras(token!!)
            listaCompras=compras as MutableList<Compra>
            val sesiones = withContext(Dispatchers.Default) {
                compraRepository.transformarComprasASesiones(compras, fechaActual)
            }
            sesiones.forEach{sesion ->
                listaReservasSpinner.add(
                    "${sesion.sesion.hora}|${sesion.sesion.nombre}|${sesion.sesion.calendario}|${sesion.compra?.id}"
                )
            }
            rellenarSpinnerReservas()
            val pagosReservas = withContext(Dispatchers.Default) {
                cajaChicaRepository.transformarComprasAPagos(compras, fechaActual)
            }

            val pagosCajaChica = withContext(Dispatchers.IO) {
                val fechaStr = fechaActual.toString()
                val pagosApi = cajaChicaRepository.obtenerPagosDelDia(token, fechaStr)
                cajaChicaRepository.transformarPagosCajaApi(pagosApi)
            }

            // Combinar y guardar
            pagosCajaChicaYReservas = (pagosReservas + pagosCajaChica).toMutableList()

            // Mostrar en interfaz
            Log.e("Sesiones día: $fechaActual", sesiones.toString())
            TablaBuilder.construirTablaSesiones(binding.tablaSesiones, sesiones, requireContext()) { sesionConCompra ->
                irADetalleDeSesion(sesionConCompra)
            }
            TablaBuilder.construirTablaPagos(binding.tablaCajaChica, pagosCajaChicaYReservas, requireContext())

            binding.tablaSesiones.visibility = View.VISIBLE
            binding.tablaCajaChica.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error en la API fake: ${e.localizedMessage}")
        }
    }

    /**
     * Rellena el spinner de reservas con los datos de las reservas de hoy.
     */
    private fun rellenarSpinnerReservas(){

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listaReservasSpinner
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerReservas.adapter = adapter
    }
    //Esta funcion te lleva al detalle de la sesion personalizada y sus datos
    private fun irADetalleDeSesion(sesionConCompra:SesionConCompra){
        val fragment=DetalleSesionFragment()
        val bundle=Bundle()
        bundle.putSerializable("sesionConCompra",sesionConCompra)
        fragment.arguments=bundle
        cambiarFragmento(fragment)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvCalendario.id->{
                val fragment=CalendarioFragmentDiario()
                cambiarFragmento(fragment)
            }
            binding.tvReservas.id->{
                val fragment=ListadoFragment()
                cambiarFragmento(fragment)
            }
            binding.selectFecha.id->{
                mostrarDialogoFecha()
            }
            binding.tvMailing.id->{
                val fragment=MailingFragment()
                cambiarFragmento(fragment)
            }
            binding.tvHoy.id->{
                volverDiaActual()
                cargarDatosSesionesHoy()
            }
            binding.tvFlechaIzquierdaHoy.id->{
                fechaActual=fechaActual.minusDays(1)
                actualizarFecha()
                cargarDatosSesionesHoy()
            }
            binding.tvFlechaDerechaHoy.id->{
                fechaActual=fechaActual.plusDays(1)
                actualizarFecha()
                cargarDatosSesionesHoy()
            }
            binding.btnScrollSubir.id->{
                binding.nestedScroll.smoothScrollTo(0, 0)
            }
            binding.btnEnviarCaja.id->{
                agregarDineroCajaChica()
            }
            binding.btnEnviarComentarios.id->{
                obtenerReservaParaPatch()
            }
        }
    }

    /**
     * Obtiene las partes de las reservas del dia y las compara con las compras,a través de los bucles comparamos
     * los campos id,nombre y fecha y si encuentrta coincidencia obtiene la compra. Después obtenemos el comentario
     * y hacemos el patch de la compra modificandola.
     */
    private fun obtenerReservaParaPatch() {
        if (binding.spinnerReservas.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Por favor seleccione una reserva ", Toast.LENGTH_SHORT).show()
            return
        }

        val lineaSeleccionada = binding.spinnerReservas.selectedItem.toString()
        val motivo = binding.spinnerComentarios.selectedItem.toString()
        val comentario = binding.editComentario.text.toString().trim()

        viewLifecycleOwner.lifecycleScope.launch {
            val compraModificada = compraRepository.enviarComentarioACompra(token = getTokenFromSharedPreferences()!!,
                listaCompras = listaCompras, lineaSeleccionada = lineaSeleccionada,
                comentario = comentario, motivo = motivo)

            if (compraModificada != null) {
                Toast.makeText(requireContext(), "Comentario añadido correctamente", Toast.LENGTH_SHORT).show()
                binding.editComentario.text.clear()

                // Enviar correo con los datos de la compra modificada
                val subject = Uri.encode(motivo + " reserva con id ${compraModificada.id}")
                val body = Uri.encode(
                    "Hola ${compraModificada.name},\n\n" +
                            "Hemos añadido el siguiente comentario a su reserva ${compraModificada.id}\n\n" +
                            "$motivo : $comentario\n\n" +
                            "Puede ponerse en contacto con nosotros a través de :\n\n" +
                            "Email: gestorReservas@gmail.com\n\n" +
                            "Teléfono : 633449393\n\n" +
                            "Gracias."
                )

                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${compraModificada.mail}?subject=$subject&body=$body")
                }

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No se pudo abrir una app de correo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Error al actualizar la compra", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun agregarDineroCajaChica(){
        val dia = fechaActual.dayOfMonth
        val mes = fechaActual.month.value
        val year=fechaActual.year
        val fechaFormateada = "$year-/$dia-$mes"
        var concepto=binding.editCopcepto.text.toString()
        var cantidad=binding.editCantidad.text.toString()
        var pagoNuevo=PagoCaja(fechaFormateada,concepto,cantidad,"","")
        pagosCajaChicaYReservas.add(pagoNuevo)
        TablaBuilder.construirTablaPagos(binding.tablaCajaChica, pagosCajaChicaYReservas, requireContext())
        binding.editCopcepto.text.clear()
        binding.editCantidad.text.clear()
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
                cargarDatosSesionesHoy()
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
        val dia = fechaActual.dayOfMonth
        val mes = fechaActual.month.value

        val fechaFormateada = "$dia/$mes"
        binding.tvFecha.text = fechaFormateada
    }
    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        Log.e("TOKEN DEVUELTO","TOKEN : $token")
        return token?.let { "Bearer $it" }
    }
    private fun cambiarFragmento(fragment:Fragment){
        val transaction=parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_principal,fragment)
            .addToBackStack(null)
        transaction.commit()
    }
    private fun detectarScroll(){
        binding.nestedScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 200) {
                binding.btnScrollSubir.visibility = View.VISIBLE
            } else {
                binding.btnScrollSubir.visibility = View.GONE
            }
        }
    }

}