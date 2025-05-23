package sv.com.chmd.transporte.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.networking.ITransporte

class AsistenciaTarRepository(private val iTransporte: ITransporte) {
    fun getAsistenciaFlow(idRuta:String, token:String): Flow<List<Asistencia>>
    = flow {
        val response = iTransporte.getAsistenciaTar(idRuta, token).awaitResponse()
        if (response.isSuccessful) {
            emit(response.body() ?: emptyList())
        } else {
            throw HttpException(response)
        }

    }.flowOn(Dispatchers.IO)
    fun getAsistenciaBajarFlow(idRuta:String, token:String): Flow<List<Asistencia>>
    = flow {

        val response = iTransporte.getAsistenciaTarBajar(idRuta, token).awaitResponse()
        if (response.isSuccessful) {
            emit(response.body() ?: emptyList())
        } else {
            throw HttpException(response)
        }

    }.flowOn(Dispatchers.IO)

}