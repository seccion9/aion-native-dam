package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionreservas.databinding.FragmentExperienciaTestBinding
import com.example.gestionreservas.databinding.FragmentTodasExpBinding

class TodasExpFragment: Fragment() {
    private lateinit var binding: FragmentTodasExpBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding= FragmentTodasExpBinding.inflate(layoutInflater,container,false)
        // // Inflamos el layout del fragmento para que cargue la vista correctamente
        return binding.root
    }
}