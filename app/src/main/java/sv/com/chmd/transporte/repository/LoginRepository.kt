package sv.com.chmd.transporte.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.Ruta
import sv.com.chmd.transporte.model.Usuario
import sv.com.chmd.transporte.networking.ITransporte

class LoginRepository(private val iTransporte: ITransporte){
    fun iniciarSesion(username: String, password: String): Flow<List<Usuario>> = flow {
        val response = iTransporte.iniciarSesion(username, password).awaitResponse()
        if (response.isSuccessful) {
            emit(response.body() ?: emptyList())
        } else {
            throw HttpException(response)
        }
    }.flowOn(Dispatchers.IO)


    fun getRutasCamionFlow(camion: String): Flow<List<Ruta>> = flow {
        val response = iTransporte.getRutasCamion(camion).awaitResponse()

        if (response.isSuccessful) {
            val rutas = response.body() ?: emptyList()
            emit(rutas)
        } else {
            when (response.code()) {
                400 -> throw IllegalArgumentException("Camión no registrado para hoy.")
                404 -> throw NoSuchElementException("Camión sin rutas activas hoy.")
                else -> throw Exception("Error desconocido: ${response.code()} ${response.message()}")
            }
        }
    }.flowOn(Dispatchers.IO)

}