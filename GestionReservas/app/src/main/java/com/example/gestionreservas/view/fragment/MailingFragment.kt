package com.example.gestionreservas.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
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
import com.example.gestionreservas.databinding.FragmentMailingBinding
import com.example.gestionreservas.model.CorreoItem
import com.example.gestionreservas.models.entity.TokenResponse
import com.example.gestionreservas.repository.MailingRepository
import com.example.gestionreservas.view.adapter.AdaptadorCorreo
import com.example.gestionreservas.viewModel.listado.Mailing.MailingViewModel
import com.example.gestionreservas.viewModel.listado.Mailing.MailingViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class MailingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentMailingBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1234
    private var nextPageToken: String? = null
    private var isLoading = false
    private lateinit var token: TokenResponse
    private val listaTotalCorreos = mutableListOf<CorreoItem>()
    private lateinit var adaptadorCorreo: AdaptadorCorreo
    private lateinit var mailingViewModel: MailingViewModel

    /**
     * Configuracion del menu para poder filtrar correos al iniciar sesión en tiempo real
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_busqueda, menu)

        val cuenta = GoogleSignIn.getLastSignedInAccount(requireContext())
        val item = menu.findItem(R.id.action_search)
        item.isVisible =
            cuenta != null && ::binding.isInitialized && binding.recyclerCorreos.visibility == View.VISIBLE

        // Recupera tu SearchView custom
        val searchView = item.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                adaptadorCorreo.filtrar(newText.orEmpty())
                return true
            }
        })
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?

    ): View {
        binding = FragmentMailingBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Mailing"
        val factory = MailingViewModelFactory(MailingRepository)
        mailingViewModel = ViewModelProvider(this, factory).get(MailingViewModel::class.java)

        instancias()
        return binding.root
    }


    //Funcion para instanciar listeners,adaptadores,funciones de inicio
    private fun instancias() {

        binding.recyclerCorreos.visibility = View.GONE
        //Listeners
        binding.btnLogin.setOnClickListener(this)
        binding.btnCerrarSesion.setOnClickListener(this)

        //Llamada a iniciar sesión de gmail
        configurarGoogleSignIn()

        //Configuración adaptador
        adaptadorCorreo = irFragmentDetalles()
        binding.recyclerCorreos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCorreos.adapter = adaptadorCorreo

        // Comprobar si ya hay cuenta conectada
        GoogleSignIn.getLastSignedInAccount(requireContext())?.let {
            inicializarCorreoConCuentaGuardada()
        }
        //Llamada a funciones de refrescar y activar animación
        refrescarCorreos()
        activarAnimacionStretch()

        observersViewModel()
    }
    private fun observersViewModel(){
        mailingViewModel.sesionCerrada.observe(viewLifecycleOwner) { cerrada ->
            if (cerrada) {
                cerrarSesion()
                mailingViewModel.resetearEstadoSesionCerrada()
            }
        }
    }
    /**
     * Detecta si se hace click en el item del adaptador y le pasa por argumentos al siguiente fragment
     * los datos completos del mensaje para poder mostrarlos en el.
     */
    private fun irFragmentDetalles(): AdaptadorCorreo {
        val adapter = AdaptadorCorreo(requireContext(), listaTotalCorreos) { mensaje: CorreoItem ->
            lifecycleScope.launch {
                try {

                    val mensajeDetallado = MailingRepository.getMensajeDetalle(token, mensaje.id)
                    val mensajeDepurado =
                        MailingRepository.depurarMensajeParaObtenerDetalles(mensajeDetallado)

                    val fragment = DetallesCorreoFragment()
                    val bundle = Bundle()
                    bundle.putSerializable("mensaje", mensajeDepurado)
                    fragment.arguments = bundle
                    val transacion = parentFragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_principal, fragment)
                    transacion.commit()

                } catch (e: Exception) {
                    Log.e("MailingFragment", "Error al obtener los detalles completos del mensaje ${e.message}")
                }
            }

        }
        return adapter
    }

    //Configuracion del inicio de sesion de gmail con nuestro id de cliente y la ruta a la api
    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
            .requestServerAuthCode(
                getString(com.example.gestionreservas.R.string.default_web_client_id),
                true
            )
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    //Metodos onclick
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnLogin.id -> {
                startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }

            binding.btnCerrarSesion.id -> {
               cerrarSesion()
            }
        }
    }

    /**
     * Inicio de sesion de GMAIL,si es exitoso obtenemos correos y sus datos y los cargamos al adaptador a traves
     * de mostrarListaCorreos()
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                task.result?.serverAuthCode?.let { authCode ->
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerCorreos.visibility = View.GONE
                    lifecycleScope.launch {
                        try {
                            val listaCorreos = MailingRepository.loginYObtenerCorreos(authCode, requireContext())

                            // Obtenemos el token después de haberlo guardado
                            token = obtenerTokenGuardado()
                            nextPageToken = null
                            mostrarListaCorreos(listaCorreos)
                            binding.btnLogin.visibility = View.GONE
                            binding.btnCerrarSesion.visibility = View.VISIBLE
                            binding.recyclerCorreos.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Login y correos OK",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.e("MailingFragment", "Error login: ${e.message}")
                            Toast.makeText(
                                requireContext(),
                                "Error,intente iniciar sesión: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnLogin.visibility = View.VISIBLE
                            binding.btnCerrarSesion.visibility = View.GONE
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Error al iniciar sesión", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Recibe la nueva lista y se la añade al adaptador para mostrarla,Añadimos animación al recycler para
     * opcion de refresh.
     */
    private fun mostrarListaCorreos(nuevaLista: List<CorreoItem>) {

        listaTotalCorreos.clear()
        listaTotalCorreos.addAll(nuevaLista)
        adaptadorCorreo.actualizarListaCompleta(nuevaLista)
        adaptadorCorreo.notifyDataSetChanged()

        val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_down)
        binding.recyclerCorreos.layoutAnimation = controller
        binding.recyclerCorreos.scheduleLayoutAnimation()
        Log.d("DEBUG", "Mostrando lista con ${nuevaLista.size} correos")
    }

    /**
     * Llama al repositorio y obtiene los nuevos mensajes añadidos a la lista para despues mostrarlos en
     * el fragment.
     */
    private fun cargarPaginaSiguiente() {
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerCorreos.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val (nuevosCorreos, nuevaToken) = MailingRepository.obtenerMensajesPagina(token, pageToken = null)
                nextPageToken = nuevaToken
                mostrarListaCorreos(nuevosCorreos)
            } catch (e: Exception) {
                Log.e("MailingFragment", "Error en paginación: ${e.message}")
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }


    /**Metodo para obtener token gmail guardado de shared preferences,devuelve un token response con
     * la expiracion,token de acceso  y el resfresh del token
     */
    private fun obtenerTokenGuardado(): TokenResponse {
        val prefs = requireContext().getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        val access = prefs.getString("access_token", null) ?: throw Exception("No hay token")
        val refresh = prefs.getString("refresh_token", null)
        return TokenResponse(
            access,
            3600,
            refresh,
            "Bearer",
            "https://www.googleapis.com/auth/gmail.readonly"
        )
    }

    /**
     * Metodo que inicia sesion directamente si hay cuenta guardada y muestra los correos,se oculta
     * el boton de login ya que no es necesario.
     */
    private fun inicializarCorreoConCuentaGuardada(showProgressBar: Boolean = true) {
        binding.btnLogin.visibility = View.GONE
        binding.btnCerrarSesion.visibility = View.VISIBLE
        if (showProgressBar) binding.progressBar.visibility = View.VISIBLE
        binding.recyclerCorreos.visibility = View.VISIBLE
        requireActivity().invalidateOptionsMenu()
        lifecycleScope.launch {
            try {
                obtenerCorreosAlHacerScroll()
            } catch (e: Exception) {
                Log.e("MailingFragment", "Error cargando correos: ${e.message}")
                binding.btnLogin.visibility = View.VISIBLE
                binding.recyclerCorreos.visibility = View.GONE
                binding.btnCerrarSesion.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Fallo cargando correos", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                if (showProgressBar) binding.progressBar.visibility = View.GONE
                binding.recyclerCorreos.visibility = View.VISIBLE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    /**
     * Detecta si hacemos scroll en el listener para llamar a obtener mensajes página y mostrarlos.
     */
    private suspend fun obtenerCorreosAlHacerScroll() {
        //Obtenemos token  y cargamos los correos
        token = obtenerTokenGuardado()
        val (lista, tokenInicial) = MailingRepository.obtenerMensajesPagina(token)
        Log.d("DEBUG", "Correos obtenidos en scroll: ${lista.size}")
        nextPageToken = tokenInicial
        mostrarListaCorreos(lista)
        //Detectamos si el usuario hace scroll,si lo hace se carga la siguiente pagina de correos
        binding.recyclerCorreos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!isLoading && nextPageToken != null && lastVisibleItem >= totalItemCount - 2) {
                    cargarPaginaSiguiente()
                }
            }
        })
    }

    /**
     * Cierra sesion del correo,vacia las listas,notifica al adaptador y oculta el boton cerrar sesion y muestra el de login
     * Refresca el menú tambien para que no aparezca la opción de buscar.
     */
    private fun cerrarSesion() {
        mailingViewModel.logout(requireContext())
        listaTotalCorreos.clear()
            adaptadorCorreo.notifyDataSetChanged()
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnCerrarSesion.visibility = View.GONE
            binding.recyclerCorreos.visibility = View.GONE
            nextPageToken = null
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
            requireActivity().invalidateOptionsMenu()

    }

    /**
     * Detecta si se refresco elrecycler view tirando hacia debajo y se cargan los nuevos correos
     */
    private fun refrescarCorreos(){
        binding.swipeRefresh.setOnRefreshListener {
            binding.recyclerCorreos.visibility = View.GONE
           inicializarCorreoConCuentaGuardada(showProgressBar = false)
        }
    }

    /**
     * Función para agregar animacion al refrescar los mensajes tirando hacia abajo como en las apps
     * de correo electrónico
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun activarAnimacionStretch() {
        val recyclerView = binding.recyclerCorreos
        val swipeRefresh = binding.swipeRefresh

        var startY = 0f
        var isDragging = false
        var isRefreshing = false
        val refreshThreshold = 120f

        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        val firstVisibleItem = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: -1

        recyclerView.setOnTouchListener { v, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    isDragging = false
                    isRefreshing = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = event.y - startY
                    // SOLO cuando estás arriba del todo (primer item visible == 0)
                    val canScrollUp = recyclerView.canScrollVertically(-1)
                    if (dy > 0 && !canScrollUp && firstVisibleItem == 0 && !swipeRefresh.isRefreshing) {
                        isDragging = true
                        for (i in 0 until recyclerView.childCount) {
                            val child = recyclerView.getChildAt(i)
                            child.translationY = dy / 2
                        }
                        if (dy > refreshThreshold) {
                            isRefreshing = true
                        }
                        return@setOnTouchListener true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        for (i in 0 until recyclerView.childCount) {
                            val child = recyclerView.getChildAt(i)
                            child.animate().translationY(0f).setDuration(300).start()
                        }
                        if (isRefreshing) {
                            // Refrescar sólo si arrastraste suficiente y desde arriba
                            binding.recyclerCorreos.visibility = View.GONE
                            inicializarCorreoConCuentaGuardada(showProgressBar = false)
                        }
                        isDragging = false
                        isRefreshing = false
                    }
                }
            }
            false
        }
    }


}
