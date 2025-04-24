package com.example.gestionreservas.view.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.ActivityReservasBinding
import com.example.gestionreservas.view.fragment.CalendarioFragmentDiario
import com.example.gestionreservas.view.fragment.HomeFragment
import com.example.gestionreservas.view.fragment.ListadoFragment

class ReservasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservasBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Establecemos el toolbar en la activity
        setSupportActionBar(binding.toolbar)

        // Configuracion de toolbar para abrir o cerrarla en el drawerlayout
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /* Manejo de diferentes selecciones en la activity,cada zona del menu llevara a un fragment
        * diferente para que haya menos consumo de datos
        * */

        binding.navView.setNavigationItemSelectedListener { item ->

            val fragment: Fragment? =
                when (item.itemId) {
                    R.id.home -> {
                        //Depuracion con log para comprobar que funciona al pinchar las opciones
                        Log.d("Navigation", "Se seleccionó Home")
                        HomeFragment()
                    }
                    R.id.listado -> {
                        Log.d("Navigation", "Se seleccionó Listado")
                        ListadoFragment()
                    }
                    R.id.calendario -> {
                        Log.d("Navigation", "Se seleccionó Calendario")
                        CalendarioFragmentDiario()
                    }
                    else -> null
            }

            fragment?.let {
                // Reemplaza el contenido del FrameLayout con el fragment seleccionado
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal, it)
                    .addToBackStack(null)  // Este metodo te permite volver al fragment anterior al hacer back o dar hacia atra
                    .commit()
            }
            //Metodo para cerrar el menu lateral una vez que se selecciona una opcion en el
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Cargamoss por defecto nuestro fragment principal(fragment_principal)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_principal, HomeFragment())
                .commit()
        }
    }
        //Funcion que permite responder a los click del usuario,es necesario sobreescribirla
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true
        else super.onOptionsItemSelected(item)
    }
}