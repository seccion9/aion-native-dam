package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gestionreservas.databinding.FragmentExperienciaTestBinding
import com.example.gestionreservas.background.CheckReservasWorker

class ExperienciaTestFragment:Fragment(),OnClickListener {
    private lateinit var binding:FragmentExperienciaTestBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentExperienciaTestBinding.inflate(layoutInflater,container,false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Experiencia Test"
        binding.btnTestNotificacion.setOnClickListener(this)
        // // Inflamos el layout del fragmento para que cargue la vista correctamente
        return binding.root
    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnTestNotificacion.id->{
                val workRequest = OneTimeWorkRequestBuilder<CheckReservasWorker>().build()
                WorkManager.getInstance(requireContext()).enqueue(workRequest)
                Toast.makeText(requireContext(), "Worker lanzado manualmente", Toast.LENGTH_SHORT).show()
            }
        }
    }
}