package sv.com.chmd.transporte.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.PuedeCerrarRutaResponse
import sv.com.chmd.transporte.networking.ITransporte

class CierreRutaViewModel(private val iTransporte: ITransporte,
                          private val sharedPreferences: SharedPreferences
) : ViewModel()  {
    fun puedeCerrarRutaTarde(idRuta:String, estatus:String,
                        onSuccess: (PuedeCerrarRutaResponse) -> Unit,
                        onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = iTransporte.puedeCerrarRutaTarde(idRuta, estatus).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            }else{
                onError("Error en la respuesta")
            }
        }
    }
}
