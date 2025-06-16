package com.example.gestionreservas.view.fragment

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.net.Uri
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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestionreservas.R
import com.example.gestionreservas.databinding.FragmentMailingBinding
import com.example.gestionreservas.models.entity.Jugador
import com.example.gestionreservas.models.entity.Monitor
import com.example.gestionreservas.models.enums.ModoImagen
import com.example.gestionreservas.models.repository.MailingRepository
import com.example.gestionreservas.network.ApiServiceFake
import com.example.gestionreservas.network.RetrofitFakeInstance
import com.example.gestionreservas.utils.ImagenUtils
import com.example.gestionreservas.view.adapter.AdaptadorImagenes
import com.example.gestionreservas.view.adapter.AdaptadorJugadores
import com.example.gestionreservas.view.dialogs.SeleccionImagenBottomSheet
import com.example.gestionreservas.viewModel.Mailing.MailingViewModel
import com.example.gestionreservas.viewModel.Mailing.MailingViewModelFactory
import java.io.File

class MailingFragment : Fragment(), OnClickListener {
    private lateinit var binding: FragmentMailingBinding
    private lateinit var adaptadorJugadores: AdaptadorJugadores
    private lateinit var adaptadorMonitores: ArrayAdapter<String>
    private lateinit var listaMonitores: ArrayList<String>
    private lateinit var viewModel: MailingViewModel
    private var jugadorSeleccionadoId: String? = null
    private var uriFotoTemporal: Uri? = null
    private var repository = MailingRepository(RetrofitFakeInstance.apiFake)
    private val imagenesSesion = mutableListOf<String>()
    private lateinit var adaptadorImagenes: AdaptadorImagenes
    private var modoActualImagen: ModoImagen = ModoImagen.JUGADOR

    /**
     * Lanza la galeria y dependiendo del tipo de modo asigna imagen a jugador  o a la sesión
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private val launcherGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val base64 = ImagenUtils.convertirUriABase64(requireContext(), uri)
                when (modoActualImagen) {
                    ModoImagen.JUGADOR -> {
                        jugadorSeleccionadoId?.let { id ->
                            getTokenFromSharedPreferences()?.let { token ->
                                viewModel.asignarImagenAJugadorLocal(id, base64)

                            }
                        }
                    }

                    ModoImagen.SESION -> {
                        imagenesSesion.add(base64)
                        viewModel.agregarFoto(base64)
                        adaptadorImagenes.notifyItemInserted(imagenesSesion.size - 1)
                    }

                }
            }
        }
    /**
     * Lanza la camara y dependiendo del tipo de modo asigna imagen a jugador  o a la sesión
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private val launcherCamara =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
            if (exito) {
                uriFotoTemporal?.let { uri ->
                    val base64 = ImagenUtils.convertirUriABase64(requireContext(), uri)
                    when (modoActualImagen) {
                        ModoImagen.JUGADOR -> {
                            jugadorSeleccionadoId?.let { id ->
                                viewModel.asignarImagenAJugadorLocal(id, base64)
                            }
                        }

                        ModoImagen.SESION -> {
                            imagenesSesion.add(base64)
                            viewModel.agregarFoto(base64)
                            adaptadorImagenes.notifyItemInserted(imagenesSesion.size - 1)
                        }

                    }

                }
            }
        }

    /**
     * Solicitud de permisos para lanzar camara
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            lanzarCamara()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Infla el botón enviar del menú para enviar la sesión
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_mailing, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override
    fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMailingBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        //Instancia viewmodel
        val factory = MailingViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MailingViewModel::class.java]

        instancias()
        observers()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun instancias() {
        //Listeners de botones
        binding.btnAgregarFoto.setOnClickListener(this)
        binding.btnAgregarJugador.setOnClickListener(this)

        //Adaptador de imagenes
        adaptadorImagenes = AdaptadorImagenes(requireContext(), imagenesSesion)
        binding.recyclerFotos.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3)
            adapter = adaptadorImagenes
        }


        //Instancias adaptador y asignamos el recyler
        adaptadorJugadores = AdaptadorJugadores(
            context = requireContext(),
            listaJugadores = emptyList(),
            onClickAgregarImagen = { jugadorId ->
                jugadorSeleccionadoId = jugadorId
                modoActualImagen = ModoImagen.JUGADOR
                mostrarDialogoSeleccionImagen()
            }

        )
        //Adaptador de monitores
        listaMonitores = ArrayList()
        viewModel.obtenerMonitores(getTokenFromSharedPreferences().toString())
        adaptadorMonitores =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaMonitores)
        adaptadorMonitores.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonitores.adapter = adaptadorMonitores

        binding.recyclerJugadores.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adaptadorJugadores
        }


    }

    /**
     * Observers del viewmodel
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observers() {
        //Actualiza la lista de jugadores de la sesión
        viewModel.sesion.observe(viewLifecycleOwner) { sesion ->
            adaptadorJugadores.actualizarLista(sesion.jugadores)
        }
        //Limpia la lista y añade los nombres de todos los monitores
        viewModel.monitores.observe(viewLifecycleOwner) { lista ->

            listaMonitores.clear()
            listaMonitores.addAll(lista.map { it.nombre })
            adaptadorMonitores.notifyDataSetChanged()
        }
    }

    /**
     * Instancias on click
     */

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnAgregarJugador.id -> {
                mostrarDialogoNuevoJugador()
            }

            binding.btnAgregarFoto.id -> {
                modoActualImagen = ModoImagen.SESION
                mostrarDialogoSeleccionImagen()
            }
        }
    }

    /**
     * Muestra dialogo de seleccion de camara o galeria y si es camara comprueba los permisos y lanza
     * la camara,si selecciona galeria lanza galeria
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun mostrarDialogoSeleccionImagen() {
        SeleccionImagenBottomSheet { tipo ->
            when (tipo) {
                SeleccionImagenBottomSheet.TipoSeleccion.CAMARA -> {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        )
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

    /**
     * Muestra un dialogo para introducir el nombre del nuevo jugador y crea un jugador con un id,
     * nombre introducido y puntuaciones vacias,después se mostrara dialogo de selección de imagen para asignarle
     * una imagen al usuario.
     */
    @RequiresApi(Build.VERSION_CODES.O)
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
                    jugadorSeleccionadoId = nuevoJugador.id
                    modoActualImagen = ModoImagen.JUGADOR
                    mostrarDialogoSeleccionImagen()

                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Función que lanza la cámara y crea un archivo jpg de la imagen sacada al jugador
     */
    @RequiresApi(Build.VERSION_CODES.O)
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

    /**
     * Se detecta pulsación de botón enviar,actualizamos los campos del viewmodel e intentamos registrar
     * la sesión a traves de este.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_enviar -> {
                val puntuacion = binding.ratingEscapeRoom.rating
                val email = binding.editEmail.text.toString()
                val monitor = binding.spinnerMonitores.selectedItem?.toString().orEmpty()

                viewModel.actualizarPuntuacion(puntuacion)
                viewModel.actualizarEmail(email)
                viewModel.actualizarMonitor(monitor)

                val token = getTokenFromSharedPreferences().orEmpty()

                viewModel.registrarSesion(token,
                    onSuccess = {
                        Toast.makeText(
                            requireContext(),
                            "Sesión registrada correctamente",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.resetearSesion()
                    },
                    onError = { mensaje ->
                        Toast.makeText(requireContext(), "Error: $mensaje", Toast.LENGTH_LONG)
                            .show()
                    }
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //Metodo para obtener nuestro token guardado en shared preferences
    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("my_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }
}