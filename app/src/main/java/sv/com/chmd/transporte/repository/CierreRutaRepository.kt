package sv.com.chmd.transporte.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.PuedeCerrarRutaResponse
import sv.com.chmd.transporte.networking.ITransporte

class CierreRutaRepository(private val iTransporte: ITransporte) {
    fun puedeCerrarRutaTarde(idRuta: String, estatus: String): Flow<Result<PuedeCerrarRutaResponse>> = flow {
        try {
            val response = iTransporte.puedeCerrarRutaTarde(idRuta, estatus).awaitResponse()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Respuesta vacía")))
            } else {
                emit(Result.failure(Exception("Error en la respuesta")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun puedeCerrarRutaMan(idRuta: String, estatus: String): Flow<Result<PuedeCerrarRutaResponse>> = flow {
        try {
            val response = iTransporte.puedeCerrarRutaMan(idRuta, estatus).awaitResponse()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Respuesta vacía")))
            } else {
                emit(Result.failure(Exception("Error en la respuesta")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}