package sv.com.chmd.transporte.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.Ruta
import sv.com.chmd.transporte.model.Usuario
import sv.com.chmd.transporte.networking.ITransporte

class LoginViewModel(
    private val iTransporte: ITransporte,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    fun iniciarSesion(username: String, password: String, onSuccess: (List<Usuario>) -> Unit,
                      onError: (Throwable) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = iTransporte.iniciarSesion(username, password).awaitResponse()
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
}