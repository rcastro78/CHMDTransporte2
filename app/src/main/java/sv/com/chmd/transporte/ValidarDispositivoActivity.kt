package sv.com.chmd.transporte

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sv.com.chmd.transporte.ui.theme.CHMDTransporteTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import retrofit2.awaitResponse
import sv.com.chmd.transporte.networking.ITransporte
import sv.com.chmd.transporte.services.LocalizacionService
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.util.nunitoBold
import sv.com.chmd.transporte.viewmodel.ValidarDispositivoViewModel
import java.util.UUID

class ValidarDispositivoActivity : ComponentActivity() {

    private val sharedPreferences: SharedPreferences by inject()
    private val validarDispositivoViewModel: ValidarDispositivoViewModel by inject()
    private val networkChangeReceiver: NetworkChangeReceiver by inject()

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkChangeReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CHMDTransporteTheme {
                val mId = Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                sharedPreferences.edit().putString("androidId", mId).apply()
                val aux_id = sharedPreferences.getString("aux_id", "")!!
                val (manufacturer, model) = getDeviceNameAndModel()
                ValidacionDispositivoScreen(aux_id, mId, manufacturer, model)
            }
        }
    }

    @Composable
    fun ValidacionDispositivoScreen(
        aux_id: String = "",
        mId: String = "",
        manufacturer: String = "",
        model: String = ""
    ) {
        var estado by remember { mutableStateOf("") }
        var hasNavigatedToMain by remember { mutableStateOf(false) }

        validarDispositivoViewModel.registraDispositivo("", mId, "$manufacturer $model",
            onRegisterSuccessful = {
                // Registro exitoso
            },
            onValidDeviceSuccessful = {
                // Dispositivo válido
            },
            onNotRegistered = {
                // Dispositivo no registrado
            },
            onInvalidDevice = {
                // Dispositivo inválido
            },
            onError = {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@ValidarDispositivoActivity,
                        "Error al validar el dispositivo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        validarDispositivoViewModel.getDispositivoValido(
            mId,
            onSuccessful = {
                estado = it.access

                if (it.access == "granted" && !hasNavigatedToMain) {
                    hasNavigatedToMain = true
                    sharedPreferences.edit().putString("token", it.token).apply()
                    Intent(
                        this@ValidarDispositivoActivity,
                        MainActivity::class.java
                    ).also { intent ->
                        startActivity(intent)
                        finish()
                    }
                }

                if (it.access in listOf("denied", "-1", "-2")) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val message = when (it.access) {
                            "denied" -> "No tienes acceso: fuera de horario laboral"
                            "-1" -> "No tienes acceso"
                            "-2" -> "Sin acceso: comunica este código a soporte técnico: $mId"
                            else -> ""
                        }
                        //Toast.makeText(this@ValidarDispositivoActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onError = {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@ValidarDispositivoActivity,
                        "Error al validar el dispositivo",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )

            when (estado) {
                "denied" -> {
                    Image(
                        painter = painterResource(id = R.drawable.bomb),
                        contentDescription = null,
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                            .padding(top = 16.dp)
                    )
                }
                "-2" -> {
                    Image(
                        painter = painterResource(id = R.drawable.stop),
                        contentDescription = null,
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                            .padding(top = 16.dp)
                    )
                }
                else -> {
                    Text(
                        text = "Validando dispositivo...",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 72.dp)
                    )

                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .align(Alignment.BottomCenter)
                    .alpha(1f)
            ) {
                val mensaje = when (estado) {
                    "denied" -> "No estás dentro del horario de trabajo: $mId"
                    "-1" -> "No tienes acceso: $mId"
                    "-2" -> "Comunica este código a soporte técnico: $mId"
                    "granted" -> ""
                    else -> ""
                }

                Text(
                    text = mensaje,
                    fontSize = 20.sp,
                    fontFamily = nunitoBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                )

                Button(
                    onClick = { finish() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.textoMasOscuro),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp, vertical = 24.dp)
                        .background(colorResource(id = R.color.textoMasOscuro), shape = RoundedCornerShape(4.dp))
                ) {
                    Text(text = "Cerrar")
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewValidacionDispositivoScreen() {
        ValidacionDispositivoScreen()
    }

    private fun getDeviceNameAndModel(): Pair<String, String> {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return Pair(manufacturer, model)
    }
}
