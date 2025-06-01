package sv.com.chmd.transporte.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.PuedeCerrarRutaResponse
import sv.com.chmd.transporte.networking.ITransporte
import sv.com.chmd.transporte.repository.CierreRutaRepository

class CierreRutaViewModel(private val repository: CierreRutaRepository) : ViewModel()  {
    private val _cerrarRutaResult = MutableStateFlow<Result<PuedeCerrarRutaResponse>?>(null)
    val cerrarRutaResult: StateFlow<Result<PuedeCerrarRutaResponse>?> = _cerrarRutaResult

    fun puedeCerrarRutaTarde(idRuta: String, estatus: String) {
        viewModelScope.launch {
            repository.puedeCerrarRutaTarde(idRuta, estatus)
                .collect { result ->
                    _cerrarRutaResult.value = result
                }
        }
    }

    fun puedeCerrarRutaMan(idRuta: String, estatus: String) {
        viewModelScope.launch {
            repository.puedeCerrarRutaMan(idRuta, estatus)
                .collect { result ->
                    _cerrarRutaResult.value = result
                }
        }
    }
}
