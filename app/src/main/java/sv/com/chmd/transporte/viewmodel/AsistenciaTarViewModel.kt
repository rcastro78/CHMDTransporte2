package sv.com.chmd.transporte.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import sv.com.chmd.transporte.model.AlumnoRutaDiferenteItem
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.networking.ITransporte
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaTarViewModel(private val iTransporte: ITransporte,
                             private val sharedPreferences: SharedPreferences
) : ViewModel()  {
    fun getAsistencia(idRuta:String, token:String, onSuccess: (List<Asistencia>) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.getAsistenciaTar(idRuta, token).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }
        }
    }

    fun getAsistenciaBajar(idRuta:String, token:String, onSuccess: (List<Asistencia>) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.getAsistenciaTarBajar(idRuta, token).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }
        }
    }


    fun getAlumnosRutaDif(idRuta:String, onSuccess: (List<AlumnoRutaDiferenteItem>) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.getAlumnosRutaDiferente(idRuta).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }
        }
    }


    fun setAlumnoAsistencia(idRuta:String,idAlumno:String, hora:String,
                            onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.asistenciaAlumnoTar(idRuta, idAlumno, hora).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }

        }
    }

    fun setAlumnoInasistencia(idRuta:String,idAlumno:String,onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {

            Log.d("INASISTENCIA", "$idRuta $idAlumno")
            val response = iTransporte.inasistenciaAlumnoTar(idRuta,idAlumno).awaitResponse()
            if(response.isSuccessful) {
                val data = response.body()!!
                Log.d("INASISTENCIA", data.toString())
                onSuccess(data)
            }
            else{
                onError(Throwable(response.message()))
            }
        }
    }

    fun setAlumnoBajada(idRuta:String,idAlumno:String, hora: String,
                        onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.descensoAlumnoTar(idRuta, idAlumno, hora).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }

        }
    }

    fun reiniciarAsistenciaTar(idRuta:String,idAlumno:String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.reiniciaAsistenciaAlumnoTar(idRuta, idAlumno).awaitResponse()
            if(response.isSuccessful){
                val data = response.body()!!
                onSuccess(data)
            }else{
                onError(Throwable(response.message()))
            }

        }
    }

    fun reiniciarBajada(idRuta:String,idAlumno:String,
                        onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.reiniciarBajada(idRuta, idAlumno).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }

        }
    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun enviarComentario(idRuta:String,comentario:String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.enviarComentario(idRuta,comentario).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }
        }
    }

    fun getComentarios(idRuta:String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.getComentario(idRuta).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!![0].comentario
                onSuccess(data)
                Log.d("comentarios",data.toString())
            } else {
                onError(Throwable(response.message()))
            }
        }
    }

    fun subirTodos(idRuta:String,onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {

        val currentTime: String = getCurrentTime()
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.subirTodosTar(idRuta,currentTime).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }

        }
    }

    fun enviarLocalizacionMovimiento(idRuta:String,idAux:String,latitud:String,longitud:String,
                                 velocidad:String,accion:String, idAlumno: String,
                                 onSuccess: (String) -> Unit,
                                 onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.enviarRuta(idRuta,idAux,latitud,longitud, "0",velocidad,accion, idAlumno).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            }else{
                onError(Throwable(response.message()))
            }
        }


    }


    fun cerrarRuta(idRuta:String,estatus:String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.cerrarRutaTarde(idRuta,estatus).awaitResponse()
            Log.d("RESPONSE",response.code().toString())
            if(response.isSuccessful){
                val data = response.body()!!
                onSuccess(data)
                Log.d("RESPONSE",data)
            }else{
                onError(Throwable(response.message()))
            }


        }
    }
}