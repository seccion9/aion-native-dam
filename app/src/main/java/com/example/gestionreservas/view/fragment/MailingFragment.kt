package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionreservas.databinding.FragmentMailingBinding

class MailingFragment: Fragment() {
    private lateinit var binding:FragmentMailingBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMailingBinding.inflate(layoutInflater)
        return binding.root
    }
}