package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentHomeBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.Sesion
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.network.RetrofitInstance
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
            val sesiones = withContext(Dispatchers.Default) {
                transformarComprasASesiones(compras, fechaActual)
            }

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
            cargarSesiones(binding.tablaSesiones, sesiones, requireContext())
            cargarPagosCajaChica(binding.tablaCajaChica, pagosCajaChicaYReservas, requireContext())

            binding.tablaSesiones.visibility = View.VISIBLE
            binding.tablaCajaChica.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error en la API fake: ${e.localizedMessage}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun transformarComprasASesiones(
        compras: List<Compra>,
        fechaSeleccionada: LocalDate
    ): List<SesionConCompra> {
        val sesiones = mutableListOf<SesionConCompra>()

        compras.forEach { compra ->
            compra.items.forEach { item ->
                val fechaItem = LocalDate.parse(item.start.substring(0, 10))

                if (fechaItem == fechaSeleccionada) {
                    val sesion = Sesion(
                        hora = item.start.substring(11, 16),
                        calendario = item.idCalendario,
                        nombre = compra.name,
                        participantes = item.peopleNumber,
                        totalPagado = item.priceTotal,
                        estado = compra.status,
                        idiomas = compra.language
                    )
                    sesiones.add(SesionConCompra(sesion, compra))
                }
            }
        }

        return sesiones
    }

    private fun cargarPagosCajaChica(tabla:TableLayout, pagos:List<PagoCaja>, context:Context) {
        tabla.removeAllViews()
        var totalParcial=0
        val filaCabecera = TableRow(context)

        val titulos = listOf("Fecha", "Concepto", "Cantidad", "Tipo", "Total Parcial")
        for (titulo in titulos) {
            filaCabecera.addView(crearCelda(context, titulo))
        }
        tabla.addView(filaCabecera)
        for (pago in pagos) {
            val fila = TableRow(context)
            val cantidadNumerica = pago.cantidad.replace("€", "").trim().replace(",", ".").toDouble()
            totalParcial += cantidadNumerica.toInt()
            var totalString="$totalParcial €"
            fila.addView(crearCelda(context, pago.fecha))
            fila.addView(crearCelda(context,pago.concepto))
            fila.addView(crearCelda(context, pago.cantidad))
            fila.addView(crearCelda(context, pago.tipo))
            fila.addView(crearCelda(context, totalString))

            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            fila.foreground = ContextCompat.getDrawable(context, outValue.resourceId)

            fila.isClickable = true
            fila.isFocusable = true
            tabla.addView(fila)

        }
    }
    private fun cargarSesiones(tabla:TableLayout, sesiones: List<SesionConCompra>, context:Context){
        tabla.removeAllViews()

        val filaCabecera = TableRow(context)
        val titulos = listOf("Hora", "Calendario", "Nombre", "Participantes", "Total Pagado", "Estado", "Idiomas")
        for (titulo in titulos) {
            filaCabecera.addView(crearCelda(context, titulo))
        }
        tabla.addView(filaCabecera)

        for (sesionConCompra in sesiones) {
            val fila = TableRow(context)
            val sesion = sesionConCompra.sesion

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

            fila.setOnClickListener {
                irADetalleDeSesion(sesionConCompra)
            }

            tabla.addView(fila)

        }
    }
    //Esta funcion te lleva al detalle de la sesion personalizada y sus datos
    private fun irADetalleDeSesion(sesionConCompra:SesionConCompra){
        val fragment=DetalleSesionFragment()
        val bundle=Bundle()
        bundle.putSerializable("sesionConCompra",sesionConCompra)
        fragment.arguments=bundle
        cambiarFragmento(fragment)
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
        cargarPagosCajaChica(binding.tablaCajaChica,pagosCajaChicaYReservas,requireContext())
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