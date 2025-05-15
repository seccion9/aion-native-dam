package com.example.gestionreservas.view.activities


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.launch
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gestionreservas.models.repository.AuthRepository
import com.example.gestionreservas.databinding.ActivityMainBinding
import com.example.gestionreservas.models.entity.LoginRequest
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        instancias()
    }
    private fun instancias(){
        //Aqui van las instancias de nuestra activity
        binding.btnLogin?.setOnClickListener(this)
        binding.btnReset?.setOnClickListener(this)
        binding.editCorreo.setText("admin@aether.com")
        binding.editPass.setText("1234")

        crearCanalNotificaciones(applicationContext)

    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnLogin?.id ->{
                if(binding.editCorreo.text.isEmpty() || binding.editPass.text.isEmpty()){
                    Toast.makeText(applicationContext,"Los campos deben estar rellenos",Toast.LENGTH_SHORT).show()
                }else{
                    /*
                    Antes de pasar aplicacion a pruebas hacer funcion de login sacando los test de los edit
                    email y password
                     */
                    //val email = binding.editCorreo.text.toString()
                    //val password = binding.editPass.text.toString()
                    hacerLogin()
                }
            }
            binding.btnReset?.id ->{
                binding.editCorreo.text.clear()
                binding.editPass.text.clear()
            }
        }
    }
    //Metodo que se encarga de hacer el login del usuario con la API

    // Funcion que guarda el token en local
    private fun saveTokenToSharedPreferences(token: String) {
        val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.apply()
    }

    /*Credenciales validas para obtener token,cambiar por llamar a funcion login comentada para insertar datos en edittest
     */
    fun hacerLogin() {
        val intent: Intent = Intent(applicationContext, ReservasActivity::class.java)
        //Credenciales validas para conexion con API
        val email = "75d3be3a6b0dc3c6770273c56c6f4bdb5cb8b426be9ac4044a0b377@aether.com"
        val password = "122f1bb7be0436316311bdd20367102e79ce6d954ef8f44bc95ae"

        //Metodo para obtener respuesta de nuestra API a nuestra autenticacion,nos devuelve un token en
        //formato string
        val authRepository = AuthRepository(RetrofitInstance.api)
        lifecycleScope.launch {
            try{

                val response = authRepository.login(email, password)
                if (response.isSuccessful && false) {
                    val token = response.body()
                    saveTokenToSharedPreferences(token.toString())

                    // Sacamos el intent y el start activity fuera del contexto para evitar errores
                    withContext(Dispatchers.Main) {
                        delay(2000)
                        startActivity(Intent(applicationContext, ReservasActivity::class.java))
                        finish()
                    }
                }else{
                    loginApiFake()
                }
            }catch (e: Exception) {
                Log.e("MainActivity", "Error en la API real: ${e.message}")

            }
        }
    }
    private suspend fun loginApiFake(){
        val response=RetrofitFakeInstance.apiFake.autenticacion(LoginRequest(
            binding.editCorreo.text.toString(),
            binding.editPass.text.toString()
        ))
        if(response.isSuccessful){
            val loginResponse = response.body() ?: return          // LoginResponse
            val token      = loginResponse.token                // "fake-token-abc123"
            saveTokenToSharedPreferences(token)
            Log.e("login Activity","Token : ${token}")
            // Sacamos el intent y el start activity fuera del contexto para evitar errores
            withContext(Dispatchers.Main) {
                delay(2000)
                startActivity(Intent(applicationContext, ReservasActivity::class.java))
                finish()
            }
        }else{
            Log.e("LoginFake", "HTTP ${response.code()}  ${response.errorBody()?.string()}")
        }
    }
    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "canal_reservas",
                "Reservas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de nuevas reservas"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }
}




