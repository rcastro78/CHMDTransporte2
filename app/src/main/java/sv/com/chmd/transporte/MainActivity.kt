package sv.com.chmd.transporte

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import sv.com.chmd.transporte.db.AsistenciaDAO
import sv.com.chmd.transporte.db.RutaCamionDAO
import sv.com.chmd.transporte.db.RutaDAO
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.services.LocalizacionService
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.ui.theme.CHMDTransporteTheme
import sv.com.chmd.transporte.util.nunitoBold
import sv.com.chmd.transporte.util.nunitoRegular
import sv.com.chmd.transporte.viewmodel.AsistenciaManViewModel
import sv.com.chmd.transporte.viewmodel.AsistenciaTarViewModel
import sv.com.chmd.transporte.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModel()
    private val asistenciaManViewModel: AsistenciaManViewModel by viewModel()
    private val asistenciaTarViewModel: AsistenciaTarViewModel by viewModel()
    private val networkChangeReceiver: NetworkChangeReceiver by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    var versionName:String = ""
    var token:String = ""
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

        token = sharedPreferences.getString("token","")!!
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(this, "No se han otorgado permisos para acceder a la localización.", Toast.LENGTH_LONG).show()
                }
            }
        when {
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }


        versionName =  try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"
        }
        setContent {
            CHMDTransporteTheme {
                MainScreen()
            }
        }
    }

    @Composable
    @Preview(showBackground = true)
    fun MainScreen() {
        var username by remember {
            mutableStateOf(
                sharedPreferences.getString("username", "") ?: ""
            )
        }
        var password by remember { mutableStateOf("") }
        var rememberMe by remember { mutableStateOf(sharedPreferences.getBoolean("rememberMe", false)) } // Estado del Checkbox


        Scaffold(
            topBar = { ToolbarPrincipal() }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Transporte CHMD",
                        color = colorResource(R.color.textoMasOscuro),
                        fontSize = 28.sp,
                        fontFamily = nunitoBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = {
                            Text(
                                "Número de Camión",
                                fontFamily = nunitoRegular
                            )
                        }, // Aplicar la fuente a la etiqueta
                        textStyle = TextStyle(fontFamily = nunitoRegular),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle, // Icono de usuario de Material Icons
                                contentDescription = "Usuario",
                                tint = colorResource(id = R.color.azulColegio)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            if (isSystemInDarkTheme()) Color.White else Color.Black, // Fondo transparente
                            Color.Black, // Texto en negro
                            focusedIndicatorColor = Color.Transparent, // Sin línea inferior al enfocarse
                            unfocusedIndicatorColor = Color.Transparent, // Sin línea inferior sin enfocar
                            disabledIndicatorColor = Color.Transparent // Sin línea inferior cuando está deshabilitado
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))


                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(
                                "Contraseña",
                                fontFamily = nunitoRegular
                            )
                        }, // Aplicar la fuente a la etiqueta
                        textStyle = TextStyle(fontFamily = nunitoRegular),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock, // Icono de usuario de Material Icons
                                contentDescription = "Usuario",
                                tint = colorResource(id = R.color.azulColegio)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(), // Oculta el texto para las contraseñas
                        colors = TextFieldDefaults.colors(
                            if (isSystemInDarkTheme()) Color.White else Color.Black, // Fondo transparente
                            Color.Black, // Texto en negro
                            focusedIndicatorColor = Color.Transparent, // Sin línea inferior al enfocarse
                            unfocusedIndicatorColor = Color.Transparent, // Sin línea inferior sin enfocar
                            disabledIndicatorColor = Color.Transparent // Sin línea inferior cuando está deshabilitado
                        )
                    )


                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()

                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { checked ->
                                if(checked){
                                    sharedPreferences.edit().putBoolean("rememberMe",true).apply()
                                    sharedPreferences.edit().putString("username",username).apply()
                                }else{
                                    sharedPreferences.edit().remove("username").apply()
                                    sharedPreferences.edit().remove("rememberMe").apply()
                                }
                                rememberMe = checked
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Recordar usuario",
                            fontFamily = nunitoRegular)
                    }


                    Text(
                        text = "Descargando...",
                        color = colorResource(R.color.rojoInasistencia),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp)
                            .alpha(0f) // Inicialmente invisible
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(onClick = {

                    Log.d("Login", "Username: $username, Password: $password")
                    if(rememberMe)
                        sharedPreferences.edit().putString("username",username).apply()
                        //if(username.lowercase().contains("camion")) {
                        loginViewModel.getRutasCamion(username.lowercase().replace("camion", ""),
                            onSuccess = { lstRutas ->
                                val db = TransporteDB.getInstance(this@MainActivity)
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.iRutaDAO.delete()
                                    db.iAsistenciaDAO.eliminaAsistenciaCompleta()
                                    lstRutas.forEach { it ->
                                        CoroutineScope(Dispatchers.IO).launch {
                                            if (it.estatus.toInt() < 2 )
                                                    db.iAsistenciaDAO.eliminaAsistencia(it.id_ruta)
                                                        if(it.turno == "1")
                                                            asistenciaManViewModel.getAsistenciaMan(it.id_ruta, token,
                                                            onSuccess = { lstAsistencia ->
                                                            lstAsistencia.forEach { alumno ->
                                                            var orden_in=""
                                                            if(alumno.orden_in == null){
                                                                orden_in = "0"
                                                            }else{
                                                                orden_in = alumno.orden_in
                                                            }

                                                            var especial="0"

                                                            val a = AsistenciaDAO(0,it.id_ruta,alumno.tarjeta,alumno.id_alumno,
                                                                alumno.nombre,alumno.domicilio,alumno.hora_manana,"",
                                                                alumno.ascenso,alumno.descenso,alumno.domicilio_s,alumno.grupo,alumno.grado,
                                                                alumno.nivel,alumno.foto,false,false,alumno.ascenso_t!!,alumno.descenso_t,
                                                                alumno.salida,orden_in,"",false,false,0,alumno.asistencia,"",
                                                                especial,"",alumno.orden_in_1.toString(),
                                                                alumno.orden_out_1.toString()
                                                                )

                                                            db.iAsistenciaDAO.guardaAsistencia(a)
                                                        }
                                                    },
                                                    onError = {})

                                            if(it.turno == "2")
                                                asistenciaTarViewModel.getAsistencia(it.id_ruta, token,
                                                    onSuccess = { lstAsistencia ->
                                                        lstAsistencia.forEach { alumno ->
                                                            var horaReg=""
                                                            if(alumno.hora_regreso == null){
                                                                horaReg = ""
                                                            }else{
                                                                horaReg = alumno.hora_regreso
                                                            }


                                                            var tarjeta=""
                                                            if(alumno.tarjeta == null){
                                                                tarjeta = ""
                                                            }else{
                                                                tarjeta = alumno.tarjeta
                                                            }


                                                            var orden_out=""
                                                            if(alumno.orden_out == null){
                                                                orden_out = "0"
                                                            }else{
                                                                orden_out = alumno.orden_out
                                                            }

                                                            var orden_in=""
                                                            if(alumno.orden_in == null){
                                                                orden_in = "0"
                                                            }else{
                                                                orden_in = alumno.orden_in
                                                            }

                                                            var especial="0"
                                                            if(orden_out.toInt()>900 && alumno.salida.toInt()<2){
                                                                especial="1"
                                                            }

                                                            val a = AsistenciaDAO(0,it.id_ruta,tarjeta,alumno.id_alumno,
                                                                alumno.nombre,alumno.domicilio,alumno.hora_manana,horaReg,
                                                                alumno.ascenso,alumno.descenso,alumno.domicilio_s,alumno.grupo,alumno.grado,
                                                                alumno.nivel,alumno.foto,false,false,alumno.ascenso_t!!,alumno.descenso_t,
                                                                alumno.salida,
                                                                orden_in,
                                                                orden_out,
                                                                false,false,0,
                                                                alumno.asistencia,"",especial)
                                                            db.iAsistenciaDAO.guardaAsistencia(a)
                                                        }
                                                    },
                                                    onError = {})
                                             db.iRutaDAO.guardaRutas(

                                                    RutaDAO(
                                                        0,
                                                        it.id_ruta,
                                                        it.nombre_ruta,
                                                        it.camion,
                                                        it.turno,
                                                        it.tipo_ruta,
                                                        it.estatus,
                                                        0
                                                    )
                                                )
                                        }
                                    }

                                }


                                Intent(this@MainActivity, SeleccionRutaActivity::class.java).also {
                                    startActivity(it)
                                }

                            },
                            onError = {
                                Log.d("Error", it.message.toString())
                            })


                    },
                        colors = ButtonDefaults.buttonColors(
                            colorResource(id = R.color.azulColegio),  // Color de fondo del botón
                            contentColor = Color.White     // Color del texto o ícono del botón
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(start = 8.dp, end = 8.dp),

                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            fontFamily = nunitoRegular
                        )
                    }

                     Text(
                            text = "Versión de la app: "+versionName,
                            color = colorResource(R.color.azulColegio),
                            fontFamily = nunitoRegular,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 12.dp)

                        )

                }




            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ToolbarPrincipal() {

        TopAppBar(
            title = { Text(
                fontFamily = nunitoBold,
                text = "CHMD", color = Color.White) },
            navigationIcon = {},
            actions = {},
            colors = TopAppBarColors(containerColor = colorResource(R.color.azulColegio),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
                scrolledContainerColor = Color.Blue)
            /*navigationIcon = {
                IconButton(onClick = { /* Acción para el icono de navegación */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                IconButton(onClick = { /* Acción para el icono de acción */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
                IconButton(onClick = { /* Otra acción */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                }
            },*/

        )
    }

}



