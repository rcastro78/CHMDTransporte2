package sv.com.chmd.transporte.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.model.Ruta
import sv.com.chmd.transporte.model.Usuario
import sv.com.chmd.transporte.networking.ITransporte
import sv.com.chmd.transporte.repository.LoginRepository
import kotlin.math.log

class LoginViewModel(
    private val iTransporte: ITransporte,
    private val loginRepository: LoginRepository
) : ViewModel() {


    fun iniciaSesion(idRuta:String, token:String): Flow<List<Usuario>>{
        return loginRepository.iniciarSesion(idRuta,token)
    }

    private val _rutasUiState = MutableStateFlow<RutasUiState>(RutasUiState.Loading)
    val rutasUiState: StateFlow<RutasUiState> = _rutasUiState

    fun getRutasCamionFlow(camion: String) {
        viewModelScope.launch {
            loginRepository.getRutasCamionFlow(camion)
                .onStart {
                    _rutasUiState.value = RutasUiState.Loading
                }
                .catch { e ->
                    _rutasUiState.value = RutasUiState.Error(e.message ?: "Error desconocido")
                }
                .collect { rutas ->
                    _rutasUiState.value = RutasUiState.Success(rutas)
                }
        }
    }


    fun getRutasCamion(camion: String,onSuccess: (List<Ruta>) -> Unit,
                       onError: (Throwable) -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {
            val response = iTransporte.getRutasCamion(camion).awaitResponse()
            if(response.isSuccessful){
                val data = response.body()!!
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess(data)
                }
            }else{
                viewModelScope.launch(Dispatchers.Main) {
                    onError(Throwable(response.message()))
                }
            }
        }

    }




    fun getRutasCamion(camion: String,pwd:String,onSuccess: (List<Ruta>) -> Unit,
                       onError: (Throwable) -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {
            val response = iTransporte.getRutasCamion(camion,pwd).awaitResponse()
            if(response.isSuccessful){
                val data = response.body()!!
                viewModelScope.launch(Dispatchers.Main) {
                    if(data.isNotEmpty()) {
                        onSuccess(data)
                    }else{
                        onError(Throwable(response.message()))
                    }
                }
            }else{
                viewModelScope.launch(Dispatchers.Main) {
                    onError(Throwable(response.message()))
                }
            }
        }

    }

    fun getRutasAuxiliar(auxId: String,onSuccess: (List<Ruta>) -> Unit,
                       onError: (Throwable) -> Unit) {

        viewModelScope.launch(Dispatchers.IO) {
            val response = iTransporte.getRutasCamion(auxId).awaitResponse()
            if(response.isSuccessful){
                val data = response.body()!!
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess(data)
                }
            }else{
                viewModelScope.launch(Dispatchers.Main) {
                    onError(Throwable(response.message()))
                }
            }
        }

    }
}


sealed class RutasUiState {
    object Loading : RutasUiState()
    data class Success(val rutas: List<Ruta>) : RutasUiState()
    data class Error(val message: String) : RutasUiState()
}