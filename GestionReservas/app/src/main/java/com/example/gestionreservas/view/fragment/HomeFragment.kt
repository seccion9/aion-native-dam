package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentHomeBinding
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.entity.ExperienciaCompleta
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.models.repository.ExperienciaRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.ui.helpers.StretchAnimationHelper
import com.example.gestionreservas.utils.TablaBuilder
import com.example.gestionreservas.view.adapter.AdaptadorCompra
import com.example.gestionreservas.view.dialogs.OpcionesDialogFragment
import com.example.gestionreservas.viewModel.Home.HomeViewModel
import com.example.gestionreservas.viewModel.Home.HomeViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID


class HomeFragment: Fragment(),OnClickListener {
    private lateinit var binding: FragmentHomeBinding
    private val cajaChicaRepository = CajaChicaRepository(RetrofitFakeInstance)
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private val experienciaRepository=ExperienciaRepository(RetrofitFakeInstance.apiFake)
    private lateinit var adaptadorListado: AdaptadorCompra
    private lateinit var listaExperiencias:MutableList<ExperienciaCompleta>
    private lateinit var listaSesiones: MutableList<SesionConCompra>
    private lateinit var homeViewModel: HomeViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Home"
        //Instancia viewmodel
        val factory = HomeViewModelFactory(compraRepository, cajaChicaRepository,experienciaRepository)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        instancias()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        detectarScroll()
        //Instancias Click
        binding.btnEnviarComentarios.setOnClickListener(this)
        binding.selectFecha.setOnClickListener(this)
        binding.tvHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        binding.btnScrollSubir.setOnClickListener(this)
        binding.btnEnviarCaja.setOnClickListener(this)
        listaExperiencias= mutableListOf()
        //ViewModel
        observersViewModel()

        //Adaptador recycler
        adaptadorListado = AdaptadorCompra(requireContext(), mutableListOf(),listaExperiencias) { sesionConCompra ->
            val bundle=Bundle()
            bundle.putSerializable("sesionConCompra",sesionConCompra)
            val dialog = OpcionesDialogFragment.newInstance(sesionConCompra)
            dialog.show(parentFragmentManager, "OpcionesDialog")
        }
        binding.recyclerSesionesHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerSesionesHome.adapter = adaptadorListado

