package com.example.gestionreservas.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentMailingBinding
import com.example.gestionreservas.models.entity.Jugador
import com.example.gestionreservas.view.adapter.AdaptadorJugadores
import com.example.gestionreservas.view.dialogs.SeleccionImagenBottomSheet
import com.example.gestionreservas.viewModel.Mailing.MailingViewModel
import com.example.gestionreservas.viewModel.Mailing.MailingViewModelFactory
import java.io.File

class MailingFragment:Fragment(),OnClickListener {
    private lateinit var binding: FragmentMailingBinding
    private lateinit var adaptadorJugadores: AdaptadorJugadores
    private lateinit var viewModel: MailingViewModel
    private var jugadorSeleccionadoId: String? = null
    private var uriFotoTemporal: Uri? = null

    private val launcherGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            jugadorSeleccionadoId?.let { id -> viewModel.asignarImagenAJugador(id, it.toString()) }
        }
    }

    private val launcherCamara = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            jugadorSeleccionadoId?.let { id ->
                uriFotoTemporal?.let { uri -> viewModel.asignarImagenAJugador(id, uri.toString()) }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            lanzarCamara()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }
    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMailingBinding.inflate(layoutInflater)
        //Instancia viewmodel
        val factory = MailingViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[MailingViewModel::class.java]

        instancias()
        observers()
        return binding.root
    }
    private fun instancias(){
        binding.btnAgregarJugador.setOnClickListener(this)
        //Instancias adaptador y asignamos el recyler
        adaptadorJugadores = AdaptadorJugadores(
            context = requireContext(),
            listaJugadores = emptyList(),
            onClickAgregarImagen = { jugadorId ->
                jugadorSeleccionadoId = jugadorId
                mostrarDialogoSeleccionImagen()
            }

        )

        binding.recyclerJugadores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adaptadorJugadores
        }

        // Cargar los jugadores iniciales desde el ViewModel
        viewModel.cargarJugadoresIniciales()

    }

    /**
     * Observers del viewmodel
     */
    private fun observers(){
        viewModel.jugadores.observe(viewLifecycleOwner) {
            adaptadorJugadores.actualizarLista(it)
        }
    }

    /**
     * Instancias on click
     */
    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnAgregarJugador.id->{
                mostrarDialogoNuevoJugador()
            }
        }
    }
    private fun mostrarDialogoSeleccionImagen() {
        SeleccionImagenBottomSheet { tipo ->
            when (tipo) {
                SeleccionImagenBottomSheet.TipoSeleccion.CAMARA -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        lanzarCamara()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                SeleccionImagenBottomSheet.TipoSeleccion.GALERIA -> launcherGaleria.launch("image/*")
            }
        }.show(parentFragmentManager, "SeleccionImagenBottomSheet")
    }


    private fun mostrarDialogoNuevoJugador() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_nuevo_jugador, null)
        val editText = dialogView.findViewById<android.widget.EditText>(R.id.etNombreJugador)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Nuevo jugador")
            .setView(dialogView)
            .setPositiveButton("Aceptar") { _, _ ->
                val nombreJugador = editText.text.toString().trim()
                if (nombreJugador.isNotEmpty()) {
                    val nuevoId = System.currentTimeMillis().toString()
                    val nuevoJugador = Jugador(
                        id = nuevoId,
                        nombre = nombreJugador,
                        imagen = "",
                        puntuaciones = emptyList()
                    )
                    viewModel.agregarJugador(nuevoJugador)
                    jugadorSeleccionadoId = nuevoId
                    mostrarDialogoSeleccionImagen()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Función que lanza la cámara y crea un archivo jpg de la imagen sacada al jugador
     */
    private fun lanzarCamara() {
        val archivoFoto = File.createTempFile("jugador_", ".jpg", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        uriFotoTemporal = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            archivoFoto
        )

        launcherCamara.launch(uriFotoTemporal)
    }

}