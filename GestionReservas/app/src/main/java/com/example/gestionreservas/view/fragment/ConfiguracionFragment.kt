package com.example.gestionreservas.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.gestionreservas.databinding.FragmentConfiguracionBinding

class ConfiguracionFragment:Fragment() {
    private lateinit var binding:FragmentConfiguracionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentConfiguracionBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Configuracion"
        instancias()
        return binding.root
    }
    private fun instancias(){
        cambiarAModoOscuro()
    }
    private fun cambiarAModoOscuro(){
        val prefs = requireContext().getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        val modoOscuroActivado = prefs.getBoolean("modo_oscuro", false)

        binding.switchModoOscuro.isChecked = modoOscuroActivado

        binding.switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            val nuevoModo = if (isChecked)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

            AppCompatDelegate.setDefaultNightMode(nuevoModo)

            prefs.edit().putBoolean("modo_oscuro", isChecked).apply()
        }
    }

}