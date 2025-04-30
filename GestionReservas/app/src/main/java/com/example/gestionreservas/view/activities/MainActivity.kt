package com.example.gestionreservas.view.activities


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionreservas.models.repository.AuthRepository
import com.example.gestionreservas.databinding.ActivityMainBinding
import com.example.gestionreservas.network.RetrofitInstance
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
        editor.apply()  // Guardamos el token
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

        // Usamos el repositorio para hacer login
        authRepository.login(email, password).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("LoginDebug", "Código: ${response.code()}")
                Log.d("LoginDebug", "Éxito: ${response.isSuccessful}")
                Log.d("LoginDebug", "Cuerpo: ${response.body()}")
                Log.d("LoginDebug", "ErrorBody: ${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val token = response.body()
                    Log.e("LoginDebug", "Token recibido: $token")
                    saveTokenToSharedPreferences(token.toString())
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LoginDebug", "ErrorBody: $errorBody")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("LoginDebug", "Error de red: ${t.message}", t)
            }
        })
    }
}




