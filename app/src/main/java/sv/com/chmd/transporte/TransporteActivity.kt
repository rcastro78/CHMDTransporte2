package sv.com.chmd.transporte

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import org.koin.androidx.viewmodel.ext.android.viewModel
import sv.com.chmd.transporte.viewmodel.TransporteViewModel
import kotlin.getValue


open class TransporteActivity : ComponentActivity() {
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    private var mTime: Long = 1000 * 60 * 60  // Timer de 1 hora
    private lateinit var connectivityReceiver: ConnectivityReceiver
    val transporteViewModel: TransporteViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTimeOut()

        // Registrar el receptor para cambios en la conectividad
        connectivityReceiver = ConnectivityReceiver(transporteViewModel)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, filter)

        // Verificar conexión inicial al iniciar la actividad
        verificarConexion()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        stopHandler()
        startHandler()
    }

    fun obtenerHoraActual(): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    private fun startHandler() {
        mHandler.postDelayed(mRunnable, mTime)
    }

    private fun stopHandler() {
        mHandler.removeCallbacks(mRunnable)
    }

    private fun setTimeOut() {
        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable {
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                stopHandler()
            }
        }
        startHandler()
    }

    override fun onPause() {
        super.onPause()
        stopHandler()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHandler()

        // Desregistrar el receptor de conectividad
        unregisterReceiver(connectivityReceiver)
    }

    override fun onResume() {
        super.onResume()
        setTimeOut()
        startHandler()
        verificarConexion() // Verificar conexión al reanudar la actividad
    }

    fun hayConexion(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    private fun verificarConexion() {
        if (!hayConexion()) {
            mostrarDialogoSinConexion()
        }
    }

    private fun mostrarDialogoSinConexion() {
        // Crear y mostrar un diálogo para advertir al usuario
        AlertDialog.Builder(this).apply {
            setTitle("Sin conexión a Internet")
            setMessage("No se ha detectado una conexión a internet. Por favor, verifica tu red e inténtalo de nuevo.")
            setCancelable(false)
            setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss() // Cerrar el diálogo si el usuario lo acepta
            }
        }.show()
    }

    // Receptor para detectar cambios en la conectividad de la red
    private inner class ConnectivityReceiver(val viewModel: TransporteViewModel) : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!hayConexion()) {
                mostrarDialogoSinConexion() // Mostrar diálogo si no hay conexión
            }else{
                verificarRedLenta(context!!)
            }
        }

        @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
        private fun verificarRedLenta(context: Context) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> {
                    viewModel.setSlowNetwork(true)
                }

                else -> {
                    viewModel.setSlowNetwork(false)
                }
            }
        }
    }
}