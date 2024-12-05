package sv.com.chmd.transporte.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import sv.com.chmd.transporte.db.TransporteDB
import java.util.concurrent.TimeUnit


class PendingDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    val db = TransporteDB.getInstance(context)

    override fun doWork(): Result {
        if (!isInternetAvailable()) {
            Log.d("InternetCheckWorker", "No hay conexión a Internet.")
        }
        val registrosPendientes = db.iAsistenciaDAO.getTotalNoProcesados()
        if(registrosPendientes > 0){
            Log.d("PendingDataWorker", "Hay $registrosPendientes registros pendientes de envío.")
        }else{
            Log.d("PendingDataWorker", "No hay registros pendientes de envío.")
        }
        scheduleNextCheck()
        return Result.success()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun scheduleNextCheck() {
        val workRequest = OneTimeWorkRequestBuilder<PendingDataWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS) // Esperar 15 segundos antes de la próxima ejecución
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}
