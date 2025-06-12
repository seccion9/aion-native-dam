package com.example.gestionreservas.view.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class SeleccionImagenDialogFragment(
    private val onSeleccion: (SeleccionTipoImagen) -> Unit
) : DialogFragment() {

    enum class SeleccionTipoImagen { CAMARA, GALERIA }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar imagen")
            .setItems(arrayOf("Tomar foto", "Elegir de galería")) { _, which ->
                when (which) {
                    0 -> onSeleccion(SeleccionTipoImagen.CAMARA)
                    1 -> onSeleccion(SeleccionTipoImagen.GALERIA)
                }
            }
            .create()
    }
}
