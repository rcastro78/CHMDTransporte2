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
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import sv.com.chmd.transporte.composables.AlumnoAsistenciaManComposable
import sv.com.chmd.transporte.composables.AsistenciasComposable
import sv.com.chmd.transporte.composables.ConfirmarInasistenciaDialog
import sv.com.chmd.transporte.composables.SearchBarAlumnos
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.services.LocalizacionService
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.ui.theme.CHMDTransporteTheme
import sv.com.chmd.transporte.util.nunitoBold
import sv.com.chmd.transporte.util.nunitoRegular
import sv.com.chmd.transporte.viewmodel.AsistenciaManViewModel
import sv.com.chmd.transporte.viewmodel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AsistenciaManActivity : TransporteActivity() {
    val asistenciaViewModel: AsistenciaManViewModel by viewModel()
    var lstAlumnos = mutableStateListOf<Asistencia>()
    var ascensos:Int=0
    var totalidad:Int=0
    var inasistencias:Int=0
    var idRuta: String? = ""
    var nombreRuta: String? = ""
    var token:String? = ""
    private val networkChangeReceiver: NetworkChangeReceiver by inject()
    val sharedPreferences: SharedPreferences by inject()
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
        token = sharedPreferences.getString("token", "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(
                Intent(
                    this@AsistenciaManActivity,
                    LocalizacionService::class.java
                )
            )
        } else {
            startService(Intent(this@AsistenciaManActivity, LocalizacionService::class.java))
        }
        setContent {
            CHMDTransporteTheme {
                idRuta = intent.getStringExtra("idRuta")
                nombreRuta = intent.getStringExtra("nombreRuta")
                getAsistencia()
                AsistenciaScreen(idRuta)
            }
        }
    }
