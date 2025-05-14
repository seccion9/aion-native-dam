package com.example.gestionreservas.view.fragment

import android.R
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gestionreservas.databinding.FragmentPostPurchaseBinding
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.Field
import com.example.gestionreservas.models.entity.ItemReserva
import com.example.gestionreservas.models.entity.Pago
import com.example.gestionreservas.models.entity.ResumenItem
import com.example.gestionreservas.models.repository.CompraRepository
import com.example.gestionreservas.network.RetrofitFakeInstance
import kotlinx.coroutines.launch
import java.util.UUID

class PostPurchaseFragment : Fragment(), OnClickListener {
    private lateinit var compra: Compra
    private lateinit var itemReserva: ItemReserva
    private lateinit var binding: FragmentPostPurchaseBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostPurchaseBinding.inflate(layoutInflater)
        instancias()
        return binding.root
    }

    private fun instancias() {
        binding.tvGuardar.setOnClickListener(this)
        binding.tvParticipantes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val seleccion = parent.getItemAtPosition(position).toString()
                Log.d("DEBUG", "Participantes seleccionado: $seleccion")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Opcional, puede quedarse vacío
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            binding.tvGuardar.id -> {
                registrarCompras()

            }
        }
    }

    private fun registrarCompras() {
        if (!validarCamposObligatorios()) return
        rellenarCompra()
        lifecycleScope.launch {
            try {
                val token = getTokenFromSharedPreferences()
                val repository = CompraRepository(RetrofitFakeInstance.apiFake)
                val response = repository.registrarCompra(token!!, compra)
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Compra con id : ${compra.id} registrada correctamente",
                        Toast.LENGTH_LONG
                    ).show()
                    vaciarEdits()
                    val fragment=CalendarioFragmentDiario()
                    cambiarFragment(fragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Hubo un error al registrar la compra ${response.message()}",
                        Toast.LENGTH_LONG

                    ).show()


                }
            } catch (e: Exception) {
                Log.e("PostPurchaseFragment", "Error al registrar compra ${e.message}")
            }
        }
    }

    private fun rellenarCompra() {
        var nombre = binding.tvNombre.text.toString()
        val email = binding.tvEmail.text.toString()
        val telefono = binding.tvTelefono.text.toString()
        val dni = binding.tvDNI.text.toString()
        val direccion = binding.tvDireccion.text.toString()
        val fechaInicio = binding.tvFechaInicio.text.toString()
        val fechaFin = binding.tvFechaFin.text.toString()
        val calendario = binding.tvSala.selectedItem.toString()
        val experiencia = binding.tvExperiencia.selectedItem.toString()
        val participantes = binding.tvParticipantes.selectedItem.toString().toInt()

        val idioma = binding.tvIdioma.selectedItem.toString()
        val estado = binding.tvEstado.text.toString()
        val precio = binding.tvPrecio.text.toString().toDouble()
        val totalPagado = binding.tvTotalPagado.text.toString().toDouble()
        val metodoPago = binding.tvMetodoPago.text.toString()
        val priceFractioned = precio / participantes

        val fieldIdioma = Field(
            id = UUID.randomUUID().toString(),
            title = "Idioma",
            name = "language",
            value = idioma,
            amount = 0.0
        )

        itemReserva = ItemReserva(
            id = UUID.randomUUID().toString(),
            idExperience = experiencia,
            idCalendario = calendario,
            idBusinessUnit = null,
            status = estado,
            internaPermanent = false,
            start = fechaInicio,
            end = fechaFin,
            duration = 60,
            peopleNumber = participantes,
            priceOriginal = precio,
            priceTotal = precio,
            priceFractioned = priceFractioned,
            discountAmount = 0,
            fields = listOf(fieldIdioma)
        )

        val resumenItem = ResumenItem(
            id = itemReserva.id,
            id_experience = experiencia,
            id_calendario = calendario,
            start = fechaInicio,
            end = fechaFin,
            people_number = participantes,
            price_original = precio,
            price_fractioned = priceFractioned
        )

        val pago = Pago(
            id = UUID.randomUUID().toString(),
            amount = totalPagado,
            method = metodoPago
        )

        compra = Compra(
            id = UUID.randomUUID().toString(),
            uuid = UUID.randomUUID().toString(),
            status = estado,
            mailStatus = "none",
            internaPermanent = false,
            idDiscount = null,
            idBono = null,
            priceFinal = precio,
            priceAfterDiscount = precio,
            priceFractioned = priceFractioned,
            isFractioned = false,
            name = nombre,
            mail = email,
            dni = dni,
            phone = telefono,
            direction = direccion,
            language = "es",
            ip = "192.168.1.10",
            comment = "",
            automaticActions = "",
            items = listOf(itemReserva),
            payments = listOf(pago),
            resumenItems = listOf(resumenItem)
        )

        Toast.makeText(requireContext(), "Compra generada con éxito", Toast.LENGTH_SHORT).show()
    }

    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            requireActivity().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    private fun validarCamposObligatorios(): Boolean {
        val camposObligatorios = listOf(
            binding.tvNombre,
            binding.tvEmail,
            binding.tvTelefono,
            binding.tvDNI,
            binding.tvDireccion,
            binding.tvFechaInicio,
            binding.tvFechaFin,
            binding.tvPrecio,
            binding.tvTotalPagado,
            binding.tvMetodoPago
        )

        for (campo in camposObligatorios) {
            if (campo.text.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Por favor, completa todos los campos obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        // Validación de spinners
        val idioma = binding.tvIdioma.selectedItem
        if (idioma == null || idioma.toString() == "--Idioma--") {
            Toast.makeText(requireContext(), "Selecciona un idioma", Toast.LENGTH_SHORT).show()
            return false
        }

        val participantes = binding.tvParticipantes.selectedItem
        if (participantes == null || participantes.toString() == "--Participantes--") {
            Toast.makeText(requireContext(), "Selecciona número de participantes", Toast.LENGTH_SHORT).show()
            return false
        }

        val calendario = binding.tvSala.selectedItem
        if (calendario == null || calendario.toString() == "--Calendario--") {
            Toast.makeText(requireContext(), "Selecciona un calendario", Toast.LENGTH_SHORT).show()
            return false
        }

        val experiencia = binding.tvExperiencia.selectedItem
        if (experiencia == null || experiencia.toString() == "--Experiencia--") {
            Toast.makeText(requireContext(), "Selecciona una experiencia", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
    private fun vaciarEdits(){
        binding.tvNombre.text.clear()
        binding.tvEmail.text.clear()
        binding.tvTelefono.text.clear()
        binding.tvDNI.text.clear()
        binding.tvDireccion.text.clear()
        binding.tvFechaInicio.text.clear()
        binding.tvFechaFin.text.clear()
        binding.tvPrecio.text.clear()
        binding.tvTotalPagado.text.clear()
        binding.tvMetodoPago.text.clear()
    }
    private fun cambiarFragment(fragment:Fragment){
        val transacion=parentFragmentManager.beginTransaction()
        transacion.replace(com.example.gestionreservas.R.id.fragment_principal,fragment)
        transacion.addToBackStack(null)
        transacion.commit()
    }

}