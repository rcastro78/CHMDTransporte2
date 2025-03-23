package sv.com.chmd.transporte.repository


import retrofit2.Response
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.networking.ITransporte
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaManRepository(private val iTransporte: ITransporte) {

    private suspend fun <T> makeRequest(request: suspend () -> Response<T>): Result<T> {
        return try {
            val response = request()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Throwable("Respuesta vacía del servidor"))
            } else {
                Result.failure(Throwable("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAsistenciaMan(idRuta: String, token: String): Result<List<Asistencia>> {
        return makeRequest { iTransporte.getAsistenciaMan(idRuta, token).awaitResponse() }
    }

    suspend fun getAsistenciaManBajar(idRuta: String, token: String): Result<List<Asistencia>> {
        return makeRequest { iTransporte.getAsistenciaManBajar(idRuta, token).awaitResponse() }
    }

    suspend fun enviarLocalizacionMovimiento(
        idRuta: String, idAux: String, latitud: String, longitud: String,
        velocidad: String, accion: String, idAlumno: String
    ): Result<String> {
        return makeRequest { iTransporte.enviarRuta(idRuta, idAux, latitud, longitud, "0", velocidad, accion, idAlumno).awaitResponse() }
    }

    suspend fun setAlumnoAsistencia(idRuta: String, idAlumno: String, hora: String, token: String): Result<String> {
        return makeRequest { iTransporte.asistenciaAlumnoMan(idRuta, idAlumno, hora, token).awaitResponse() }
    }

    suspend fun setAlumnoBajada(idRuta: String, idAlumno: String, token: String): Result<String> {
        return makeRequest { iTransporte.descensoAlumnoMan(idRuta, idAlumno, token).awaitResponse() }
    }

    suspend fun setAlumnoInasistencia(idRuta: String, idAlumno: String): Result<String> {
        return makeRequest { iTransporte.inasistenciaAlumnoMan(idRuta, idAlumno).awaitResponse() }
    }

    suspend fun bajarTodos(idRuta: String): Result<String> {
        return makeRequest { iTransporte.bajarTodosMan(idRuta, getCurrentTime()).awaitResponse() }
    }

    suspend fun reiniciaAsistencia(idRuta: String, idAlumno: String): Result<String> {
        return makeRequest { iTransporte.reiniciaAsistenciaAlumnoMan(idRuta, idAlumno).awaitResponse() }
    }

    suspend fun reiniciaBajada(idRuta: String, idAlumno: String): Result<String> {
        return makeRequest { iTransporte.reiniciarBajadaMan(idRuta, idAlumno).awaitResponse() }
    }

    suspend fun cerrarRuta(idRuta: String, estatus: String): Result<String> {
        return makeRequest { iTransporte.cerrarRuta(idRuta, estatus).awaitResponse() }
    }

    suspend fun enviarComentario(idRuta: String, comentario: String): Result<String> {
        return makeRequest { iTransporte.enviarComentario(idRuta, comentario).awaitResponse() }
    }

    suspend fun getComentarios(idRuta: String): Result<String> {
        return makeRequest {
            iTransporte.getComentario(idRuta).awaitResponse()
        }.map { it.firstOrNull()?.comentario ?: "Sin comentarios" }
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }


}
