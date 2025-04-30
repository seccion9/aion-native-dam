package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentListadoBinding
import com.example.gestionreservas.databinding.FragmentListadoSemanalBinding

class ListadoSemanalFragment:Fragment(),OnClickListener {
    private lateinit var binding: FragmentListadoSemanalBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentListadoSemanalBinding.inflate(layoutInflater)
        instancias()
        return binding.root
    }
    private fun instancias(){
        binding.tvListadoDia.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvListadoDia.id->{
                val fragment=ListadoFragment()
                val transaction=parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_principal,fragment)
                    .addToBackStack(null)
                transaction.commit()
            }
            binding.tvListadoSemanal.id->{

            }
        }
    }
}