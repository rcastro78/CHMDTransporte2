package sv.com.chmd.transporte

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import sv.com.chmd.transporte.AsistenciaTarActivity
import sv.com.chmd.transporte.composables.AlumnoAsistenciaTarBajarComposable
import sv.com.chmd.transporte.composables.AlumnoAsistenciaTarComposable
import sv.com.chmd.transporte.composables.AlumnoOtraRutaComposable
import sv.com.chmd.transporte.composables.AsistenciasComposable
import sv.com.chmd.transporte.composables.AsistenciasDescensoComposable
import sv.com.chmd.transporte.composables.SearchBarAlumnos
import sv.com.chmd.transporte.composables.SlowNetworkScreen
import sv.com.chmd.transporte.db.AsistenciaDAO
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.AlumnoRutaDiferenteItem
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.services.LocalizacionService
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.ui.theme.CHMDTransporteTheme
import sv.com.chmd.transporte.util.nunitoBold
import sv.com.chmd.transporte.util.nunitoRegular
import sv.com.chmd.transporte.viewmodel.AsistenciaTarViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaTarBajarActivity : TransporteActivity() {
    private val asistenciaViewModel: AsistenciaTarViewModel by viewModel()
    private val networkChangeReceiver: NetworkChangeReceiver by inject()
    private val sharedPreferences: android.content.SharedPreferences by inject()
    var lstAlumnos = mutableStateListOf<Asistencia>()
    var lstAlumnosOtraRuta = mutableStateListOf<AlumnoRutaDiferenteItem>()
    var descensos:Int=0
    var totalidad:Int=0
    var inasistencias:Int=0
    var idRuta: String? = ""
    var nombreRuta: String? = ""
    var token:String?=""
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
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Intent(this@AsistenciaTarBajarActivity, SeleccionRutaActivity::class.java).also {
                    startActivity(it)
                }
                finish()
            }
        })
        token = sharedPreferences.getString("token", "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(
                Intent(
                    this@AsistenciaTarBajarActivity,
                    LocalizacionService::class.java
                )
            )
        } else {
            startService(Intent(this@AsistenciaTarBajarActivity, LocalizacionService::class.java))
        }
        setContent {
            CHMDTransporteTheme {
                idRuta = intent.getStringExtra("idRuta")
                nombreRuta = intent.getStringExtra("nombreRuta")
                val isSlowNetwork by transporteViewModel.isSlowNetwork.collectAsState()
                if (isSlowNetwork) {
                    SlowNetworkScreen(this)
                } else {
                    if(hayConexion())
                        getAlumnosOtraRuta()

                    getAsistencia()
                    AsistenciaScreen(idRuta)
                }
            }
        }
    }

    fun getAlumnosOtraRuta(){
        asistenciaViewModel.getAlumnosRutaDif(idRuta.toString(),
            onSuccess = {
                lstAlumnosOtraRuta.clear()
                lstAlumnosOtraRuta.addAll(it)
                Log.d("alumnos _or_",lstAlumnosOtraRuta.size.toString())
            },
            onError = {})
    }

    /*
    fun getAsistencia(){
        if(hayConexion()) {
            insertaRegistros(idRuta.toString(),"1","2")
            asistenciaViewModel.getAsistenciaBajar(idRuta.toString(), token.toString(),
                onSuccess = { it ->
                    lstAlumnos.clear()
                    lstAlumnos.addAll(it)
                    lstAlumnos.removeIf { it.ascenso_t == "2" && it.descenso_t == "2" }
                    //lstAlumnos.removeIf { it.salida == "2" }
                    //lstAlumnos.removeIf { it.salida == "3" }
                    val noVan =
                        lstAlumnos.count {it.asistencia == "0" }
                    totalidad = lstAlumnos.count { it.asistencia == "1" }

                }, onError = {}
            )
        }else{
            lstAlumnos.clear()
            CoroutineScope(Dispatchers.IO).launch {
                val db = TransporteDB.getInstance(this@AsistenciaTarBajarActivity)
                val data = db.iAsistenciaDAO.getAlumnosBajarTarde(idRuta.toString())
                withContext(Dispatchers.Main) {

                    val asistenciaList: Collection<Asistencia> = data.map { asistenciaDAO ->
                        Asistencia(
                            tarjeta = asistenciaDAO.tarjeta,
                            ascenso = asistenciaDAO.ascenso,
                            ascenso_t = asistenciaDAO.ascenso_t ?: "0",
                            asistencia = asistenciaDAO.asistencia,
                            descenso = asistenciaDAO.descenso,
                            descenso_t = asistenciaDAO.descenso_t,
                            domicilio = asistenciaDAO.domicilio,
                            domicilio_s = asistenciaDAO.domicilio_s,
                            estatus = "",  // Valor predeterminado
                            fecha = "",  // Valor predeterminado
                            foto = asistenciaDAO.foto,
                            grado = asistenciaDAO.grado,
                            grupo = asistenciaDAO.grupo,
                            hora_manana = asistenciaDAO.horaManana,
                            hora_regreso = asistenciaDAO.horaRegreso,
                            id_alumno = asistenciaDAO.idAlumno,
                            id_ruta_h = asistenciaDAO.idRuta,
                            id_ruta_h_s = "",  // Valor predeterminado
                            nivel = asistenciaDAO.nivel,
                            nombre = asistenciaDAO.nombreAlumno,
                            orden_in = asistenciaDAO.ordenIn,
                            orden_out = asistenciaDAO.ordenOut,
                            salida = asistenciaDAO.salida,
                            tipo_asistencia = ""  // Valor predeterminado
                        )
                    }
                    lstAlumnos.addAll(asistenciaList)
                    lstAlumnos.removeIf { it.ascenso_t == "2" && it.descenso_t == "2" }
                    lstAlumnos.removeIf { it.salida == "2" }
                    lstAlumnos.removeIf { it.salida == "3" }
                    val noVan =
                        lstAlumnos.count { it.ascenso_t == "0" && it.descenso_t == "0" && it.asistencia == "0" }
                    totalidad = lstAlumnos.count { it.asistencia == "1" } - noVan
                }
            }

        }
    }
*/

    fun getAsistencia() {
        if (hayConexion()) {
            insertaRegistros(idRuta.toString(), "1", "2")

            CoroutineScope(Dispatchers.IO).launch {
                asistenciaViewModel.getAsistenciaBajarFlow(idRuta.toString(), token!!)
                    .catch { e ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@AsistenciaTarBajarActivity,
                                "Error: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .collect { it ->
                        withContext(Dispatchers.Main) {
                            lstAlumnos.clear()
                            lstAlumnos.addAll(it)
                            lstAlumnos.removeIf { it.ascenso_t == "2" && it.descenso_t == "2" }

                            val noVan = lstAlumnos.count {
                                it.ascenso_t == "0" && it.descenso_t == "0" && it.asistencia == "0"
                            }
                            totalidad = lstAlumnos.count { it.asistencia == "1" } - noVan
                        }
                    }
            }

        } else {
            // 📴 Offline
            lstAlumnos.clear()
            CoroutineScope(Dispatchers.IO).launch {
                val db = TransporteDB.getInstance(this@AsistenciaTarBajarActivity)
                val data = db.iAsistenciaDAO.getAlumnosBajarTarde(idRuta.toString())

                withContext(Dispatchers.Main) {
                    val asistenciaList: Collection<Asistencia> = data.map { asistenciaDAO ->
                        Asistencia(
                            tarjeta = asistenciaDAO.tarjeta,
                            ascenso = asistenciaDAO.ascenso,
                            ascenso_t = asistenciaDAO.ascenso_t ?: "0",
                            asistencia = asistenciaDAO.asistencia,
                            descenso = asistenciaDAO.descenso,
                            descenso_t = asistenciaDAO.descenso_t,
                            domicilio = asistenciaDAO.domicilio,
                            domicilio_s = asistenciaDAO.domicilio_s,
                            estatus = "",
                            fecha = "",
                            foto = asistenciaDAO.foto,
                            grado = asistenciaDAO.grado,
                            grupo = asistenciaDAO.grupo,
                            hora_manana = asistenciaDAO.horaManana,
                            hora_regreso = asistenciaDAO.horaRegreso,
                            id_alumno = asistenciaDAO.idAlumno,
                            id_ruta_h = asistenciaDAO.idRuta,
                            id_ruta_h_s = "",
                            nivel = asistenciaDAO.nivel,
                            nombre = asistenciaDAO.nombreAlumno,
                            orden_in = asistenciaDAO.ordenIn,
                            orden_out = asistenciaDAO.ordenOut,
                            salida = asistenciaDAO.salida,
                            tipo_asistencia = "",
                            orden_especial = "0",
                            orden_in_1 = asistenciaDAO.ordenIn1,
                            orden_out_1 = asistenciaDAO.ordenOut1
                        )
                    }
                    lstAlumnos.addAll(asistenciaList)
                    lstAlumnos.removeIf { it.ascenso_t == "2" && it.descenso_t == "2" }
                    lstAlumnos.removeIf { it.salida == "2" || it.salida == "3" }

                    val noVan = lstAlumnos.count {
                        it.ascenso_t == "0" && it.descenso_t == "0" && it.asistencia == "0"
                    }
                    totalidad = lstAlumnos.count { it.asistencia == "1" } - noVan
                }
            }
        }
    }


    @Composable
    @Preview(showBackground = true)
    fun AsistenciaScreen(idRuta: String? = "0") {
        var searchText by remember { mutableStateOf("") }
        var showMessageDialog by remember { mutableStateOf(false) }
        var showOtrasRutasDialog by remember { mutableStateOf(false) }
        var showInasistDialog  by remember { mutableStateOf(false) }
        var nombreEstudiante by remember { mutableStateOf("") }
        var idAlumnoInasist by remember { mutableStateOf("") }

        val filteredList:List<Asistencia>
        if(searchText.isNotEmpty()) {
            filteredList = lstAlumnos.filter {
                it.nombre.contains(searchText, ignoreCase = true)
            }
        }else{
            filteredList = lstAlumnos
        }

        Scaffold(
            topBar = { ToolbarAlumnos(onBackClick = {
                Intent(this@AsistenciaTarBajarActivity, SeleccionRutaActivity::class.java).also {
                    startActivity(it)
                }
                finish()
            }, onUpdateClick = {
                getAsistencia() },
                onOtrasRutasClick = {
                    showOtrasRutasDialog = true
                },
                onMessageClick = {
                    showMessageDialog = true
                }) }
        ) { paddingValues ->


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {


                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = nombreRuta!!,
                        color = colorResource(R.color.textoMasOscuro),
                        fontSize = 24.sp,
                        fontFamily = nunitoBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SearchBarAlumnos(searchText,
                        onSearchTextChange = { searchText = it }
                    )
                    AsistenciasDescensoComposable(
                        descensos = lstAlumnos.count { it.ascenso_t == "1" && it.descenso_t=="1" }.toString(),
                        totalidad.toString(),
                        inasistencias.toString()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        itemsIndexed(
                            filteredList.sortedWith(
                                compareByDescending<Asistencia> { it.ascenso_t.toInt() }
                                    .thenByDescending { if ((it.orden_out?.toIntOrNull() ?: 0) > 900) 0 else 1 } // Priorizar orden_out > 900
                                    .thenBy { it.salida.toInt() } // Orden ascendente por salida
                                    .thenByDescending { it.orden_out?.toIntOrNull() ?: 0 } // Orden descendente por orden_out
                                    .thenBy { it.ascenso_t.toInt() } // Orden ascendente por ascenso_t
                                    .thenBy { it.descenso_t.toInt() } // Orden ascendente por descenso_t
                                    .thenBy { it.id_alumno.toInt() } // Orden ascendente por id_alumno
                            )
                        ) { index, lst ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            val foto =
                                "http://chmd.chmd.edu.mx:65083/CREDENCIALES/alumnos/${lst.foto}"
                            Log.d("foto", foto)
                            AlumnoAsistenciaTarBajarComposable(
                                lst.id_alumno,
                                lst.id_ruta_h_s,
                                lst.hora_regreso,
                                lst.nombre,
                                lst.orden_out,
                                lst.orden_out_1,
                                lst.domicilio_s,
                                foto,
                                lst.ascenso_t,
                                lst.descenso_t,
                                lst.asistencia,
                                lst.salida,
                                lst.ascenso,
                                lst.descenso,
                                modifier = Modifier,
                                onImageClick = { idAlumno, ruta ->
                                    Log.d("BAJADA T", idAlumno)
                                    Log.d("BAJADA T", lst.ascenso_t)
                                    Log.d("BAJADA T", lst.descenso_t)
                                    Log.d("BAJADA T", lst.asistencia)

                                    if (lst.ascenso_t == "1" && lst.descenso_t == "0" && lst.asistencia != "0") {
                                        if (hayConexion()) {
                                            asistenciaViewModel.enviarLocalizacionMovimiento(lst.id_ruta_h_s,
                                                "C" + sharedPreferences.getString("username", "")
                                                    .toString(),
                                                sharedPreferences.getString("latitude", "0.0")
                                                    .toString(),
                                                sharedPreferences.getString("longitude", "0.0")
                                                    .toString(),
                                                sharedPreferences.getString("speed", "0.0")
                                                    .toString(),
                                                "B", lst.id_alumno,
                                                onSuccess = {},
                                                onError = {})


                                            asistenciaViewModel.setAlumnoBajada(ruta,
                                                idAlumno,
                                                getCurrentTime(),
                                                onSuccess = {
                                                    Log.d("asistencia _al_", it)

                                                    val db = TransporteDB.getInstance(this@AsistenciaTarBajarActivity)
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        db.iAsistenciaDAO.bajaTurnoTar(lst.id_ruta_h_s,lst.id_alumno,1,getCurrentTime())
                                                    }

                                                    getAsistencia()
                                                },
                                                onError = {
                                                    Log.e("asistencia _al_", it.message.toString())
                                                }
                                            )
                                        }else{
                                            //Toast.makeText(applicationContext,"Sin conexion",Toast.LENGTH_LONG).show()
                                            val db = TransporteDB.getInstance(this@AsistenciaTarBajarActivity)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                db.iAsistenciaDAO.bajaTurnoTar(sharedPreferences.getString("idRuta","").toString(),
                                                    lst.id_alumno,
                                                    -1,
                                                    getCurrentTime())
                                            }
                                            CoroutineScope(Dispatchers.Main).launch {
                                                delay(1000)
                                                getAsistencia()
                                            }
                                        }
                                    }


                                    if (lst.ascenso_t == "1" && lst.descenso_t == "1" && lst.asistencia != "0")
                                        if(hayConexion()) {
                                            asistenciaViewModel.reiniciarBajada(ruta,
                                                idAlumno,
                                                onSuccess = {
                                                    Log.d("asistencia _al_", it)
                                                    getAsistencia()
                                                },
                                                onError = {
                                                    Log.e("asistencia _al_", it.message.toString())
                                                }
                                            )
                                        }else{
                                            val db = TransporteDB.getInstance(this@AsistenciaTarBajarActivity)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                db.iAsistenciaDAO.reiniciaBajadaTar(
                                                    sharedPreferences.getString("idRuta","").toString(),
                                                    lst.id_alumno,
                                                    -1,
                                                    getCurrentTime())
                                            }
                                            CoroutineScope(Dispatchers.Main).launch {
                                                delay(1000)
                                                getAsistencia()
                                            }

                                        }




                                    /*if(asistencia.ascenso == "0" && asistencia.asistencia != "0")
                                        asistenciaViewModel.setAlumnoAsistencia(ruta,idAlumno,"15:23",
                                            onSuccess = {
                                                Log.d("asistencia _al_",it)
                                                getAsistencia()
                                            },
                                            onError = {
                                                Log.e("asistencia _al_",it.message.toString())
                                            }
                                        )
                                    if(asistencia.ascenso == "1" && asistencia.descenso == "0")
                                        asistenciaViewModel.reiniciaAsistencia(ruta,idAlumno,
                                            onSuccess = {
                                                Log.d("asistencia _al_",it)
                                                getAsistencia()
                                            },
                                            onError = {
                                                Log.e("asistencia _al_",it.message.toString())
                                            }
                                        )

                                    if(asistencia.ascenso == "2")
                                        asistenciaViewModel.reiniciaAsistencia(ruta,idAlumno,
                                            onSuccess = {
                                                Log.d("asistencia _al_",it)
                                                getAsistencia()
                                            },
                                            onError = {
                                                Log.e("asistencia _al_",it.message.toString())
                                            }
                                        )*/

                                },
                                onInasistenciaClick = { idAlumno, ruta ->
                                    idAlumnoInasist = filteredList.filter { it.nombre == lst.nombre }[0].id_alumno
                                    nombreEstudiante = lst.nombre
                                    showInasistDialog = true
                                }

                            )

                        }
                    }



                    Button(
                        onClick = {
                            val db = TransporteDB.getInstance(this@AsistenciaTarBajarActivity)
                            if (lstAlumnos.count { it.ascenso_t == "1" && it.descenso_t=="1" } == totalidad) {
                                Toast.makeText(
                                    this@AsistenciaTarBajarActivity,
                                    "Ya se han registrado todos los alumnos",
                                    Toast.LENGTH_LONG
                                ).show()

                                if(!hayConexion()){

                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.iRutaDAO.cambiaEstatusRuta(estatus = "2", offline = 1, idRuta = idRuta.toString())
                                    }
                                    Intent(
                                        this@AsistenciaTarBajarActivity,
                                        SeleccionRutaActivity::class.java
                                    ).also {
                                        startActivity(it)
                                    }
                                }else {

                                    CoroutineScope(Dispatchers.IO).launch {
                                        val alumnosSP = db.iAsistenciaDAO.getAsistenciaSP().size
                                        if(alumnosSP>0){
                                            withContext(Dispatchers.Main){
                                                Toast.makeText(this@AsistenciaTarBajarActivity,"No se puede cerrar todavía, hay registros pendientes de procesar",Toast.LENGTH_LONG).show()
                                                return@withContext
                                            }
                                        }

                                    }

                                    asistenciaViewModel.cerrarRuta(idRuta.toString(),
                                        "2",
                                        onSuccess = {

                                            val db =
                                                TransporteDB.getInstance(this@AsistenciaTarBajarActivity)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                db.iRutaDAO.cambiaEstatusRuta(
                                                    estatus = "2",
                                                    offline = 0,
                                                    idRuta = idRuta.toString()
                                                )
                                            }

                                            Intent(
                                                this@AsistenciaTarBajarActivity,
                                                SeleccionRutaActivity::class.java
                                            ).also {
                                                startActivity(it)
                                            }
                                        },
                                        onError = {it->
                                            CoroutineScope(Dispatchers.Main).launch {
                                                Toast.makeText(
                                                    this@AsistenciaTarBajarActivity,
                                                    "Error al cerrar la ruta: $it.message",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }

                                        })
                                }
                            } else {
                                Toast.makeText(
                                    this@AsistenciaTarBajarActivity, "No se puede cerrar todavía",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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
                            text = "Cerrar Ruta",
                            fontFamily = nunitoRegular
                        )
                    }

                }

            }

        }

        //Dialogos
        if (showOtrasRutasDialog) {
            AlumnosRutaDiferenteDialog(
                isOpen = showOtrasRutasDialog,
                onDismiss = { showOtrasRutasDialog = false },
                alumnos = lstAlumnosOtraRuta
            )
        }

        ComentarioRutaDialog(
            isOpen = showMessageDialog,
            onDismiss = { showMessageDialog = false },
            onAccept = { comment ->
                asistenciaViewModel.enviarComentario(idRuta.toString(),comment,onSuccess = {
                    showMessageDialog = false
                }, onError = {
                    showMessageDialog = false
                })
            }
        )

    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ToolbarAlumnos(
        onBackClick: () -> Unit,
        onUpdateClick: () -> Unit,
        onMessageClick: () -> Unit,
        onOtrasRutasClick:()-> Unit ={}
    ) {
        TopAppBar(
            title = { Text(
                fontFamily = nunitoBold,
                text = "CHMD", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { onUpdateClick() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Update",
                        tint = Color.White
                    )
                }


                IconButton(onClick = { onMessageClick() }) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Message",
                        tint = Color.White
                    )
                }

                if(lstAlumnosOtraRuta.isNotEmpty()) {

                    Box(modifier = Modifier.size(56.dp)
                    ) {


                        IconButton(onClick = { onOtrasRutasClick() }) {
                            Image(
                                painter = painterResource(id = R.drawable.ruta), // Reemplaza con tu drawable
                                contentDescription = "Descripción de la imagen",
                                modifier = Modifier
                                    .size(32.dp)// O el tamaño que desees
                                    .clickable(onClick = { onOtrasRutasClick() }) // Hacer clic en la imagen
                            )


                            Badge(
                                modifier = Modifier
                                    .padding(end = 6.dp, top = 2.dp)
                                    .align(Alignment.TopEnd) // Alineación en la esquina superior derecha
                            ) {
                                if(lstAlumnosOtraRuta.size<100)
                                    Text(text = lstAlumnosOtraRuta.size.toString())
                                else
                                    Text(text ="99+")

                            }
                        }
                    }
                }

            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(R.color.azulColegio),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )

        )



    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }


    @Composable
    fun ComentarioRutaDialog(
        isOpen: Boolean,
        onDismiss: () -> Unit,
        onAccept: (String) -> Unit
    ) {
        var comment by remember { mutableStateOf("") }

        if (isOpen) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Enviar comentarios",
                        color = colorResource(R.color.textoMasOscuro),
                        fontSize = 24.sp,
                        fontFamily = nunitoBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                },
                text = {
                    Column {
                        TextField(
                            value = comment,
                            onValueChange = { comment = it },
                            label = { Text("Escribe el comentario") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onAccept(comment)
                            onDismiss() // Cierra el diálogo al aceptar
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    Button(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }


    @Composable
    fun AlumnosRutaDiferenteDialog(
        isOpen: Boolean,
        onDismiss: () -> Unit,
        alumnos: List<AlumnoRutaDiferenteItem> // Aquí se pueden representar los alumnos como una lista de strings
    ) {
        if (isOpen) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(text = "CHMD",
                        fontFamily = nunitoBold,
                        fontSize = 24.sp,
                        color = colorResource(id = R.color.azulColegio))
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Encabezado
                        Text(
                            text = "Alumnos de otras rutas",
                            fontFamily = nunitoRegular,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            color = colorResource(id = R.color.azulColegio)
                        )

                        // Lista de alumnos
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            itemsIndexed(alumnos){index,alumno->
                                val foto = "http://chmd.chmd.edu.mx:65083/CREDENCIALES/alumnos/${alumno.foto}"
                                AlumnoOtraRutaComposable(alumno.id_alumno,
                                    foto,
                                    alumno.nombre,
                                    alumno.nombre_ruta,
                                    "Camión: "+alumno.camion)
                            }
                            /*items(alumnos) { alumno ->
                                Text(text = alumno, modifier = Modifier.padding(8.dp))
                            }*/
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                },
                properties = DialogProperties(usePlatformDefaultWidth = false) // Usar el ancho del contenido
            )
        }
    }

    fun insertaRegistros(id_ruta: String, estatus:String, turno:String){
        CoroutineScope(Dispatchers.IO).launch {
            val db = TransporteDB.getInstance(this@AsistenciaTarBajarActivity)
            if (estatus.toInt() < 2)
                db.iAsistenciaDAO.eliminaAsistencia(id_ruta)
            asistenciaViewModel.getAsistencia(id_ruta, "",
                onSuccess = { lstAsistencia ->
                    Log.d("SUBIR", lstAsistencia.size.toString())
                    lstAsistencia.forEach { alumno ->
                        var horaReg = ""
                        if (alumno.hora_regreso == null) {
                            horaReg = ""
                        } else {
                            horaReg = alumno.hora_regreso
                        }


                        var tarjeta = ""
                        if (alumno.tarjeta == null) {
                            tarjeta = ""
                        } else {
                            tarjeta = alumno.tarjeta
                        }


                        var orden_out = ""
                        if (alumno.orden_out_1 == null) {
                            orden_out = "0"
                        } else {
                            orden_out = alumno.orden_out_1
                        }

                        var orden_in = ""
                        if (alumno.orden_in == null) {
                            orden_in = "0"
                        } else {
                            orden_in = alumno.orden_in
                        }

                        var especial = "0"
                        if (orden_out.toInt() > 900 && alumno.salida.toInt() < 2) {
                            especial = "1"
                        }

                        val a = AsistenciaDAO(
                            0,
                            id_ruta,
                            tarjeta,
                            alumno.id_alumno,
                            alumno.nombre,
                            alumno.domicilio,
                            alumno.hora_manana,
                            horaReg,
                            alumno.ascenso,
                            alumno.descenso,
                            alumno.domicilio_s,
                            alumno.grupo,
                            alumno.grado,
                            alumno.nivel,
                            alumno.foto,
                            false,
                            false,
                            alumno.ascenso_t!!,
                            alumno.descenso_t,
                            alumno.salida,
                            orden_in,
                            orden_out,
                            false,
                            false,
                            0,
                            alumno.asistencia,
                            "",
                            especial,
                            alumno.salida,
                            alumno.orden_in_1.toString(),
                            alumno.orden_out_1.toString()
                        )
                        db.iAsistenciaDAO.guardaAsistencia(a)
                    }
                },
                onError = {
                    Log.d("ERROR", it.message.toString())
                })

        }
    }


}



