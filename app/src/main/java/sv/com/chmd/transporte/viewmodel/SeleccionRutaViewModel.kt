package sv.com.chmd.transporte.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.RutaCamionItem

class SeleccionRutaViewModel(
    private val c: Context,
    private val db: TransporteDB,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _lstRutas = MutableStateFlow<List<RutaCamionItem>>(emptyList())
    val lstRutas: StateFlow<List<RutaCamionItem>> = _lstRutas

    init {
        getRutas()
    }

    private fun getRutas() {
        viewModelScope.launch(Dispatchers.IO) {
            val rutas = db.iRutaDAO.getRutasActivas()
                .filter { it.estatus.toInt() < 2 }
                .sortedBy { it.turno }

            if (rutas.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        c.applicationContext,
                        "No hay rutas asignadas",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            _lstRutas.value = rutas.map {
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
    }

    fun guardarRutaSeleccionada(idRuta: String, nombreRuta: String) {
        sharedPreferences.edit().putString("idRuta", idRuta).apply()
    }
}