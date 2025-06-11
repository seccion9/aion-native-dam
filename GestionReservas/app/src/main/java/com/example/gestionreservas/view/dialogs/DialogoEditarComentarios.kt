package com.example.gestionreservas.view.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.DialogoEditarComentarioBinding
import com.example.gestionreservas.models.entity.Comentario
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DialogoEditarComentarios(
    private val onGuardar: (Comentario) -> Unit
) : DialogFragment() {

    private var _binding: DialogoEditarComentarioBinding? = null
    private val binding get() = _binding!!
    private var comentarioExistente: Comentario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            comentarioExistente = it.getSerializable("comentario") as? Comentario
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogoEditarComentarioBinding.inflate(LayoutInflater.from(context))
        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        inicializarUI()
        return dialog
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun inicializarUI() {
        comentarioExistente?.let { comentario ->
            binding.editUsuario.setText(comentario.nombreUsuario)
            binding.editFecha.setText(comentario.fecha)
            binding.editDescripcion.setText(comentario.descripcion)

            val tipos = resources.getStringArray(R.array.comentario_array)
            val index = tipos.indexOf(comentario.tipo)
            if (index != -1) {
                binding.spinnerTipoComentario.setSelection(index)
            }
        } ?: run {
            // Si es nuevo comentario, prerellenar campos útiles
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            binding.editFecha.setText(LocalDateTime.now().format(formatter))
            binding.editUsuario.setText("Sergio Gómez")
            binding.tvTituloEditar.setText("Agregar comentario")
        }

        binding.btnGuardar.setOnClickListener {
            val fecha = binding.editFecha.text.toString().trim()
            val usuario = binding.editUsuario.text.toString().trim()
            val descripcion = binding.editDescripcion.text.toString().trim()
            val tipo = binding.spinnerTipoComentario.selectedItem.toString()

            if (fecha.isBlank() || usuario.isBlank() || descripcion.isBlank()) {
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

            val comentarioFinal = Comentario(
                id = comentarioExistente?.id ?: java.util.UUID.randomUUID().toString(),
                fecha = fecha,
                nombreUsuario = usuario,
                descripcion = descripcion,
                tipo = tipo,
            )

            onGuardar(comentarioFinal)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun nuevaInstanciaParaEditar(
            comentario: Comentario,
            onGuardar: (Comentario) -> Unit
        ): DialogoEditarComentarios {
            return DialogoEditarComentarios(onGuardar).apply {
                arguments = Bundle().apply {
                    putSerializable("comentario", comentario)
                }
            }
        }

        fun nuevaInstanciaParaAgregar(
            onGuardar: (Comentario) -> Unit
        ): DialogoEditarComentarios {
            return DialogoEditarComentarios(onGuardar)
        }
    }
}
