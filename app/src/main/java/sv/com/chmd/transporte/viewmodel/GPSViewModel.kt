package sv.com.chmd.transporte.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class GPSViewModel(private val context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val _isGpsEnabled = mutableStateOf(isGpsActive())
    val isGpsEnabled: State<Boolean> get() = _isGpsEnabled

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            _isGpsEnabled.value = isGpsActive()
        }
    }

    init {
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        appContext.registerReceiver(locationReceiver, filter)
    }

    private fun isGpsActive(): Boolean {
        val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onCleared() {
        super.onCleared()
        appContext.unregisterReceiver(locationReceiver)
    }
}