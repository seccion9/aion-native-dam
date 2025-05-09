package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentExperienciaTestBinding

class ExperienciaTestFragment:Fragment() {
    private lateinit var binding:FragmentExperienciaTestBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentExperienciaTestBinding.inflate(layoutInflater,container,false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Experiencia Test"
        // // Inflamos el layout del fragmento para que cargue la vista correctamente
        return binding.root
    }
}