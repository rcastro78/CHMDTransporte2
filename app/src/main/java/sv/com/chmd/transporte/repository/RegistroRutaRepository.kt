package sv.com.chmd.transporte.repository

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.GenericResponse
import sv.com.chmd.transporte.networking.ITransporte

class RegistroRutaRepository(private val iTransporte: ITransporte) {
    fun registraDatosRuta(idAuxiliar:String, camion: String, idRuta:String,
                          accion:String,latitud:String,longitud:String): Flow<GenericResponse> = flow {
        val response = iTransporte.registraRuta(idAuxiliar, camion, idRuta, accion, latitud, longitud).awaitResponse()
        if (response.isSuccessful) {
            emit(response.body() ?: GenericResponse(false, response.message()))
        }else{
            emit(GenericResponse(false, response.message()))
        }
    }.flowOn(Dispatchers.IO)
}