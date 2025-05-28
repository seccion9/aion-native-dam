package com.example.gestionreservas.models.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.PagoCajaChica
import com.example.gestionreservas.network.RetrofitFakeInstance
import java.time.LocalDate

class CajaChicaRepository(private val retrofit: RetrofitFakeInstance) {

    suspend fun obtenerPagosDelDia(token: String, fecha: String): List<PagoCajaChica> {
        val response = retrofit.apiFake.getPagosCajaDia(token, fecha)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error HTTP ${response.code()}")
        }
    }

    suspend fun obtenerCompras(token: String): List<Compra> {
        val response = retrofit.apiFake.getPurchasesV2(token)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error HTTP ${response.code()}")
        }
    }

    fun transformarPagosCajaApi(pagos: List<PagoCajaChica>): List<PagoCaja> {
        return pagos.map {
            PagoCaja(it.fecha, it.concepto, it.cantidad.toString(), it.tipo, "")
        }
    }
    suspend fun registrarPagoCajaChica(token: String, pago: PagoCaja): Boolean {
        return try {
            val response = retrofit.apiFake.registrarPagoCajaChica("Bearer $token", pago)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun transformarComprasAPagos(compras: List<Compra>, fecha: LocalDate): List<PagoCaja> {
        val pagos = mutableListOf<PagoCaja>()
        compras.forEach { compra ->
            val itemsDelDia = compra.items.filter {
                LocalDate.parse(it.start.substring(0, 10)) == fecha
            }

            if (itemsDelDia.isNotEmpty()) {
                val fechaStr = itemsDelDia.first().start.substring(0, 10)
                val concepto = "Reserva de ${compra.name}"
                compra.payments.forEach { pago ->
                    pagos.add(
                        PagoCaja(
                            fechaStr,
                            concepto,
                            "%.2f €".format(compra.priceFinal),
                            pago.method,
                            "%.2f €".format(pago.amount)
                        )
                    )
                }
            }
        }
        return pagos
    }
}