/*
fun getAsistencia(){
    if(hayConexion()){
        asistenciaViewModel.getAsistenciaMan(idRuta.toString(), token!!,
            onSuccess = { it ->
                lstAlumnos.clear()
                lstAlumnos.addAll(it)
                val noVan = it.count { it.ascenso=="0" && it.descenso=="0" && it.asistencia=="0"}
                ascensos = it.count { it.ascenso == "1" && it.descenso == "0" }
                inasistencias =
                    it.count { it.ascenso == "2" && it.descenso == "2" } + it.count { it.asistencia == "0" }
                val noAsisten = it.count { it.asistencia == "0" }
                //totalidad = it.count { it.asistencia.toInt()>0 } - noAsisten
                totalidad = 0
            }, onError = {}
        )
    }else{
        //Traer la informacion desde la base de datos
        //Offline
        lstAlumnos.clear()
        CoroutineScope(Dispatchers.IO).launch {
            val db = TransporteDB.getInstance(this@AsistenciaManActivity)
            val data = db.iAsistenciaDAO.getAsistencia(idRuta.toString())
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
                ascensos = lstAlumnos.count { it.ascenso == "1" && it.descenso == "0" }
                inasistencias =
                    lstAlumnos.count { it.ascenso == "2" && it.descenso == "2" } + lstAlumnos.count { it.asistencia == "0" }
                val noAsisten = lstAlumnos.count { it.asistencia == "0" }
                //totalidad = lstAlumnos.count { it.asistencia.toInt()>0 } - noAsisten
                totalidad = 0
            }

        }
    }
}
*/

    fun getAsistencia() {
        if (hayConexion()) {
            CoroutineScope(Dispatchers.IO).launch {
                asistenciaViewModel.getAsistenciaMan(idRuta.toString(), token!!)
                    .catch { error ->
                        //Log.e("Error", error.message ?: "Error desconocido")
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@AsistenciaManActivity,"Ocurrió un error al tratar de obtener los datos",Toast.LENGTH_LONG).show()
                        }
                    }
                    .collect { it ->
                        lstAlumnos.clear()
                        lstAlumnos.addAll(it)

                        val noVan = it.count { it.ascenso == "0" && it.descenso == "0" && it.asistencia == "0" }
                        ascensos = it.count { it.ascenso == "1" && it.descenso == "0" }
                        inasistencias = it.count { it.ascenso == "2" && it.descenso == "2" } + it.count { it.asistencia == "0" }
                        totalidad = 0
                    }
            }
        } else {
            // Traer la información desde la base de datos (Offline)
            lstAlumnos.clear()
            CoroutineScope(Dispatchers.IO).launch {
                val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                val data = db.iAsistenciaDAO.getAsistencia(idRuta.toString())

                val asistenciaList = data.map { asistenciaDAO ->
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
                        tipo_asistencia = ""
                    )
                }

                withContext(Dispatchers.Main) {
                    lstAlumnos.addAll(asistenciaList)
                    ascensos = lstAlumnos.count { it.ascenso == "1" && it.descenso == "0" }
                    inasistencias = lstAlumnos.count { it.ascenso == "2" && it.descenso == "2" } + lstAlumnos.count { it.asistencia == "0" }
                    totalidad = 0
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
                Intent(this@AsistenciaManActivity, SeleccionRutaActivity::class.java).also {
                    startActivity(it)
                }
            }, onUpdateClick = { getAsistencia() }, onMessageClick = {
                showMessageDialog = true
            }, onUploadClick = {

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
                    SearchBarAlumnos(searchText,
                        onSearchTextChange = { searchText = it }
                    )
                    AsistenciasComposable(
                        lstAlumnos.count { it.ascenso == "1" && it.descenso == "0" }.toString(),
                        (lstAlumnos.count { it.asistencia.toInt()==1 && it.ascenso.toInt()<2}).toString(),
                        lstAlumnos.count { it.ascenso.toInt()==2 }.toString())
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        itemsIndexed(
                            filteredList.sortedWith(
                                compareByDescending<Asistencia> {it.asistencia.toInt()}
                                    .thenBy { it.ascenso.toInt() }            // Orden ascendente para ascenso
                                    .thenBy { it.descenso.toInt() }           // Orden ascendente para descenso
                                    .thenBy { it.orden_in!!.toInt() }            // Orden ascendente para ordenIn
                                    .thenBy { it.salida.toInt() }
                            )
                        ) { index, asistencia ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            val foto = "http://chmd.chmd.edu.mx:65083/CREDENCIALES/alumnos/${asistencia.foto}"
                            Log.d("foto", foto)
                            AlumnoAsistenciaManComposable(
                                asistencia.id_alumno,
                                asistencia.id_ruta_h,
                                asistencia.hora_manana,
                                asistencia.nombre,
                                asistencia.orden_in,
                                asistencia.domicilio,
                                foto,
                                asistencia.ascenso,
                                asistencia.descenso,
                                asistencia.asistencia,
                                modifier = Modifier,
                                onImageClick = {idAlumno, ruta ->
                                   if(asistencia.ascenso == "0" && asistencia.asistencia != "0")
                                       if(hayConexion()) {

                                           asistenciaViewModel.enviarLocalizacionMovimiento(asistencia.id_ruta_h,
                                               "C"+sharedPreferences.getString("username","").toString(),
                                               sharedPreferences.getString("latitude", "0.0").toString(),
                                               sharedPreferences.getString("longitude", "0.0").toString(),
                                               sharedPreferences.getString("speed", "0.0").toString(),
                                               "S",asistencia.id_alumno,
                                               onSuccess = {},
                                               onError = {})

                                           val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                                           CoroutineScope(Dispatchers.IO).launch {
                                               db.iAsistenciaDAO.asisteTurnoMan(idRuta!!,asistencia.id_alumno,1,getCurrentTime())
                                           }
                                           asistenciaViewModel.setAlumnoAsistencia(ruta,
                                               idAlumno,
                                               getCurrentTime(), token!!,
                                               onSuccess = {
                                                   Log.d("asistencia _al_", it)
                                                   getAsistencia()
                                               },
                                               onError = {
                                                   Log.e("asistencia _al_", it.message.toString())
                                               }
                                           )
                                       }else{
                                           val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                                           CoroutineScope(Dispatchers.IO).launch {
                                               Log.d("asistencia _al_",idRuta.toString())
                                               Log.d("asistencia _al_",asistencia.id_alumno)
                                               db.iAsistenciaDAO.asisteTurnoMan(idRuta!!,asistencia.id_alumno,-1,getCurrentTime())
                                           }
                                           Thread.sleep(1000)
                                           getAsistencia()

                                       }
                                    if((asistencia.ascenso == "1" && asistencia.descenso == "0") || asistencia.ascenso == "2")
                                        if(hayConexion()) {
                                            val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                db.iAsistenciaDAO.reiniciaAsistenciaMan(idRuta!!,asistencia.id_alumno,1,getCurrentTime())
                                            }
                                            asistenciaViewModel.reiniciaAsistencia(ruta,asistencia.id_alumno,
                                                onSuccess = {
                                                    Log.d("asistencia _al_",it)
                                                    getAsistencia()
                                                },
                                                onError = {
                                                    Log.e("asistencia _al_",it.message.toString())
                                                }
                                            )
                                        }else{
                                            val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                db.iAsistenciaDAO.reiniciaAsistenciaMan(idRuta!!,asistencia.id_alumno,-1,getCurrentTime())
                                            }
                                            Thread.sleep(1000)
                                            getAsistencia()
                                        }


                                    if(asistencia.ascenso == "2")
                                        asistenciaViewModel.reiniciaAsistencia(ruta,idAlumno,
                                            onSuccess = {
                                                Log.d("asistencia _al_",it)
                                                getAsistencia()
                                            },
                                            onError = {
                                                Log.e("asistencia _al_",it.message.toString())
                                            }
                                        )

                                },
                                onInasistenciaClick = {ruta, idAlumno ->
                                    idAlumnoInasist = filteredList.filter { it.nombre == asistencia.nombre }[0].id_alumno
                                    nombreEstudiante = asistencia.nombre
                                    showInasistDialog = true
                                }

                                )

                        }
                    }
                    //Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {

                        //val _ascensos = lstAlumnos.count { it.ascenso == "1" && it.descenso == "0" }
                        //val _totalidad = lstAlumnos.count { it.asistencia.toInt()>0 } - lstAlumnos.count { it.ascenso == "2" && it.descenso == "2" }

                        //Si hay conexion, verificar si ya se han registrado todos los alumnos
                        //Todos deben tener ascenso==1 y descenso==0, asistencia==1
                        var _ascensos=0
                        var _totalidad=0
                        if(hayConexion()){

                        }else{
                            _ascensos = lstAlumnos.count { it.ascenso == "1" && it.descenso == "0" }
                            _totalidad = lstAlumnos.count { it.asistencia.toInt()==1 && it.ascenso.toInt()<2}

                        }


                        if(_ascensos == _totalidad){
                            Toast.makeText(this@AsistenciaManActivity,"Ya se han registrado todos los alumnos",Toast.LENGTH_LONG).show()
                            val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                            if(!hayConexion()){
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.iRutaDAO.cambiaEstatusRuta(estatus = "1", offline = 1, idRuta = idRuta.toString())
                                }
                                Intent(
                                    this@AsistenciaManActivity,
                                    SeleccionRutaActivity::class.java
                                ).also {
                                    startActivity(it)
                                }
                            }else{
                                val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                                CoroutineScope(Dispatchers.IO).launch {
                                    val alumnosSP = db.iAsistenciaDAO.getAsistenciaSP().size
                                    if(alumnosSP>0){
                                        withContext(Dispatchers.Main){
                                            Toast.makeText(this@AsistenciaManActivity,"No se puede cerrar todavía, hay registros pendientes de procesar",Toast.LENGTH_LONG).show()
                                            return@withContext
                                        }
                                    }

                                }
                                asistenciaViewModel.cerrarRuta(idRuta.toString(),"1", onSuccess = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.iRutaDAO.cambiaEstatusRuta(estatus = "1", offline = 0, idRuta = idRuta.toString())
                                    }

                                    Intent(this@AsistenciaManActivity, SeleccionRutaActivity::class.java).also {
                                        startActivity(it)
                                    }
                                },
                                    onError = {
                                        CoroutineScope(Dispatchers.Main).launch{
                                            Toast.makeText(this@AsistenciaManActivity,"No se pudo cerrar la ruta, consulta con IT",Toast.LENGTH_LONG).show()
                                        }
                                    })
                            }



                        }else{
                            Toast.makeText(this@AsistenciaManActivity,"No se puede cerrar todavía",Toast.LENGTH_LONG).show()
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


        ConfirmarInasistenciaDialog(
            isOpen = showInasistDialog,
            nombre = nombreEstudiante,
            onDismiss = { showInasistDialog = false },
            onAccept = {
                if(hayConexion()) {

                    val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.noAsisteTurnoMan(idRuta!!,idAlumnoInasist,-1,getCurrentTime())
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
                    val db = TransporteDB.getInstance(this@AsistenciaManActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.noAsisteTurnoMan(idRuta!!,idAlumnoInasist,-1,getCurrentTime())
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
        onMessageClick: () -> Unit,
        onUploadClick: () -> Unit
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
                /*
                Box(modifier = Modifier.size(56.dp)
                ) {
                    IconButton(onClick = { onUploadClick() }) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Message",
                            tint = Color.White
                        )
                    }

                    Badge(
                        modifier = Modifier
                            .padding(end = 6.dp, top = 2.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(text = "99+")

                    }
                }*/


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



}



