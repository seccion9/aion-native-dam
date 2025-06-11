package com.example.gestionreservas.view.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.R.layout.bottomsheet_filtro_comentarios
import com.example.gestionreservas.databinding.FragmentComentariosBinding
import com.example.gestionreservas.models.entity.Comentario
import com.example.gestionreservas.models.enums.AccionComentario
import com.example.gestionreservas.models.repository.ComentariosRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.view.adapter.AdaptadorComentarios
import com.example.gestionreservas.view.dialogs.DialogoEditarComentarios
import com.example.gestionreservas.viewModel.Comentarios.ComentariosViewModel
import com.example.gestionreservas.viewModel.Comentarios.ComentariosViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ComentariosFragment: Fragment(),OnClickListener {
    private lateinit var binding:FragmentComentariosBinding
    private lateinit var viewModel:ComentariosViewModel
    private val comentariosRepository=ComentariosRepository(RetrofitFakeInstance.apiFake)
    private lateinit var adaptadorComentarios: AdaptadorComentarios
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaInicioSeleccionada: String? = null
    @RequiresApi(Build.VERSION_CODES.O)
    private var fechaFinSeleccionada: String? = null

    //Añadimos al menú el boton de filtro
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pagos, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentComentariosBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Comentarios"

        //Viewmodel
        val factory = ComentariosViewModelFactory(comentariosRepository)
        viewModel = ViewModelProvider(this, factory)[ComentariosViewModel::class.java]

        binding.swipeRefreshComentarios.isRefreshing = true
        viewModel.obtenerComentarios(getTokenFromSharedPreferences().toString())

        instancias()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias(){
        binding.tvFechaFiltro.text = "Todas las fechas"
        binding.btnAgregarComentario.setOnClickListener(this)

        //refresca la pantalla y los comentarios
        binding.swipeRefreshComentarios.setOnRefreshListener {
            viewModel.obtenerComentarios(getTokenFromSharedPreferences().toString())
        }


        //Adaptador
        adaptadorComentarios = AdaptadorComentarios(requireContext(), emptyList()) { comentario, accion ->
            when (accion) {
                // Acción de editar comentario
                AccionComentario.EDITAR -> {
                    val dialogo = DialogoEditarComentarios.nuevaInstanciaParaEditar(comentario) { comentarioEditado ->
                        viewModel.editarComentario(getTokenFromSharedPreferences().toString(), comentarioEditado)
                    }
                    dialogo.show(parentFragmentManager, "DialogoEditarComentario")

                }
                // Acción de eliminar comentario
                AccionComentario.ELIMINAR -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que quieres eliminar este comentario?")
                        .setPositiveButton("Sí") { _, _ ->
                            viewModel.eliminarComentario(getTokenFromSharedPreferences().toString(),comentario)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()

                }
            }
        }

        binding.recylerComentarios.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recylerComentarios.adapter = adaptadorComentarios
        //Animación del recycler
        binding.recylerComentarios.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fade_in)

        observers()
        filtrarPorDescripcion()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun filtrarPorDescripcion() {
        binding.editFiltroNombre.addTextChangedListener { editable ->
            val descripcion = editable.toString() ?: ""
            viewModel.actualizarFiltros(
                descripcion = descripcion,
                fechaInicio = fechaInicioSeleccionada,
                fechaFin = fechaFinSeleccionada
            )
        }
    }


    /**
     * Observers del viewmodel para cambiar datos de la vista en tiempo rela.
     */
    private fun observers() {
        //Actualiza la lista de comentarios al detecta un cambio el viewmodel
        viewModel.comentarios.observe(viewLifecycleOwner) { comentarios ->
            Log.d("ComentariosFragment", "Se recibieron ${comentarios.size} comentarios")
            adaptadorComentarios.actualizarLista(comentarios)
            binding.swipeRefreshComentarios.isRefreshing = false
            binding.recylerComentarios.scheduleLayoutAnimation()
        }
        //Comprueba si fue exitosa la eliminación de un comentario
        viewModel.comentarioEliminado.observe(viewLifecycleOwner) { exito ->
            exito?.let {
                if (it) {
                    Toast.makeText(
                        requireContext(),
                        "Comentario eliminado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al eliminar el comentario",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                viewModel.limpiarEstadoComentarioEliminado()
            }
        }
    }
    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    /**
     * Detecta la pulsación del icono de filtro del menú para mostrar bottomsheet
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filtro -> {
                mostrarBottomSheetFiltro()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Muestra bottomsheet filtrar comentarios
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarBottomSheetFiltro() {
        val bottomSheetView = layoutInflater.inflate(bottomsheet_filtro_comentarios, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(bottomSheetView)

        val editFechaInicio = bottomSheetView.findViewById<EditText>(R.id.editFechaInicio)
        val editFechaFin = bottomSheetView.findViewById<EditText>(R.id.editFechaFin)
        val btnAplicar = bottomSheetView.findViewById<TextView>(R.id.btnAplicarFiltros)
        val btnCancelar = bottomSheetView.findViewById<TextView>(R.id.btnCancelarFiltros)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val hoy = LocalDate.now()

        // Fecha Inicio
        editFechaInicio.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
                val fecha = LocalDate.of(year, month + 1, day)
                editFechaInicio.setText(fecha.format(formatter))
            }, hoy.year, hoy.monthValue - 1, hoy.dayOfMonth)
            datePicker.show()
        }

        // Fecha Fin
        editFechaFin.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
                val fecha = LocalDate.of(year, month + 1, day)
                editFechaFin.setText(fecha.format(formatter))
            }, hoy.year, hoy.monthValue - 1, hoy.dayOfMonth)
            datePicker.show()
        }

        // Cancelar
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        // Aplicar filtro
        btnAplicar.setOnClickListener {
            val fechaInicio = editFechaInicio.text.toString()
            val fechaFin = editFechaFin.text.toString()

            if (fechaInicio.isNotEmpty() && fechaFin.isNotEmpty()) {
                // Aquí sí tiene sentido guardar
                fechaInicioSeleccionada = fechaInicio
                fechaFinSeleccionada = fechaFin

                // Reaplicamos filtro combinado
                val descripcionActual = binding.editFiltroNombre.text.toString()
                viewModel.actualizarFiltros(
                    descripcion = descripcionActual,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin
                )

                binding.tvFechaFiltro.text = "${fechaInicio.substring(5,10)} / ${fechaFin.substring(5,10)}"
                dialog.dismiss()
            }
        }


        dialog.show()
    }

    /**
     * Muestra dialogo de agregar comentario al pulsar en agregar y lo añade a la api a través
     * del viewmodel
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnAgregarComentario.id -> {
                val dialogo = DialogoEditarComentarios.nuevaInstanciaParaAgregar { nuevoComentario ->
                    viewModel.agregarComentario(getTokenFromSharedPreferences().toString(), nuevoComentario)
                }
                dialogo.show(parentFragmentManager, "DialogoNuevoComentario")
            }
        }
    }


}