        //Animación para el recycler al recargar datos
        val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fade_in)
        binding.recyclerSesionesHome.layoutAnimation = controller

        //Activamos scroll de inicio en el recycler
        binding.recyclerSesionesHome.isNestedScrollingEnabled = true
        binding.recyclerSesionesHome.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        configurarDeteccionScroll()
        //Detecta el refresh y refreca los datos de la vista
        binding.swipeRefresh.setOnRefreshListener {
            refrescarTodaLaPantalla()
        }

        binding.nestedScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            // Si está en la parte superior, permitimos el refresco
            binding.swipeRefresh.isEnabled = scrollY == 0
        }

    }
    //Observers del viewmodel
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observersViewModel(){
        //Actualiza la lista de sesiones del recycler con una animación
        homeViewModel.sesiones.observe(viewLifecycleOwner) { sesiones ->
            listaSesiones = sesiones.toMutableList()
            adaptadorListado.actualizarLista(listaSesiones)
            binding.recyclerSesionesHome.scheduleLayoutAnimation()
            binding.recyclerSesionesHome.visibility = View.VISIBLE
        }
        //Actualiza la lista de experiences
        homeViewModel.experiencias.observe(viewLifecycleOwner){ experiencias ->
            listaExperiencias=experiencias.toMutableList()
            adaptadorListado.actualizarExperiencias(listaExperiencias)
        }
        //Actualiza los pagos de caja chica y construye su tabla
        homeViewModel.pagos.observe(viewLifecycleOwner) { pagos ->
            TablaBuilder.construirTablaPagos(binding.tablaCajaChica, pagos, requireContext())
            binding.tablaCajaChica.visibility = View.VISIBLE
        }
        //Crea la tabla de comentarios con sus datos
        homeViewModel.comentarios.observe(viewLifecycleOwner){ comentarios ->
            Log.d("OBSERVER", "Actualizando comentarios: ${comentarios.size}")
            TablaBuilder.construirTablaComentarios(binding.tablaComentarios, comentarios, requireContext())
        }
        //Actualiza la fecha de la vista y recarga los nuevos datos
        homeViewModel.fechaActual.observe(viewLifecycleOwner) { fecha ->
            val dia = fecha.dayOfMonth
            val mes = fecha.month.value
            val fechaFormateada = "$dia/$mes"
            binding.tvFecha.text = fechaFormateada
            cargarDatosSesionesHoy()

            val token = getTokenFromSharedPreferences()
            if (token != null) {
                homeViewModel.obtenerComentarios(token, fecha)
                homeViewModel.obtenerExperiencias(token)
            }
        }
    }

    /**
     * Obtiene el token de shared prefrences si no es nulo y si la fecha actual no es nula.
     * Después carga datos de vista a través del viewmodel con el token y la fecha
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarDatosSesionesHoy() {
        binding.tablaCajaChica.visibility = View.GONE
        getTokenFromSharedPreferences()?.let { token ->
            homeViewModel.fechaActual.value?.let { fecha ->
                homeViewModel.cargarDatosDesdeJsonServer(token, fecha)
            }
        } ?: Toast.makeText(requireContext(), "No se pudo recuperar el token", Toast.LENGTH_SHORT).show()
    }

    /**
     * Comprobamos si el campo del edit es nulo o vacio,si no obtenemos el usuario de shared preferences y
     * el token y lo registramos en la api con todos los datos que se necesitan.(Esto debo moverlo al viewmodel)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviarComentarios() {
        if (binding.editComentario.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Escribe algo antes de enviar", Toast.LENGTH_SHORT).show()
            return
        }
        val prefs = requireContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val nombreUsuario = prefs.getString("nombre_usuario", "Usuario")
        val token = prefs.getString("auth_token", null)

        if (token == null) {
            Log.e("ENVIAR_COMENTARIO", "Token no encontrado en SharedPreferences")
            Toast.makeText(requireContext(), "No se puede enviar el comentario: sesión no iniciada", Toast.LENGTH_LONG).show()
            return
        }
        val horaActual = LocalDateTime.now().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        val fecha = homeViewModel.fechaActual.value ?: LocalDate.now()
        val fechaFormateada = "$fecha $horaActual"

        //Corrutina para registrar comentario en api a traves de comprarepository.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val comentario = Comentario(
                    id = UUID.randomUUID().toString(),
                    tipo = binding.spinnerComentarios.selectedItem.toString(),
                    fecha = fechaFormateada,
                    nombreUsuario = nombreUsuario ?: "Usuario",
                    descripcion = binding.editComentario.text.toString()
                )

                val success = compraRepository.registrarComentarioAPI("Bearer $token", comentario)

                Log.e("ENVIO_COMENTARIO", success.toString())
                //Comprobamos si es exitoso,vaciamos campos,notificamos y actualizamos fecha y comentarios
                //a traves de viewmodel
                if (success) {

                    Toast.makeText(requireContext(), "Comentario añadido correctamente", Toast.LENGTH_LONG).show()
                    binding.editComentario.text.clear()
                    kotlinx.coroutines.delay(500)
                    Log.d("RELOAD", "Volviendo a cargar comentarios")
                    // Fuerza el LiveData para que vuelva a emitir y así se llame observer
                    homeViewModel.actualizarFecha(homeViewModel.fechaActual.value ?: LocalDate.now())

                    homeViewModel.obtenerComentarios(token, fecha)
                } else {
                    Toast.makeText(requireContext(), "Error al enviar comentario", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("ENVIAR_COMENTARIO", "Error: ${e.message}")
            }
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
    //Metodos on click de la vista
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            binding.selectFecha.id->{
                mostrarDialogoFecha()
            }
            binding.tvHoy.id -> {
                homeViewModel.actualizarFecha(LocalDate.now())
            }

            binding.tvFlechaIzquierdaHoy.id -> {
                homeViewModel.fechaActual.value?.let {
                    homeViewModel.actualizarFecha(it.minusDays(1))
                }
            }

            binding.tvFlechaDerechaHoy.id -> {
                homeViewModel.fechaActual.value?.let {
                    homeViewModel.actualizarFecha(it.plusDays(1))
                }
            }

            binding.btnScrollSubir.id->{
                binding.nestedScroll.smoothScrollTo(0, 0)
            }
            binding.btnEnviarCaja.id->{
                agregarDineroCajaChica()
            }
            binding.btnEnviarComentarios.id->{
                enviarComentarios()
            }
        }
    }

    /**
     * Obtenemos datos para registrar pago en efectivo y lo registramos a traves del viewmodel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun agregarDineroCajaChica() {
        val concepto = binding.editCopcepto.text.toString().trim()
        val cantidad = binding.editCantidad.text.toString().trim()
        val token=getTokenFromSharedPreferences()
        homeViewModel.agregarPagoCajaChica(token!!,concepto, cantidad, onSuccess = {
                binding.editCopcepto.text.clear()
                binding.editCantidad.text.clear()
                Toast.makeText(requireContext(), "Pago añadido correctamente", Toast.LENGTH_SHORT).show()
            },
            onError = { mensaje ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Mostramos dialogo para seleccionar dia al que queremos ir.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarDialogoFecha() {
        val fechaHoy = LocalDate.now()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val nuevaFecha = LocalDate.of(year, month + 1, dayOfMonth)
                homeViewModel.actualizarFecha(nuevaFecha)
            },
            fechaHoy.year,
            fechaHoy.monthValue - 1,
            fechaHoy.dayOfMonth
        )
        datePicker.show()
    }

    @SuppressLint("SetTextI18n")
    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        Log.e("TOKEN DEVUELTO","TOKEN : $token")
        return token?.let { "Bearer $it" }
    }
    //Método para cambiar de fragmento
    private fun cambiarFragmento(fragment:Fragment){
        val transaction=parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_principal,fragment)
            .addToBackStack(null)
        transaction.commit()
    }
    //Metodo para detectar scroll y mostrar boton de subir.
    private fun detectarScroll(){
        binding.nestedScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 300) {
                binding.btnScrollSubir.visibility = View.VISIBLE
            } else {
                binding.btnScrollSubir.visibility = View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configurarDeteccionScroll() {
        // Siempre bloqueamos el scroll del NestedScroll al principio
        binding.nestedScroll.requestDisallowInterceptTouchEvent(true)

        // Detectamos toques en el RecyclerView
        binding.recyclerSesionesHome.setOnTouchListener { _, _ ->
            val posicionCajaChicaY = binding.tablaCajaChica.y
            val scrollYActual = binding.nestedScroll.scrollY
            val alturaVisible = binding.nestedScroll.height

            val yaSeVeCajaChica = scrollYActual + alturaVisible >= posicionCajaChicaY
            val recyclerYaNoPuedeScroll = !binding.recyclerSesionesHome.canScrollVertically(1)

            if (recyclerYaNoPuedeScroll && yaSeVeCajaChica) {
                // Ya terminó el scroll del RecyclerView y se ve la zona de caja chica
                binding.nestedScroll.requestDisallowInterceptTouchEvent(false)
                Log.d("SCROLL", "✔ Se permite scroll en NestedScrollView")
            } else {
                // Aún no ha llegado a la zona → bloqueamos scroll del Nested
                binding.nestedScroll.requestDisallowInterceptTouchEvent(true)
                Log.d("SCROLL", " Bloqueamos scroll del NestedScrollView")
            }

            false
        }

        // Este listener solo gestiona el botón flotante
        binding.nestedScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.btnScrollSubir.visibility = if (scrollY > 200) View.VISIBLE else View.GONE
        }
    }

    /**
     * Muestra animación refresg hasta que actualizamos la fecha y se cargan los datos
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun refrescarTodaLaPantalla() {
        binding.swipeRefresh.isRefreshing = true

        homeViewModel.fechaActual.value?.let {
            homeViewModel.actualizarFecha(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(1000)
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show()
        }
    }

}