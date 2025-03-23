package sv.com.chmd.transporte.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.awaitResponse
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.networking.ITransporte
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaManViewModel(
    private val iTransporte: ITransporte,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private fun <T> makeRequest(
        request: suspend () -> Response<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = request()
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                        ?: onError(Throwable("Respuesta vacía del servidor"))
                } else {
                    onError(Throwable("Error ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private fun <T> makeRequestFlow(request: suspend () -> Response<T>): Flow<T> {
        return flow {
            val response = request()
            if (response.isSuccessful) {
                response.body()?.let { emit(it) }
                    ?: throw Throwable("Respuesta vacía del servidor")
            } else {
                throw Throwable("Error ${response.code()}: ${response.message()}")
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getAsistenciaMan(idRuta: String, token: String): Flow<List<Asistencia>> {
        return makeRequestFlow { iTransporte.getAsistenciaMan(idRuta, token).awaitResponse() }
    }

    fun getAsistenciaMan(idRuta: String, token: String, onSuccess: (List<Asistencia>) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.getAsistenciaManBajar(idRuta, token).awaitResponse() }, onSuccess, onError)
    }

    fun getAsistenciaManBajar(idRuta: String, token: String, onSuccess: (List<Asistencia>) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.getAsistenciaManBajar(idRuta, token).awaitResponse() }, onSuccess, onError)
    }

    fun getAsistenciaManBajar(idRuta: String, token: String):Flow<List<Asistencia>> {
        return makeRequestFlow{ iTransporte.getAsistenciaManBajar(idRuta, token).awaitResponse()}
    }



    fun enviarLocalizacionMovimiento(
        idRuta: String, idAux: String, latitud: String, longitud: String,
        velocidad: String, accion: String, idAlumno: String,
        onSuccess: (String) -> Unit, onError: (Throwable) -> Unit
    ) {
        makeRequest({ iTransporte.enviarRuta(idRuta, idAux, latitud, longitud, "0", velocidad, accion, idAlumno).awaitResponse() }, onSuccess, onError)
    }

    fun setAlumnoAsistencia(idRuta: String, idAlumno: String, hora: String, token: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.asistenciaAlumnoMan(idRuta, idAlumno, hora, token).awaitResponse() }, onSuccess, onError)
    }

    fun setAlumnoBajada(idRuta: String, idAlumno: String, token: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.descensoAlumnoMan(idRuta, idAlumno, token).awaitResponse() }, onSuccess, onError)
    }

    fun setAlumnoInasistencia(idRuta: String, idAlumno: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        Log.d("INASISTENCIA", "$idRuta $idAlumno")
        makeRequest({ iTransporte.inasistenciaAlumnoMan(idRuta, idAlumno).awaitResponse() }, {
            Log.d("INASISTENCIA", it)
            onSuccess(it)
        }, onError)
    }

    fun bajarTodos(idRuta: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.bajarTodosMan(idRuta, getCurrentTime()).awaitResponse() }, onSuccess, onError)
    }

    fun reiniciaAsistencia(idRuta: String, idAlumno: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.reiniciaAsistenciaAlumnoMan(idRuta, idAlumno).awaitResponse() }, onSuccess, onError)
    }

    fun reiniciaBajada(idRuta: String, idAlumno: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.reiniciarBajadaMan(idRuta, idAlumno).awaitResponse() }, onSuccess, onError)
    }

    fun cerrarRuta(idRuta: String, estatus: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.cerrarRuta(idRuta, estatus).awaitResponse() }, {
            Log.d("RESPONSE", it)
            onSuccess(it)
        }, onError)
    }

    fun enviarComentario(idRuta: String, comentario: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.enviarComentario(idRuta, comentario).awaitResponse() }, onSuccess, onError)
    }

    fun getComentarios(idRuta: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        makeRequest({ iTransporte.getComentario(idRuta).awaitResponse() }, {
            val comentario = it.firstOrNull()?.comentario ?: "Sin comentarios"
            Log.d("comentarios", comentario)
            onSuccess(comentario)
        }, onError)
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
