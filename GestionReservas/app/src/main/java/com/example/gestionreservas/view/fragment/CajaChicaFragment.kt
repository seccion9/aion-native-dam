package com.example.gestionreservas.view.fragment

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentCajaChicaBinding
import com.example.gestionreservas.models.repository.CajaChicaRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorCajaChica
import com.example.gestionreservas.view.dialog.DialogoPagoCajaFragment
import com.example.gestionreservas.viewModel.CajaChica.CajaChicaViewModel
import com.example.gestionreservas.viewModel.CajaChica.CajaChicaViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.time.format.DateTimeFormatter

class CajaChicaFragment : Fragment(), OnClickListener {
    private lateinit var binding: FragmentCajaChicaBinding
    private lateinit var adaptadorCajaChica: AdaptadorCajaChica
    private lateinit var viewModel: CajaChicaViewModel
    private var accionActual: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCajaChicaBinding.inflate(layoutInflater)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Caja Chica"
        val cajaChicaRepository = CajaChicaRepository(RetrofitFakeInstance)
        //Viewmodel
        val factory = CajaChicaViewModelFactory(cajaChicaRepository)
        viewModel = ViewModelProvider(this, factory)[CajaChicaViewModel::class.java]

        binding.swipeRefreshCajaChica.isRefreshing = true
        instancias()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        binding.btnConfirmarSeleccion.setOnClickListener(this)
        binding.cajaChicaItem.btnCajaOpciones.setOnClickListener(this)
        viewModel.obtenerFechaHoy()
        //Refresca los datos de la vista
        binding.swipeRefreshCajaChica.setOnRefreshListener {
            viewModel.obtenerFechaHoy()
        }

        //Muestra dialogo de las opciones a elegir al pinchar en menu del item de la caja aparte de instanciar
        //el adaptador
        adaptadorCajaChica = AdaptadorCajaChica(requireContext(), emptyList()) { pagoSeleccionado ->
            mostrarDialogoOpciones()
        }

        binding.recyclerPagosCaja.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerPagosCaja.adapter = adaptadorCajaChica
        //Animación del recylerview
        binding.recyclerPagosCaja.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fade_in)

        observers()
    }

    /**
     * Observers del viewmodel para actualizar la info de la vista en tiempo real cuando el usuario va
     * interactuando con la vista
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observers() {
        //Actualiza lista de pagos y asigna el total al textView añadiendo animación al recyler
        viewModel.pagosCajaChica.observe(viewLifecycleOwner) { pagos ->
            adaptadorCajaChica.actualizarLista(pagos)
            val total = pagos.sumOf { it.cantidad.toDoubleOrNull() ?: 0.0 }
            binding.cajaChicaItem.tvCajaTotal.text = String.format("%.2f€", total)
            binding.recyclerPagosCaja.scheduleLayoutAnimation()
            binding.swipeRefreshCajaChica.isRefreshing = false

        }
        //Comprueba si el pago registrado fue exitoso o no
        viewModel.pagoRegistrado.observe(viewLifecycleOwner) { exito ->
            exito?.let {
                if (it) {
                    Toast.makeText(
                        requireContext(),
                        "Pago guardado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar el pago", Toast.LENGTH_SHORT)
                        .show()
                }

                viewModel.limpiarEstadoPago()
            }
        }
        //Comprueba si el pago eliminado fue exitoso o no
        viewModel.pagoEliminado.observe(viewLifecycleOwner) { exito ->
            exito?.let {
                if (it) {
                    Toast.makeText(requireContext(), "Pago eliminado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar el pago", Toast.LENGTH_SHORT).show()
                }

                viewModel.limpiarEstadoPagoEliminado()
            }
        }

        //Actualiza la fecha de la vista y recarga los nuevos datos
        viewModel.fechaActual.observe(viewLifecycleOwner) { fecha ->
            val token = "Bearer ${getTokenFromSharedPreferences()}"
            val fechaFormateada = fecha.format(DateTimeFormatter.ISO_LOCAL_DATE)

            if (token != null) {
                viewModel.obtenerPagosCajaDia(token, fechaFormateada)

            }
        }
    }

    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    /**
     * Funciones on click del fragment
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.cajaChicaItem.btnCajaOpciones.id -> {
                mostrarDialogoOpciones()
            }
            //Dependiendo de la opción seleccionada en el dialogo hara la opción de editar o eliminar un pago.
            binding.btnConfirmarSeleccion.id -> {
                val pagoSeleccionado = adaptadorCajaChica.obtenerSeleccionado()
                if (pagoSeleccionado != null) {
                    val token = getTokenFromSharedPreferences()
                    when (accionActual) {
                        //Abre un dialogo al seleccionar item de la caja y edita un pago seleccionado
                        "editar" -> {
                            val dialogo = DialogoPagoCajaFragment.nuevaInstanciaParaEditar(pagoSeleccionado) { pagoEditado ->
                                viewModel.editarPago(token.toString(), pagoEditado)
                            }
                            dialogo.show(parentFragmentManager, "DialogoEditarPago")
                        }
                        //Abre un dialogo al seleccionar item de la caja y elimina un pago seleccionado
                        "eliminar" -> {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Confirmar eliminación")
                                .setMessage("¿Estás seguro de que quieres eliminar este pago?")
                                .setPositiveButton("Sí") { _, _ ->
                                    viewModel.eliminarPago(token.toString(), pagoSeleccionado)
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }

                    }
                    adaptadorCajaChica.mostrarCheckboxes(false)
                    binding.btnConfirmarSeleccion.visibility = View.GONE
                    accionActual = null
                } else {
                    mostrarSnackbar("Selecciona un pago primero")
                }
            }


        }
    }

    /**
     * Dialogo para mostrar diferentes opciones al usuario y dependiendo de la opción seleccinada la vista
     * mostrará unas opciones u otras.
     */
    private fun mostrarDialogoOpciones() {
        val opciones = arrayOf("Agregar", "Editar", "Eliminar")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona una opción")
        builder.setItems(opciones) { dialog, which ->
            when (which) {
                //Opcion agregar pago a través de DialogoPagoCajaFragment
                0 -> {
                    accionActual = null
                    val token = "Bearer ${getTokenFromSharedPreferences() ?: ""}"
                    val dialogo = DialogoPagoCajaFragment.nuevaInstanciaParaAgregar { nuevoPago ->
                        viewModel.agregarPago(token, nuevoPago)
                    }
                    dialogo.show(parentFragmentManager, "DialogoAgregarPago")
                }
                //Opcion editar pago a través de DialogoPagoCajaFragment
                1 -> {
                    accionActual = "editar"
                    adaptadorCajaChica.mostrarCheckboxes(true)
                    binding.btnConfirmarSeleccion.visibility = View.VISIBLE
                    mostrarSnackbar("Selecciona un pago para editar y pulsa Confirmar")
                }
                //Cambia estado check box y boton confirmación despues de eliminar pago.
                2 -> {
                    accionActual = "eliminar"
                    adaptadorCajaChica.mostrarCheckboxes(true)
                    binding.btnConfirmarSeleccion.visibility = View.VISIBLE
                    mostrarSnackbar("Selecciona un pago para borrar y pulsa Confirmar")
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
    //Mensaje general para mostrar snackbar
    private fun mostrarSnackbar(mensaje: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
    }

}