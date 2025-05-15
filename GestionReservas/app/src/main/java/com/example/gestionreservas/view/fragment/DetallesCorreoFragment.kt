package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gestionreservas.databinding.FragmentDetallesCorreoBinding

class DetallesCorreoFragment:Fragment() {
    private lateinit var binding:FragmentDetallesCorreoBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentDetallesCorreoBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Confirmación Reserva"

        return binding.root
    }
}