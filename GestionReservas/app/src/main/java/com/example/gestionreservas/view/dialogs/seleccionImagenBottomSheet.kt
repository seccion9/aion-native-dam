package com.example.gestionreservas.view.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gestionreservas.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SeleccionImagenBottomSheet(
    private val onSeleccion: (TipoSeleccion) -> Unit
) : BottomSheetDialogFragment() {

    enum class TipoSeleccion { CAMARA, GALERIA }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val vista = inflater.inflate(R.layout.dialog_seleccion_imagen, container, false)

        vista.findViewById<View>(R.id.opcionCamara).setOnClickListener {
            dismiss()
            onSeleccion(TipoSeleccion.CAMARA)
        }

        vista.findViewById<View>(R.id.opcionGaleria).setOnClickListener {
            dismiss()
            onSeleccion(TipoSeleccion.GALERIA)
        }

        return vista
    }
}
