package com.example.gestionreservas.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentMailingBinding
import com.example.gestionreservas.model.CorreoItem
import com.example.gestionreservas.models.entity.TokenResponse
import com.example.gestionreservas.repository.MailingRepository
import com.example.gestionreservas.view.adapter.AdaptadorCorreo
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMailingBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Mailing"
        instancias()
        return binding.root
    }


    //Funcion para instanciar listeners,adaptadores,funciones de inicio
    private fun instancias() {
        binding.btnLogin.setOnClickListener(this)
        binding.btnCerrarSesion.setOnClickListener(this)

        configurarGoogleSignIn()

        adaptadorCorreo = AdaptadorCorreo(requireContext(), listaTotalCorreos)
        binding.recyclerCorreos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCorreos.adapter = adaptadorCorreo

        // Comprobar si ya hay cuenta conectada
        GoogleSignIn.getLastSignedInAccount(requireContext())?.let {
            inicializarCorreoConCuentaGuardada()
        }
    }


    //Configuracion del inicio de sesion de gmail con nuestro id de cliente y la ruta a la api
    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
            .requestServerAuthCode(getString(com.example.gestionreservas.R.string.default_web_client_id), true)
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    //Metodos onclick
    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnLogin.id->{
                startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            }
            binding.btnCerrarSesion.id->{
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
                    lifecycleScope.launch {
                        try {
                            // Guarda tokens en SharedPreferences
                            MailingRepository.loginYObtenerCorreos(authCode, requireContext())

                            inicializarCorreoConCuentaGuardada()
                            Toast.makeText(requireContext(), "Login y correos OK", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("MailingFragment", "Error login: ${e.message}")
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Recibe la nueva lista y se la añade al adaptador para mostrarla
     */
    private fun mostrarListaCorreos(nuevaLista: List<CorreoItem>) {

        listaTotalCorreos.clear()
        listaTotalCorreos.addAll(nuevaLista)
        adaptadorCorreo.notifyDataSetChanged()
        Log.d("DEBUG", "Mostrando lista con ${nuevaLista.size} correos")
    }

    /**
     * Llama al repositorio y obtiene los nuevos mensajes añadidos a la lista para despues mostrarlos en
     * el fragment.
     */
    private fun cargarPaginaSiguiente() {
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val (nuevosCorreos, nuevaToken) = MailingRepository.obtenerMensajesPagina(token, nextPageToken)
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
        return TokenResponse(access, 3600, refresh, "Bearer", "https://www.googleapis.com/auth/gmail.readonly")
    }

    /**
     * Metodo que inicia sesion directamente si hay cuenta guardada y muestra los correos,se oculta
     * el boton de login ya que no es necesario.
     */
    private fun inicializarCorreoConCuentaGuardada() {
        binding.btnLogin.visibility = View.GONE
        binding.btnCerrarSesion.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                obtenerCorreosAlHacerScroll()
            } catch (e: Exception) {
                Log.e("MailingFragment", "Error cargando correos: ${e.message}")
                binding.btnLogin.visibility = View.VISIBLE
                binding.btnCerrarSesion.visibility = View.GONE
                Toast.makeText(requireContext(), "Fallo cargando correos", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    private suspend fun obtenerCorreosAlHacerScroll(){
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
    private fun cerrarSesion() {
        googleSignInClient.signOut().addOnCompleteListener {
            // Si quieres impedir que vuelva a iniciar sesión automática-
            // mente en este dispositivo, usa también revokeAccess():
            googleSignInClient.revokeAccess().addOnCompleteListener {
                Log.d("MailingFragment", "Cuenta desconectada")
            }
        }

        requireContext().getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        listaTotalCorreos.clear()
        adaptadorCorreo.notifyDataSetChanged()
        binding.btnLogin.visibility = View.VISIBLE
        nextPageToken = null
        Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }
}
