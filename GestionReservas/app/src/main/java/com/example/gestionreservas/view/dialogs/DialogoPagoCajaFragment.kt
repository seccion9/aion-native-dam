package com.example.gestionreservas.view.dialog

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.gestionreservas.databinding.FragmentDialogoPagoCajaBinding
import com.example.gestionreservas.models.entity.PagoCaja
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Dialogo para mostrar las diferentes opciones a la hora de pinchar en item de caja registradora en la
 * vista de caja chica.
 */
class DialogoPagoCajaFragment(
    private val onGuardar: (PagoCaja) -> Unit
) : DialogFragment() {

    private var _binding: FragmentDialogoPagoCajaBinding? = null
    private val binding get() = _binding!!

    private var pagoExistente: PagoCaja? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Intenta obtneer un pago si los argumentos no son nulos
        arguments?.let {
            pagoExistente = it.getSerializable("pago") as? PagoCaja
        }
    }
    //Se crea el dialogo
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogoPagoCajaBinding.inflate(LayoutInflater.from(context))
        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        inicializarUI()

        return dialog
    }
    //Si hay un pago existente(editar) rellenara los items con sus datos para después editarlo.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun inicializarUI() {
        pagoExistente?.let { pago ->
            binding.etFecha.setText(pago.fecha)
            binding.etConcepto.setText(pago.concepto)
            binding.editCantidad.setText(pago.cantidad)
        }

        binding.btnCancelar.setOnClickListener {
            dismiss()
        }
        //Crea un pago con los datos obtenidos en el pop up y guardará un nuevo objeto en la api
        binding.btnGuardar.setOnClickListener {
            val fecha = binding.etFecha.text.toString().trim()
            val concepto = binding.etConcepto.text.toString().trim()
            val cantidad = binding.editCantidad.text.toString().trim()

            if (fecha.isBlank() || concepto.isBlank() || cantidad.isBlank()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            try {
                LocalDateTime.parse(fecha, formatter)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Formato de fecha incorrecto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoPago = PagoCaja(
                id = pagoExistente?.id ?: UUID.randomUUID().toString(),
                fecha = fecha,
                concepto = concepto,
                cantidad = cantidad,
                tipo = "Efectivo",
                parcial = null
            )

            onGuardar(nuevoPago)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun nuevaInstanciaParaEditar(pago: PagoCaja, onGuardar: (PagoCaja) -> Unit): DialogoPagoCajaFragment {
            return DialogoPagoCajaFragment(onGuardar).apply {
                arguments = Bundle().apply {
                    putSerializable("pago", pago)
                }
            }
        }

        fun nuevaInstanciaParaAgregar(onGuardar: (PagoCaja) -> Unit): DialogoPagoCajaFragment {
            return DialogoPagoCajaFragment(onGuardar)
        }
    }
}
