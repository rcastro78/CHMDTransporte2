package sv.com.chmd.transporte.repository


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.networking.ITransporte
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaManRepository(private val iTransporte: ITransporte) {
    fun getAsistenciaManFlow(idRuta: String, token: String): Flow<List<Asistencia>> = flow {
        val response = iTransporte.getAsistenciaMan(idRuta, token).awaitResponse()

        if (response.isSuccessful) {
            emit(response.body() ?: emptyList())
        } else {
            throw HttpException(response)
        }
    }.flowOn(Dispatchers.IO)


    fun getAsistenciaManBajarFlow(idRuta: String, token: String): Flow<List<Asistencia>> = flow {
        val response = iTransporte.getAsistenciaManBajar(idRuta, token).awaitResponse()
        if (response.isSuccessful) {
            emit(response.body() ?: emptyList())
        } else {
            throw HttpException(response)
        }
    }.flowOn(Dispatchers.IO)
}
