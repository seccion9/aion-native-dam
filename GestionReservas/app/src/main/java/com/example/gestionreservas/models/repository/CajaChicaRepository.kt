package com.example.gestionreservas.models.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.gestionreservas.models.entity.Compra
import com.example.gestionreservas.models.entity.PagoCaja
import com.example.gestionreservas.models.entity.PagoCajaChica
import com.example.gestionreservas.network.RetrofitFakeInstance
import java.time.LocalDate

class CajaChicaRepository(private val retrofit: RetrofitFakeInstance) {
    /**
     * Obtiene los pagos de un día concreto de la API con el token y fehca,devuelve en el body
     * una lista de tipo PagoCaja
     */
    suspend fun obtenerPagosDelDia(token: String, fecha: String): List<PagoCaja> {
        val response = retrofit.apiFake.getPagosCajaDia(token, fecha)
        if (response.isSuccessful) {

            return response.body() ?: emptyList()
        } else {
            throw Exception("Error HTTP ${response.code()}")
        }
    }
    /**
     * Obtiene los pagos de la API con el token ,devuelve en el body
     * una lista de tipo PagoCaja
     */
    suspend fun obtenerPagosTotales(token:String):List<PagoCajaChica>{
        val response=retrofit.apiFake.getPagosCaja("Bearer $token")
        if(response.isSuccessful){
            return response.body() ?: emptyList()
        }else{
            throw Exception("Error HTTP ${response.code()}")
        }
    }

    /**
     * Obtiene todos los pagos de caja chica sin ningun filtro a través del token
     */
    suspend fun obtenerPagosTotalesCajaChica(token:String):List<PagoCaja>{
        val response=retrofit.apiFake.getPagosCajaV2(token)
        if(response.isSuccessful){
            return response.body() ?: emptyList()
        }else{
            throw Exception("Error HTTP ${response.code()}")
        }
    }
    /**
     * Obtiene todas las compras de la API sin ningun filtro a través del token
     */
    suspend fun obtenerCompras(token: String): List<Compra> {
        val response = retrofit.apiFake.getPurchasesV2(token)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error HTTP ${response.code()}")
        }
    }

    /**
     * Registra un pago de la caja chica con el token y ese pago y devuelve true o false dependiendo
     * del exito de la operación.
     */
    suspend fun registrarPagoCajaChica(token: String, pago: PagoCaja): Boolean {
        return try {
            val response = retrofit.apiFake.registrarPagoCajaChica("Bearer $token", pago)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Edita un pago de la caja chica con el token y los valores modificados,devuelve true o false
     * dependiendo del exito de la operación.
     */
    suspend fun editarPago(token: String, pago: PagoCaja): Boolean {
        return try {
            val id = pago.id ?: return false
            Log.d("EDITAR_PAGO", "ID que envío: ${id}")
            Log.d("EDITAR_PAGO", "Token: Bearer $token")

            val response = retrofit.apiFake.editarPagoCajaChica("Bearer $token", id, pago)

            if (!response.isSuccessful) {
                Log.e("CajaChicaRepository", "Error al editar pago: HTTP ${response.code()}")
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("CajaChicaRepository", "Excepción al editar pago: ${e.message}")
            false
        }
    }

    /**
     *Elimina un pago de la API con el token y ese pago que se usará su id,devolvera true o false dependiendo del
     * exito de la operación.
     */
    suspend fun eliminarPago(token: String, pago: PagoCaja): Boolean {
        return try {
            val response = retrofit.apiFake.eliminarPago("Bearer $token", pago.id.toString())
            if (!response.isSuccessful) {
                Log.e("CajaChicaRepository", "Error al borrar pago: HTTP ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("CajaChicaRepository", "Excepción al borrar pago: ${e.message}")
            false
        }
    }
}
