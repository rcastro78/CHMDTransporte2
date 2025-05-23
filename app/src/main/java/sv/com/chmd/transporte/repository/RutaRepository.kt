package sv.com.chmd.transporte.repository

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.RutaCamionItem

class RutaRepository(
    private val db: TransporteDB,
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) {
    fun obtenerRutasActivas(): Flow<List<RutaCamionItem>> = flow {
        val rutas = db.iRutaDAO.getRutasActivas()
            .filter { it.estatus.toInt() < 2 }
            .sortedBy { it.turno }



        val rutaItems = rutas.map {
            RutaCamionItem(
                it.camion,
                it.estatus,
                it.idRuta,
                it.nombre,
                it.tipoRuta,
                it.turno
            )
        }

        emit(rutaItems)
    }.flowOn(Dispatchers.IO)

    fun guardarRutaSeleccionada(idRuta: String) {
        sharedPreferences.edit().putString("idRuta", idRuta).apply()
    }

    fun getTotalNoProcesados(): Int {
        return db.iAsistenciaDAO.getTotalNoProcesados()
    }
}
