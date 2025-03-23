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
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.AlumnoRutaDiferenteItem
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.networking.ITransporte
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaTarViewModel(
    private val iTransporte: ITransporte,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private fun <T> executeRequest(
        call: suspend () -> Response<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = call()
                if (response.isSuccessful) {
                    response.body()?.let(onSuccess) ?: onError(Throwable("Respuesta vacía"))
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

    fun getAsistencia(idRuta: String, token: String, onSuccess: (List<Asistencia>) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.getAsistenciaTar(idRuta, token).awaitResponse() }, onSuccess, onError)
    }

    fun getAsistencia(idRuta: String, token: String): Flow<List<Asistencia>> {
        return makeRequestFlow { iTransporte.getAsistenciaTar(idRuta, token).awaitResponse() }
    }

    fun getAsistenciaBajar(idRuta: String, token: String, onSuccess: (List<Asistencia>) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.getAsistenciaTarBajar(idRuta, token).awaitResponse() }, onSuccess, onError)
    }

    fun getAlumnosRutaDif(idRuta: String, onSuccess: (List<AlumnoRutaDiferenteItem>) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.getAlumnosRutaDiferente(idRuta).awaitResponse() }, onSuccess, onError)
    }

    fun setAlumnoAsistencia(idRuta: String, idAlumno: String, hora: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.asistenciaAlumnoTar(idRuta, idAlumno, hora).awaitResponse() }, onSuccess, onError)
    }

    fun setAlumnoInasistencia(idRuta: String, idAlumno: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.inasistenciaAlumnoTar(idRuta, idAlumno).awaitResponse() }, onSuccess, onError)
    }

    fun setAlumnoBajada(idRuta: String, idAlumno: String, hora: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.descensoAlumnoTar(idRuta, idAlumno, hora).awaitResponse() }, onSuccess, onError)
    }

    fun reiniciarAsistenciaTar(idRuta: String, idAlumno: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.reiniciaAsistenciaAlumnoTar(idRuta, idAlumno).awaitResponse() }, onSuccess, onError)
    }

    fun reiniciarBajada(idRuta: String, idAlumno: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.reiniciarBajada(idRuta, idAlumno).awaitResponse() }, onSuccess, onError)
    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun enviarComentario(idRuta: String, comentario: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.enviarComentario(idRuta, comentario).awaitResponse() }, onSuccess, onError)
    }

    fun getComentarios(idRuta: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({
            val response = iTransporte.getComentario(idRuta).awaitResponse()
            response.body()?.firstOrNull()?.comentario?.let { Response.success(it) } ?: Response.error(500, "Sin comentarios".toResponseBody())
        }, onSuccess, onError)
    }

    fun subirTodos(idRuta: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        val currentTime = getCurrentTime()
        executeRequest({ iTransporte.subirTodosTar(idRuta, currentTime).awaitResponse() }, onSuccess, onError)
    }

    fun enviarLocalizacionMovimiento(
        idRuta: String,
        idAux: String,
        latitud: String,
        longitud: String,
        velocidad: String,
        accion: String,
        idAlumno: String,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        executeRequest({ iTransporte.enviarRuta(idRuta, idAux, latitud, longitud, "0", velocidad, accion, idAlumno).awaitResponse() }, onSuccess, onError)
    }

    fun cerrarRuta(idRuta: String, estatus: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        executeRequest({ iTransporte.cerrarRutaTarde(idRuta, estatus).awaitResponse() }, onSuccess, onError)
    }
}
