package sv.com.chmd.transporte.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.DispositivoActivo
import sv.com.chmd.transporte.networking.ITransporte

class ValidarDispositivoViewModel(private val iTransporte: ITransporte,
                                  private val sharedPreferences: SharedPreferences
) : ViewModel()  {

    private val VALIDO="granted"
    private val REGISTRADO="-1"
    private val NO_REGISTRADO="-2"
    private val NO_VALIDO="denied"


    fun getDispositivoValido(androidId: String, onSuccessful: (DispositivoActivo) -> Unit, onError: (Exception) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {

                val response = iTransporte.validaDispositivo2(androidId).awaitResponse()
                if (response.isSuccessful) {
                    val result = response.body()
                    onSuccessful(result!!)
                }else{
                    onError(Exception("Error en la respuesta"))
                }
        }

    }


    fun registraDispositivo(
        usuario: String,
        androidId: String,
        cel: String,
        onRegisterSuccessful: (String) -> Unit,
        onValidDeviceSuccessful: (String) -> Unit,
        onNotRegistered: (String) -> Unit,
        onInvalidDevice: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = iTransporte.registrarDispositivo(usuario, androidId, cel).awaitResponse()
                if (response.isSuccessful) {
                    val result = response.body()!!
                    Log.d("REGISTRO", result)

                    verificarDispositivo(
                        androidId,
                        onRegisterSuccessful = onRegisterSuccessful,
                        onValidDeviceSuccessful = onValidDeviceSuccessful,
                        onNotRegistered = onNotRegistered,
                        onInvalidDevice = onInvalidDevice
                    )
                }
            } catch (e: Exception) {
                Log.d("ERROR_REGISTRO", e.toString())
                onError(e)
            }
        }
    }

    private fun verificarDispositivo(
        androidId: String,
        onRegisterSuccessful: (String) -> Unit,
        onValidDeviceSuccessful: (String) -> Unit,
        onNotRegistered: (String) -> Unit,
        onInvalidDevice: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = iTransporte.validaDispositivo2(androidId).awaitResponse()
                if (response.isSuccessful) {
                    val result = response.body()?.access
                    result?.let {
                        Log.d("VERIFICAR", it)
                        when (it) {
                            VALIDO -> onValidDeviceSuccessful(it)
                            REGISTRADO -> onRegisterSuccessful(it)
                            NO_REGISTRADO -> onNotRegistered(it)
                            NO_VALIDO -> onInvalidDevice(it)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("ERROR_VERIF", e.toString())
            }
        }
    }

}