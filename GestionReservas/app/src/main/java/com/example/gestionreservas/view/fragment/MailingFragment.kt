package com.example.gestionreservas.view.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.databinding.FragmentMailingBinding
import com.example.gestionreservas.repository.MailingRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.gestionreservas.R
import com.example.gestionreservas.model.CorreoItem

@RequiresApi(Build.VERSION_CODES.O)
class MailingFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentMailingBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1234

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMailingBinding.inflate(inflater, container, false)

        instancias()
        return binding.root
    }
    private fun instancias(){
        binding.btnLogin.setOnClickListener(this)
        configurarGoogleSignIn()
    }

    // Configura Google Sign-In con el scope de Gmail API y cliente Web
    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
            .requestServerAuthCode(getString(R.string.default_web_client_id), true)
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    // Funciones on click del fragment
    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnLogin.id->{
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    // Procesa el resultado del login y consulta Gmail API
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            if (task.isSuccessful) {
                val account = task.result
                val authCode = account?.serverAuthCode
                Log.d("MailingFragment", "AuthCode: $authCode")

                lifecycleScope.launch {
                    try {
                        val token = authCode?.let { MailingRepository.getAccessToken(it) }
                        Log.d("MailingFragment", "AccessToken: $token")

                        val mensajes = token?.let { MailingRepository.getMensajes(it) }

                        mensajes?.messages?.forEach {
                            Log.d("MailingFragment", "ID: ${it.id}, Thread: ${it.threadId}")
                        }
                        val listaCorreos = mutableListOf<CorreoItem>()


                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Login y consulta OK", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Log.e("MailingFragment", "Error general: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } else {
                Log.e("MailingFragment", "Fallo en login: ${task.exception}")
                Toast.makeText(requireContext(), "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
