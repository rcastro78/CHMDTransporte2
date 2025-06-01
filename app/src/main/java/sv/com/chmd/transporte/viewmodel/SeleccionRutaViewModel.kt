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
import sv.com.chmd.transporte.repository.RutaRepository

class SeleccionRutaViewModel(
    private val repository: RutaRepository
) : ViewModel() {

    private val _lstRutas = MutableStateFlow<List<RutaCamionItem>>(emptyList())
    val lstRutas: StateFlow<List<RutaCamionItem>> = _lstRutas

    init {
        getRutas()
    }

    private fun getRutas() {
        viewModelScope.launch {
            repository.obtenerRutasActivas().collect { rutas ->
                _lstRutas.value = rutas
            }
        }
    }

    fun guardarRutaSeleccionada(idRuta: String) {
        repository.guardarRutaSeleccionada(idRuta)
    }

    suspend fun getTotalNoProcesados(): Int {
        return withContext(Dispatchers.IO) {
            repository.getTotalNoProcesados()
        }
    }
}