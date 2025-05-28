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
import com.example.gestionreservas.models.entity.SesionConCompra
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.utils.TablaBuilder
import com.example.gestionreservas.view.adapter.AdaptadorCompra
import com.example.gestionreservas.viewModel.listado.Home.HomeViewModel
import com.example.gestionreservas.viewModel.listado.Home.HomeViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID


class HomeFragment: Fragment(),OnClickListener {
    private lateinit var binding: FragmentHomeBinding
    private val cajaChicaRepository = CajaChicaRepository(RetrofitFakeInstance)
    private val compraRepository = CompraRepository(RetrofitFakeInstance.apiFake)
    private lateinit var adaptadorListado: AdaptadorCompra
    private lateinit var listaSesiones: MutableList<SesionConCompra>
    private lateinit var homeViewModel: HomeViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Home"

        val factory = HomeViewModelFactory(compraRepository, cajaChicaRepository)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        instancias()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        detectarScroll()
        detectarScrollRecycler()
        //Instancias Click
        binding.btnEnviarComentarios.setOnClickListener(this)
        binding.selectFecha.setOnClickListener(this)
        binding.tvHoy.setOnClickListener(this)
        binding.tvFlechaIzquierdaHoy.setOnClickListener(this)
        binding.tvFlechaDerechaHoy.setOnClickListener(this)
        binding.btnScrollSubir.setOnClickListener(this)
        binding.btnEnviarCaja.setOnClickListener(this)

        //ViewModel
        observersViewModel()
        //Adaptador recycler

        adaptadorListado = AdaptadorCompra(requireContext(), mutableListOf())

        binding.recyclerSesionesHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.recyclerSesionesHome.adapter = adaptadorListado

        //Metodos para cargar datos

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observersViewModel(){
        homeViewModel.sesiones.observe(viewLifecycleOwner) { sesiones ->
            listaSesiones = sesiones.toMutableList()
            adaptadorListado.actualizarLista(listaSesiones)
            binding.recyclerSesionesHome.visibility = View.VISIBLE
        }

        homeViewModel.pagos.observe(viewLifecycleOwner) { pagos ->
            TablaBuilder.construirTablaPagos(binding.tablaCajaChica, pagos, requireContext())
            binding.tablaCajaChica.visibility = View.VISIBLE
        }
        homeViewModel.comentarios.observe(viewLifecycleOwner){ comentarios ->
            Log.d("OBSERVER", "Actualizando comentarios: ${comentarios.size}")
            TablaBuilder.construirTablaComentarios(binding.tablaComentarios, comentarios, requireContext())
        }
        homeViewModel.fechaActual.observe(viewLifecycleOwner) { fecha ->
            val dia = fecha.dayOfMonth
            val mes = fecha.month.value
            val fechaFormateada = "$dia/$mes"
            binding.tvFecha.text = fechaFormateada
            cargarDatosSesionesHoy()

            val token = getTokenFromSharedPreferences()
            if (token != null) {
                homeViewModel.obtenerComentarios(token, fecha)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cargarDatosSesionesHoy() {
        binding.tablaCajaChica.visibility = View.GONE
        getTokenFromSharedPreferences()?.let { token ->
            homeViewModel.fechaActual.value?.let { fecha ->
                homeViewModel.cargarDatosDesdeJsonServer(token, fecha)
            }
        } ?: Toast.makeText(requireContext(), "No se pudo recuperar el token", Toast.LENGTH_SHORT).show()
    }

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
    private fun detectarScrollRecycler(){
        binding.recyclerSesionesHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("SCROLL", "Scroll interno RecyclerView: dy = $dy")

            }
        })
    }


}