package com.example.gestionreservas.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestionreservas.R

class ListadoFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // // Inflamos el layout del fragmento para que cargue la vista correctamente
        return layoutInflater.inflate(R.layout.fragment_listado, container, false)
    }
}