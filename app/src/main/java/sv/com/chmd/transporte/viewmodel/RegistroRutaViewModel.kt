package sv.com.chmd.transporte.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sv.com.chmd.transporte.model.GenericResponse
import sv.com.chmd.transporte.repository.RegistroRutaRepository

class RegistroRutaViewModel(private val repository: RegistroRutaRepository):ViewModel() {
    private val _registroEstado = MutableStateFlow<GenericResponse?>(null)
    val registroEstado: StateFlow<GenericResponse?> = _registroEstado.asStateFlow()
    fun registraRuta(
        idAuxiliar: String,
        camion: String,
        idRuta: String,
        accion: String,
        latitude: String,
        longitude: String
    ) {
        viewModelScope.launch {
            repository.registraDatosRuta(
                idAuxiliar,
                camion,
                idRuta,
                accion,
                latitude,
                longitude
            ).collect { response ->
                _registroEstado.value = response
            }
        }
    }
}