package com.example.gestionreservas.view.activities


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.launch
import android.view.View
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
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
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), View.OnClickListener,OnCheckedChangeListener {
    private lateinit var binding: ActivityMainBinding
    private var recordarCuenta=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        val mantenerSesion = prefs.getBoolean("mantener_sesion", false)

        if (token != null && mantenerSesion) {
            startActivity(Intent(this, ReservasActivity::class.java))
            finish()
            return
        }
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        instancias()
    }
    private fun instancias(){
        //Aqui van las instancias de nuestra activity
        binding.btnLogin.setOnClickListener(this)
        binding.btnReset.setOnClickListener(this)
        binding.checkboxCuenta.setOnCheckedChangeListener(this)

        binding.editCorreo.setText("admin@aether.com")
        binding.editPass.setText("1234")


    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnLogin.id ->{
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
            binding.btnReset.id ->{
                binding.editCorreo.text.clear()
                binding.editPass.text.clear()
            }
        }
    }
    //Metodo que se encarga de hacer el login del usuario con la API

    // Funcion que guarda el token y preferencias en local
    private fun saveTokenToSharedPreferences(token: String, nombre: String) {
        val sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("auth_token", token)
            .putString("nombre_usuario", nombre)
            .putBoolean("mantener_sesion", recordarCuenta)
            .commit()
    }

    /*Credenciales validas para obtener token,cambiar por llamar a funcion login comentada para insertar datos en edittest
     */
    fun hacerLogin() {
        val intent: Intent = Intent(this, ReservasActivity::class.java)
        //Credenciales validas para conexion con API
        val email = ""
        val password = ""

        //Metodo para obtener respuesta de nuestra API a nuestra autenticacion,nos devuelve un token en
        //formato string
        val authRepository = AuthRepository(RetrofitInstance.api)
        lifecycleScope.launch {
            try{
                    loginApiFake()
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
            val prefs = getSharedPreferences("ajustes", MODE_PRIVATE)
            prefs.edit().putBoolean("notificaciones_activadas", true).apply()
            val loginResponse = response.body() ?: return
            val token = loginResponse.token
            val nombre = loginResponse.nombreUsuario
            saveTokenToSharedPreferences(token, nombre)
            Log.e("LoginResponse", "Código: ${response.code()}, Body: ${response.body()}, Error: ${response.errorBody()?.string()}")

            Log.e("TOKEN GUARDADO","TOKEN: $token")
            withContext(Dispatchers.Main) {
                delay(2000)
                startActivity(Intent(applicationContext, ReservasActivity::class.java))
                finish()
            }
        }else{
            Log.e("LoginFake", "HTTP ${response.code()}  ${response.errorBody()?.string()}")
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(binding.checkboxCuenta.isChecked){
            recordarCuenta=isChecked
        }
    }
}




