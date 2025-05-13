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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private fun instancias() {
        binding.btnLogin.setOnClickListener(this)
        configurarGoogleSignIn()

        val cuenta = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (cuenta != null) {
            obtenerCorreosDirectamente()
        }
    }

    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
            .requestServerAuthCode(getString(R.string.default_web_client_id), true)
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.btnLogin.id) {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val authCode = task.result?.serverAuthCode
                Log.d("MailingFragment", "AuthCode: $authCode")

                lifecycleScope.launch {
                    try {
                        val tokenResponse = authCode?.let { MailingRepository.getAccessToken(it) }

                        if (tokenResponse != null) {
                            guardarTokens(requireContext(), tokenResponse.access_token, tokenResponse.refresh_token)

                            val lista = MailingRepository.obtenerMensajes(tokenResponse)

                            withContext(Dispatchers.Main) {
                                val adaptador = AdaptadorCorreo(requireContext(), lista)
                                binding.recyclerCorreos.layoutManager = LinearLayoutManager(requireContext())
                                binding.recyclerCorreos.adapter = adaptador
                                Toast.makeText(requireContext(), "Login y consulta OK", Toast.LENGTH_SHORT).show()
                            }
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

    private fun obtenerCorreosDirectamente() {
        val accessToken = obtenerAccessToken(requireContext())
        val refreshToken = obtenerRefreshToken(requireContext())

        if (accessToken == null) {
            Log.e("MailingFragment", "No se encontró access token")
            return
        }

        val token = TokenResponse(
            access_token = accessToken,
            refresh_token = refreshToken,
            expires_in = 3600,
            token_type = "Bearer",
            scope = "https://www.googleapis.com/auth/gmail.readonly"
        )

        lifecycleScope.launch {
            try {
                val lista = MailingRepository.obtenerMensajes(token)
                withContext(Dispatchers.Main) {
                    val adaptador = AdaptadorCorreo(requireContext(), lista)
                    binding.recyclerCorreos.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerCorreos.adapter = adaptador
                }
            } catch (e: Exception) {
                Log.e("MailingFragment", "Error al obtener correos automáticamente: ${e.message}")
            }
        }
    }

    private fun guardarTokens(context: Context, accessToken: String, refreshToken: String?) {
        val prefs = context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    private fun obtenerAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        return prefs.getString("access_token", null)
    }

    private fun obtenerRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        return prefs.getString("refresh_token", null)
    }
}
