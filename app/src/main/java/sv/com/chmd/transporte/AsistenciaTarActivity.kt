package sv.com.chmd.transporte

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import sv.com.chmd.transporte.composables.AlumnoAsistenciaManComposable
import sv.com.chmd.transporte.composables.AlumnoAsistenciaTarComposable
import sv.com.chmd.transporte.composables.AlumnoOtraRutaComposable
import sv.com.chmd.transporte.composables.AsistenciasComposable
import sv.com.chmd.transporte.composables.ConfirmarInasistenciaDialog
import sv.com.chmd.transporte.composables.ConfirmarProcesoCompletoDialog
import sv.com.chmd.transporte.composables.SearchBarAlumnos
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.AlumnoRutaDiferenteItem
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.services.LocalizacionService
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.ui.theme.CHMDTransporteTheme
import sv.com.chmd.transporte.util.nunitoBold
import sv.com.chmd.transporte.util.nunitoRegular
import sv.com.chmd.transporte.viewmodel.AsistenciaManViewModel
import sv.com.chmd.transporte.viewmodel.AsistenciaTarViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaTarActivity : TransporteActivity() {
    private val asistenciaViewModel: AsistenciaTarViewModel by viewModel()
    private val networkChangeReceiver: NetworkChangeReceiver by inject()
    private val sharedPreferences:SharedPreferences by inject()
    var lstAlumnos = mutableStateListOf<Asistencia>()
    var lstAlumnosOtraRuta = mutableStateListOf<AlumnoRutaDiferenteItem>()
    var totalNoProcesados = 0
    var ascensos:Int=0
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
        token = sharedPreferences.getString("token","")
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(
                Intent(
                    this@AsistenciaTarActivity,
                    LocalizacionService::class.java
                )
            )
        } else {
            startService(Intent(this@AsistenciaTarActivity, LocalizacionService::class.java))
        }
        setContent {
            CHMDTransporteTheme {
                idRuta = intent.getStringExtra("idRuta")
                nombreRuta = intent.getStringExtra("nombreRuta")
                getAsistencia()

                if(hayConexion())
                    getAlumnosOtraRuta()

                AsistenciaScreen(idRuta)
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

    fun getAsistencia(){

        if(hayConexion()) {
            asistenciaViewModel.getAsistencia(idRuta.toString(), token!!,
                onSuccess = { it ->
                    lstAlumnos.clear()
                    lstAlumnos.addAll(it)
                    try {
                        lstAlumnos.removeAll { it.asistencia.toInt() == 0 }
                    }catch (_:Exception){}
                    ascensos = it.count { it.ascenso_t == "1" && it.salida.toInt() < 2 }
                    inasistencias = it.count { it.ascenso_t == "2" && it.descenso_t == "2" }
                    totalidad = it.count { it.asistencia == "1" }
                }, onError = {}
            )
        }else{
            //Offline
            lstAlumnos.clear()
            CoroutineScope(Dispatchers.IO).launch {
                val db = TransporteDB.getInstance(this@AsistenciaTarActivity)
                val data = db.iAsistenciaDAO.getAsistenciaTarde(idRuta.toString())
                withContext(Dispatchers.Main){

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
                    lstAlumnos.removeAll { it.asistencia.toInt() == 0 }
                    ascensos = lstAlumnos.count { it.ascenso_t == "1" && it.salida.toInt() < 2 }
                    inasistencias = lstAlumnos.count { it.ascenso_t == "2" && it.descenso_t == "2" }
                    totalidad = lstAlumnos.count { it.asistencia == "1" }
                }

            }
        }

    }

    @Composable
    @Preview(showBackground = true)
    fun AsistenciaScreen(idRuta: String? = "0") {
        var searchText by remember { mutableStateOf("") }
        var showInasistDialog by remember { mutableStateOf(false) }
        var nombreEstudiante by remember { mutableStateOf("") }
        var idAlumnoInasist by remember { mutableStateOf("") }
        var showMessageDialog by remember { mutableStateOf(false) }
        var showOtrasRutasDialog by remember { mutableStateOf(false) }
        var showSubirTodosDialog by remember { mutableStateOf(false) }
        var filteredList:List<Asistencia>
        if(searchText.isNotEmpty()) {
            filteredList = lstAlumnos.filter {
                it.nombre.contains(searchText, ignoreCase = true)
            }
        }else{
            filteredList = lstAlumnos
        }

        Scaffold(
            topBar = { ToolbarAlumnos(onBackClick = {
                Intent(this@AsistenciaTarActivity, SeleccionRutaActivity::class.java).also {
                    startActivity(it)
                }
            }, onUpdateClick = {
                getAsistencia() },

                onOtrasRutasClick = {
                    showOtrasRutasDialog = true
                },

                onUpClick = {
                    showSubirTodosDialog = true

            }, onMessageClick = {
                    showMessageDialog = true
                })
            }
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
                    val inasistencias =
                        lstAlumnos.count { it.ascenso_t == "2" && it.descenso_t == "2" }
                    val salio = lstAlumnos.count { it.salida.toInt() == 2 }
                    val porSalir = lstAlumnos.count { it.salida.toInt() == 3 }
                    val totalAlumnos =
                        lstAlumnos.count { it.asistencia != "0" } - inasistencias - salio - porSalir
                    SearchBarAlumnos(searchText,
                        onSearchTextChange = { searchText = it }
                    )
                    AsistenciasComposable(
                        ascensos.toString(),
                        totalAlumnos.toString(),
                        inasistencias.toString()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        itemsIndexed(filteredList
                            /*filteredList.sortedWith(
                                compareByDescending<Asistencia> { it.orden_out!!.toInt() > 900 }            // Prioridad para los que tienen orden_out > 900
                                    .thenBy { it.salida.toInt() }
                                    .thenBy { it.ascenso_t.toInt() }                                // Orden ascendente para ascenso_t
                                    .thenBy { it.descenso_t.toInt() }
                                    .thenBy { it.orden_out!!.toInt() }
                            )*/
                        ) { index, asistencia ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            val foto =
                                "http://chmd.chmd.edu.mx:65083/CREDENCIALES/alumnos/${asistencia.foto}"
                            Log.d("foto", foto)
                            AlumnoAsistenciaTarComposable(
                                asistencia.id_alumno,
                                asistencia.id_ruta_h_s,
                                asistencia.hora_regreso,
                                asistencia.nombre,
                                asistencia.orden_out,
                                asistencia.domicilio_s,
                                foto,
                                asistencia.ascenso_t,
                                asistencia.descenso_t,
                                asistencia.asistencia,
                                asistencia.salida,
                                modifier = Modifier,
                                onImageClick = { idAlumno, ruta ->


                                         val db =
                                            TransporteDB.getInstance(this@AsistenciaTarActivity)

                                    //Caso 1: Asistencia

                                    if (asistencia.ascenso_t == "0" && asistencia.asistencia != "0")

                                        if (hayConexion()) {
                                            asistenciaViewModel.enviarLocalizacionMovimiento(
                                                asistencia.id_ruta_h_s,
                                                "C" + sharedPreferences.getString(
                                                    "username",
                                                    ""
                                                )
                                                    .toString(),
                                                sharedPreferences.getString("latitude", "0.0")
                                                    .toString(),
                                                sharedPreferences.getString("longitude", "0.0")
                                                    .toString(),
                                                sharedPreferences.getString("speed", "0.0")
                                                    .toString(),
                                                "S",
                                                asistencia.id_alumno,
                                                onSuccess = {},
                                                onError = {})

                                            asistenciaViewModel.setAlumnoAsistencia(ruta,
                                                idAlumno,
                                                getCurrentTime(),
                                                onSuccess = {
                                                    Log.d("asistencia _al_", it)
                                                    getAsistencia()
                                                },
                                                onError = {
                                                    Log.e(
                                                        "asistencia _al_",
                                                        it.message.toString()
                                                    )
                                                }
                                            )

                                            val db =
                                                TransporteDB.getInstance(this@AsistenciaTarActivity)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Log.d("ASISTIR _tarde_", intent.getStringExtra("idRuta")!!)
                                                Log.d("ASISTIR _tarde_", asistencia.id_alumno)

                                                db.iAsistenciaDAO.asisteTurnoTar(
                                                    intent.getStringExtra("idRuta")!!,
                                                    asistencia.id_alumno, 1, getCurrentTime()
                                                )
                                            }

                                        } else {
                                            val db =
                                                TransporteDB.getInstance(this@AsistenciaTarActivity)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Log.d("ASISTIR _tarde_", asistencia.id_ruta_h_s)
                                                Log.d("ASISTIR _tarde_", asistencia.id_alumno)
                                                db.iAsistenciaDAO.asisteTurnoTar(
                                                    intent.getStringExtra("idRuta")!!,
                                                    asistencia.id_alumno, -1, getCurrentTime()
                                                )
                                            }
                                            Thread.sleep(1000)
                                            getAsistencia()
                                        }

                                    //Fin caso 1

                                    //Caso 2, subio, pero debe reiniciarse
                                    if (asistencia.ascenso_t == "1" && asistencia.asistencia == "1") {
                                        //Reiniciar asistencia en la tarde
                                        if (hayConexion()) {

                                            asistenciaViewModel.reiniciarAsistenciaTar(ruta,
                                                idAlumno,
                                                onSuccess = {
                                                    Log.d("asistencia _al_", it)
                                                    getAsistencia()
                                                },
                                                onError = {
                                                    Log.e(
                                                        "asistencia _al_",
                                                        it.message.toString()
                                                    )
                                                }
                                            )

                                            CoroutineScope(Dispatchers.IO).launch {

                                                db.iAsistenciaDAO.reiniciaAsistenciaTar(
                                                    intent.getStringExtra("idRuta")!!,
                                                    asistencia.id_alumno, 1, getCurrentTime()
                                                )
                                            }

                                        } else {
                                            val db =
                                                TransporteDB.getInstance(this@AsistenciaTarActivity)
                                            CoroutineScope(Dispatchers.IO).launch {

                                                db.iAsistenciaDAO.reiniciaAsistenciaTar(
                                                    intent.getStringExtra("idRuta")!!,
                                                    asistencia.id_alumno, -1, getCurrentTime()
                                                )
                                            }
                                            Thread.sleep(1000)
                                            getAsistencia()
                                        }
                                    }

                                    //Fin caso 2

                                    //Caso 3, se marca inasistencia, pero no la quiere
                                    if (asistencia.ascenso_t == "2" && asistencia.asistencia == "1") {
                                            if (hayConexion()) {

                                                asistenciaViewModel.reiniciarAsistenciaTar(ruta,
                                                    idAlumno,
                                                    onSuccess = {
                                                        Log.d("asistencia _al_", it)
                                                        getAsistencia()
                                                    },
                                                    onError = {
                                                        Log.e(
                                                            "asistencia _al_",
                                                            it.message.toString()
                                                        )
                                                    }
                                                )

                                                CoroutineScope(Dispatchers.IO).launch {

                                                    db.iAsistenciaDAO.reiniciaAsistenciaTar(
                                                        intent.getStringExtra("idRuta")!!,
                                                        asistencia.id_alumno, -1, getCurrentTime()
                                                    )
                                                }

                                            } else {
                                                val db =
                                                    TransporteDB.getInstance(this@AsistenciaTarActivity)
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    db.iAsistenciaDAO.reiniciaAsistenciaTar(
                                                        intent.getStringExtra("idRuta")!!,
                                                        asistencia.id_alumno, -1, getCurrentTime()
                                                    )
                                                }
                                                Thread.sleep(1000)
                                                getAsistencia()
                                            }
                                        }
                                    //Fin caso 3






                                },
                                onInasistenciaClick = {ruta, idAlumno ->
                                    idAlumnoInasist = filteredList.filter { it.nombre == asistencia.nombre }[0].id_alumno
                                    nombreEstudiante = asistencia.nombre
                                    showInasistDialog = true
                                }

                            )

                        }
                    }



                    Button(
                        onClick = {
                            val db = TransporteDB.getInstance(this@AsistenciaTarActivity)
                            if (ascensos == totalAlumnos) {
                                Toast.makeText(
                                    this@AsistenciaTarActivity,
                                    "Ya se han registrado todos los alumnos",
                                    Toast.LENGTH_LONG
                                ).show()

                                if(!hayConexion()){

                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.iRutaDAO.cambiaEstatusRuta(estatus = "1", offline = 1, idRuta = idRuta.toString())
                                    }
                                    Intent(
                                        this@AsistenciaTarActivity,
                                        SeleccionRutaActivity::class.java
                                    ).also {
                                        startActivity(it)
                                    }
                                }else {

                                    CoroutineScope(Dispatchers.IO).launch {
                                        val alumnosSP = db.iAsistenciaDAO.getAsistenciaSP().size
                                        if(alumnosSP>0){
                                            withContext(Dispatchers.Main){
                                                Toast.makeText(this@AsistenciaTarActivity,"No se puede cerrar todavía, hay registros pendientes de procesar",Toast.LENGTH_LONG).show()
                                                return@withContext
                                            }
                                        }

                                    }

                                    asistenciaViewModel.cerrarRuta(idRuta.toString(),
                                        "1",
                                        onSuccess = {
                                            val db =
                                                TransporteDB.getInstance(this@AsistenciaTarActivity)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                db.iRutaDAO.cambiaEstatusRuta(
                                                    estatus = "1",
                                                    offline = 0,
                                                    idRuta = idRuta.toString()
                                                )
                                            }

                                            Intent(
                                                this@AsistenciaTarActivity,
                                                SeleccionRutaActivity::class.java
                                            ).also {
                                                startActivity(it)
                                            }
                                        },
                                        onError = {})
                                }
                            } else {
                                Toast.makeText(
                                    this@AsistenciaTarActivity, "No se puede cerrar todavía",
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

            ConfirmarInasistenciaDialog(
                isOpen = showInasistDialog,
                nombre = nombreEstudiante,
                onDismiss = { showInasistDialog = false },
                onAccept = {
                    if(hayConexion()) {

                        val db = TransporteDB.getInstance(this@AsistenciaTarActivity)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.iAsistenciaDAO.noAsisteTurnoTar(idRuta!!,idAlumnoInasist,-1,getCurrentTime())
                        }

                        asistenciaViewModel.setAlumnoInasistencia(
                            idAlumnoInasist,
                            idRuta.toString(),
                            onSuccess = {
                                Log.d("asistencia _al_", it)
                                showInasistDialog = false
                                getAsistencia()
                            },
                            onError = {
                                showInasistDialog = false
                            })
                    }else{
                        val db = TransporteDB.getInstance(this@AsistenciaTarActivity)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.iAsistenciaDAO.noAsisteTurnoTar(idRuta!!,idAlumnoInasist,-1,getCurrentTime())
                        }
                        Thread.sleep(1000)
                        getAsistencia()
                    }
                }
            )


        }

        //Dialogos
        if (showOtrasRutasDialog) {
            AlumnosRutaDiferenteDialog(
                isOpen = showOtrasRutasDialog,
                onDismiss = { showOtrasRutasDialog = false },
                alumnos = lstAlumnosOtraRuta
            )
        }

        ConfirmarProcesoCompletoDialog(
            isOpen = showSubirTodosDialog,
            mensaje = "¿Deseas efectuar la subida completa?",
            onDismiss = { showSubirTodosDialog = false },
            onAccept = {
                if(hayConexion()) {
                    val db = TransporteDB.getInstance(this@AsistenciaTarActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.subenTodosTurnoTar(idRuta.toString(), 1, getCurrentTime())
                    }
                    asistenciaViewModel.subirTodos(idRuta.toString(),
                        onSuccess = { getAsistencia() },
                        onError = {}

                    )
                }else{
                    val db = TransporteDB.getInstance(this@AsistenciaTarActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.subenTodosTurnoTar(idRuta.toString(), -1, getCurrentTime())
                    }
                }
                showSubirTodosDialog = false
            }
        )


        ConfirmarInasistenciaDialog(
            isOpen = showInasistDialog,
            nombre = nombreEstudiante,
            onDismiss = { showInasistDialog = false },
            onAccept = {
                if(hayConexion()) {
                    asistenciaViewModel.setAlumnoInasistencia(
                        idAlumnoInasist,
                        idRuta.toString(),
                        onSuccess = {
                            val db = TransporteDB.getInstance(this@AsistenciaTarActivity)
                            CoroutineScope(Dispatchers.IO).launch {
                                db.iAsistenciaDAO.noAsisteTurnoTar(idRuta.toString(), idAlumnoInasist,-1, getCurrentTime())
                            }
                            Log.d("asistencia _al_", it)
                            showInasistDialog = false
                            getAsistencia()
                        },
                        onError = {
                            showInasistDialog = false
                        })
                }else{
                    val db = TransporteDB.getInstance(this@AsistenciaTarActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.noAsisteTurnoTar(idRuta.toString(), idAlumnoInasist,-1, getCurrentTime())
                    }
                    Thread.sleep(1000)
                    getAsistencia()
                }
            }
        )

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
        onUpClick: () -> Unit,
        onMessageClick: () -> Unit,
        onOtrasRutasClick: () -> Unit = {}
    ) {
        // Estado mutable para el total de no procesados
        var totalNoProcesados by remember { mutableStateOf(0) }

        // Actualiza el estado en un CoroutineScope
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val db = TransporteDB.getInstance(this@AsistenciaTarActivity) // Usa el contexto adecuado
                val data = db.iAsistenciaDAO.getAsistencia(idRuta.toString()).filter { it.procesado == -1 }
                totalNoProcesados = data.size
            }
        }

        TopAppBar(
            title = { Text(fontFamily = nunitoBold, text = "CHMD", color = Color.White) },
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

                IconButton(onClick = { onUpClick() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.rotate(90f)
                    )
                }

                IconButton(onClick = { onMessageClick() }) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Message",
                        tint = Color.White
                    )
                }

                // Mostrar el botón de advertencia con el badge si hay items no procesados


                // Mostrar icono de otras rutas si hay elementos
                if (lstAlumnosOtraRuta.isNotEmpty()) {
                    Box(modifier = Modifier.size(56.dp)) {
                        IconButton(onClick = { onOtrasRutasClick() }) {
                            Image(
                                painter = painterResource(id = R.drawable.ruta),
                                contentDescription = "Descripción de la imagen",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { onOtrasRutasClick() }
                            )
                        }

                        Badge(
                            modifier = Modifier
                                .padding(end = 6.dp, top = 2.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = if (lstAlumnosOtraRuta.size < 100) {
                                    lstAlumnosOtraRuta.size.toString()
                                } else {
                                    "99+"
                                }
                            )
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


}



