package sv.com.chmd.transporte.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import sv.com.chmd.transporte.services.LocalizacionService

class LocalizationViewModel(): ViewModel() {
    fun startLocalizacionService(context: Context) {
        val intent = Intent(context, LocalizacionService::class.java)
        Log.d("Localizacion", "Iniciando servicio de localización")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopLocalizacionService(context: Context) {
        Log.d("Localizacion", "Deteniendo servicio de localización")
        val intent = Intent(context, LocalizacionService::class.java)
        context.stopService(intent)
    }
}