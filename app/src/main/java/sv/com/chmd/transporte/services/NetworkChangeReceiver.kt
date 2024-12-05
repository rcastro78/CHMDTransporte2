package sv.com.chmd.transporte.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.networking.ITransporte
import sv.com.chmd.transporte.networking.TransporteAPI
import java.lang.Exception

class NetworkChangeReceiver : BroadcastReceiver() {
    lateinit var iTransporteService: ITransporte

    override fun onReceive(context: Context, intent: Intent?) {
        val isConnected = checkInternet(context)
        val db = TransporteDB.getInstance(context)
        iTransporteService = TransporteAPI.getCHMDService()!!
        if(isConnected){


            try {
                CoroutineScope(Dispatchers.IO).launch {
                    db.iRutaDAO.getRutasCerradasOffline().forEach { ruta ->
                        procesarRutaCerrada(ruta.idRuta, ruta.estatus, db)
                    }


                    val alumnosProcesadosSinRed = db.iAsistenciaDAO.getAsistenciaSP()
                    alumnosProcesadosSinRed.forEach { alumno ->

                        if (alumno.ascenso.toInt() > 0 || alumno.descenso.toInt() > 0) {
                            //alumno procesado en la ruta de la mañana
                            //enviar al server
                            CoroutineScope(Dispatchers.IO).launch {
                                //Log.d("ALUMNO PROC MAN", alumno.nombreAlumno)
                                procesarAlumnosMan(
                                    db,
                                    alumno.idAlumno,
                                    alumno.idRuta,
                                    alumno.ascenso,
                                    alumno.descenso,
                                    alumno.horaRegistro
                                )
                            }
                        }
                        if (alumno.ascenso_t!!.toInt() > 0 || alumno.descenso_t.toInt() > 0) {
                            //alumno procesado en la ruta de la tarde
                            //enviar al server
                            CoroutineScope(Dispatchers.IO).launch {
                                Log.d("ALUMNO_PROC_TAR", alumno.nombreAlumno)
                                procesarAlumnosTar(
                                    db,
                                    alumno.idAlumno,
                                    alumno.idRuta,
                                    alumno.ascenso_t!!,
                                    alumno.descenso_t,
                                    alumno.horaRegistro
                                )
                            }
                        }
                    }

                }
            }catch (e:Exception){

            }

        }else{
            //Toast.makeText(context,"No está conectado",Toast.LENGTH_LONG).show()
        }
    }


    fun checkInternet(context: Context?): Boolean {
        val serviceManager = ServiceManager(context!!)
        return serviceManager.isNetworkAvailable
    }


    internal class ServiceManager(var context: Context) {
        val isNetworkAvailable: Boolean
            get() {
                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = cm.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }

    }


    fun procesarRutaCerrada(id_ruta:String,estatus:String,db:TransporteDB){
        CoroutineScope(Dispatchers.IO).launch {
            val response = iTransporteService.cerrarRuta(id_ruta,estatus).awaitResponse()
            if(response.isSuccessful){
                db.iRutaDAO.cambiaOffline(1,id_ruta)
            }
        }
    }


    fun procesarAlumnosMan(db:TransporteDB, id_alumno: String, id_ruta: String,ascenso:String,descenso:String,hora: String) {


        val call = iTransporteService.procesarMan(id_ruta, id_alumno,ascenso,descenso,hora)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code()==200) {
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("PROCESADO MAN OFFLINE", id_alumno)
                        db.iAsistenciaDAO.actualizaProcesados(id_ruta,id_alumno)
                    }
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {

                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })
    }

    fun procesarAlumnosTar(db:TransporteDB,id_alumno: String, id_ruta: String,ascenso:String,descenso:String,
    hora:String) {

       CoroutineScope(Dispatchers.IO).launch {
           val response = iTransporteService.procesarTar(id_ruta,id_alumno,ascenso, descenso,hora).awaitResponse()
           //Lo hace bien
           //val response = iTransporteService.asistenciaAlumnoTar(id_alumno,id_ruta).awaitResponse()
           if(response.isSuccessful){
               if(response.code()==200)

                   CoroutineScope(Dispatchers.IO).launch {
                       Log.d("PROCESADO TARDE OFFLINE", id_alumno)
                       db.iAsistenciaDAO.actualizaProcesados(id_ruta,id_alumno)
                   }
           }else{
               Log.d("ERROR-TRANSPORTE", response.code().toString())
           }

        }

        /*val call = iTransporteService.procesarTar(id_alumno, id_ruta,ascenso,descenso)
        call.enqueue(object : Callback<String> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.code()==200)
                CoroutineScope(Dispatchers.IO).launch {
                    db.iAsistenciaDAO.actualizaProcesados(id_ruta,id_alumno)
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {

                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })*/
    }

}