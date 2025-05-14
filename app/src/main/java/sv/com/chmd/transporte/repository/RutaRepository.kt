package sv.com.chmd.transporte.repository

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.RutaCamionItem

class RutaRepository(
    private val db: TransporteDB,
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) {
    suspend fun obtenerRutasActivas(): List<RutaCamionItem> {
        val rutas = db.iRutaDAO.getRutasActivas()
            .filter { it.estatus.toInt() < 2 }
            .sortedBy { it.turno }

        if (rutas.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "No hay rutas asignadas", Toast.LENGTH_LONG).show()
            }
        }

        return rutas.map {
            RutaCamionItem(
                it.camion,
                it.estatus,
                it.idRuta,
                it.nombre,
                it.tipoRuta,
                it.turno
            )
        }
    }

    fun guardarRutaSeleccionada(idRuta: String) {
        sharedPreferences.edit().putString("idRuta", idRuta).apply()
    }

    fun getTotalNoProcesados(): Int {
        return db.iAsistenciaDAO.getTotalNoProcesados()
    }
}
