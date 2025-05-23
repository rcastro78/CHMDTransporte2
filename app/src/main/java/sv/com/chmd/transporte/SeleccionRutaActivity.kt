package sv.com.chmd.transporte

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import sv.com.chmd.transporte.composables.RutaItem
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.Ruta
import sv.com.chmd.transporte.model.RutaCamionItem
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.ui.theme.CHMDTransporteTheme
import sv.com.chmd.transporte.util.nunitoBold
import sv.com.chmd.transporte.util.nunitoRegular
import sv.com.chmd.transporte.viewmodel.LoginViewModel
import sv.com.chmd.transporte.viewmodel.SeleccionRutaViewModel

class SeleccionRutaActivity : TransporteActivity() {

    private val networkChangeReceiver: NetworkChangeReceiver by inject()
    private val sharedPreferences:SharedPreferences by inject()
    private val viewModel: SeleccionRutaViewModel by viewModel()
    override fun onResume() {
        super.onResume()

    }


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
        val auxiliar = sharedPreferences.getString("auxiliar","usuario")
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //El boton back no hace nada
            }
        })

        setContent {
            CHMDTransporteTheme {
                //lstRutas.clear()
                //getRutas()
               SeleccionRutaScreen(auxiliar!!)
            }
        }
    }

    @Composable
    fun SeleccionRutaScreen(auxiliar: String = "") {
        val rutas by viewModel.lstRutas.collectAsState()
        var isLoading by remember { mutableStateOf(true) }

        // Simula un pequeño delay para esperar que rutas se carguen
        LaunchedEffect(rutas) {
            if (isLoading) {
                delay(300)
                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                ToolbarSeleccion(
                    onUploadClick = {
                        Toast.makeText(
                            this@SeleccionRutaActivity,
                            "Hay datos no sincronizados, cuando tengas una conexión a internet se van a sincronizar",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        // ⏳ Mostrar loading mientras se inicializa la lista
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = colorResource(R.color.azulColegio)
                        )
                    }

                    rutas.isEmpty() -> {
                        // 🖼 Mostrar imagen y texto si no hay rutas
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.closed),
                                contentDescription = "Sin rutas",
                                modifier = Modifier.size(200.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay rutas disponibles en este momento",
                                fontSize = 18.sp,
                                fontFamily = nunitoRegular,
                                color = colorResource(R.color.textoMasOscuro),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        // ✅ Mostrar rutas disponibles
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Seleccionar Ruta",
                                color = colorResource(R.color.textoMasOscuro),
                                fontSize = 24.sp,
                                fontFamily = nunitoBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Bienvenido(a), $auxiliar",
                                color = colorResource(R.color.azulColegio),
                                fontSize = 14.sp,
                                fontFamily = nunitoRegular,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                itemsIndexed(rutas) { index, item ->
                                    if (index > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    RutaItem(
                                        rutaText = generarCodigoRuta(item.turno, item.tipo_ruta, item.camion) + "-" + item.nombre_ruta,
                                        tipoMovText = if (item.estatus == "0") "S" else "B",
                                        tipoRutaImage = if (item.turno == "1") R.drawable.am else R.drawable.pm,
                                        modifier = Modifier,
                                        onClick = {
                                            val context = this@SeleccionRutaActivity
                                            val idRuta = item.id_ruta_h
                                            val nombreRuta = item.nombre_ruta
                                            sharedPreferences.edit().putString("idRuta", idRuta).apply()

                                            val intent = when {
                                                item.turno == "1" && item.estatus == "0" -> Intent(context, AsistenciaManActivity::class.java)
                                                item.turno == "1" && item.estatus == "1" -> Intent(context, AsistenciaManBajarActivity::class.java)
                                                item.turno == "2" && item.estatus == "0" -> Intent(context, AsistenciaTarActivity::class.java)
                                                item.turno == "2" && item.estatus == "1" -> Intent(context, AsistenciaTarBajarActivity::class.java)
                                                else -> null
                                            }

                                            intent?.apply {
                                                putExtra("idRuta", idRuta)
                                                putExtra("nombreRuta", nombreRuta)
                                                context.startActivity(this)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ToolbarSeleccion(onUploadClick: () -> Unit = {}) {
        var totalNoProcesados by remember { mutableStateOf(0) }

        // Actualiza el estado en un CoroutineScope usando Dispatchers.IO
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val db = TransporteDB.getInstance(this@SeleccionRutaActivity) // Usa el contexto adecuado
                totalNoProcesados = db.iAsistenciaDAO.getTotalNoProcesados()
            }
        }
        TopAppBar(
            title = { Text(
                fontFamily = nunitoBold,
                text = "CHMD", color = Color.White) },
            navigationIcon = {},
            actions = {
                IconButton(onClick = {
                    val intent = Intent(this@SeleccionRutaActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Close Session")
                }


                /*if (totalNoProcesados > 0) {
                    Box(modifier = Modifier.size(56.dp)) {
                        IconButton(onClick = { onUploadClick() }) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Warning",
                                tint = Color.White
                            )
                        }

                        Badge(
                            modifier = Modifier
                                .padding(end = 6.dp, top = 2.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Text(text = totalNoProcesados.toString())
                        }
                    }
                }*/

            },
            colors = TopAppBarColors(containerColor = colorResource(R.color.azulColegio),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
                scrolledContainerColor = Color.Blue),



        )
    }



    private fun generarCodigoRuta(turno:String, tipo_ruta:String, camion:String):String{
        var trn = ""
        var truta = ""
        var cmn = ""
        if (turno.equals("1")) {
            trn = "M"
        }
        if (turno.equals("2")) {
            trn = "T"
        }
        if (tipo_ruta.equals("1")) {
            truta = "G"
        }
        if (tipo_ruta.equals("2")) {
            truta = "K"
        }
        if (tipo_ruta.equals("3")) {
            truta = "T"
        }
        if (tipo_ruta.equals("4")) {
            truta = "R"
        }

        cmn = camion
        return trn + truta + cmn

    }

}

