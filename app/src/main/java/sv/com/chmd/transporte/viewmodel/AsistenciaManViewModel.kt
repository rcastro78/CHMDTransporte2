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

    fun getAsistenciaMan(idRuta:String, token:String, onSuccess: (List<Asistencia>) -> Unit,onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.getAsistenciaMan(idRuta, token).awaitResponse()

            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }
        }
    }

    fun getAsistenciaManBajar(idRuta:String, token:String, onSuccess: (List<Asistencia>) -> Unit,onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.getAsistenciaManBajar(idRuta, token).awaitResponse()

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

    fun setAlumnoAsistencia(idRuta:String,idAlumno:String, hora:String, token:String,
                            onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.asistenciaAlumnoMan(idRuta, idAlumno, hora, token).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            } else {
                onError(Throwable(response.message()))
            }

        }
    }


    fun setAlumnoBajada(idRuta:String,idAlumno:String, token:String,
                        onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.descensoAlumnoMan(idRuta, idAlumno, token).awaitResponse()
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
            val response = iTransporte.inasistenciaAlumnoMan(idRuta,idAlumno).awaitResponse()
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

    fun bajarTodos(idRuta:String,onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.bajarTodosMan(idRuta, getCurrentTime()).awaitResponse()
            if (response.isSuccessful) {
                val data = response.body()!!
                onSuccess(data)
            }else{
                onError(Throwable(response.message()))
            }
        }
    }

    fun reiniciaAsistencia(idRuta:String, idAlumno:String,
                           onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.reiniciaAsistenciaAlumnoMan(idRuta,idAlumno).awaitResponse()
            if(response.isSuccessful){
                val data = response.body()!!
                onSuccess(data)
            }else{
                onError(Throwable(response.message()))
            }

        }
    }


    fun reiniciaBajada(idRuta:String, idAlumno:String,
                       onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.reiniciarBajadaMan(idRuta,idAlumno).awaitResponse()
            if(response.isSuccessful){
                val data = response.body()!!
                onSuccess(data)
            }else{
                onError(Throwable(response.message()))
            }

        }
    }

    fun cerrarRuta(idRuta:String,estatus:String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporte.cerrarRuta(idRuta,estatus).awaitResponse()
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

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

}