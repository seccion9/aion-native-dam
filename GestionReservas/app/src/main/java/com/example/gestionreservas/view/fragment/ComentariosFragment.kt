package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionreservas.databinding.FragmentComentariosBinding
import com.example.gestionreservas.databinding.FragmentListadoBinding

class ComentariosFragment: Fragment() {
    private lateinit var binding:FragmentComentariosBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentComentariosBinding.inflate(layoutInflater)

        return binding.root
    }
}