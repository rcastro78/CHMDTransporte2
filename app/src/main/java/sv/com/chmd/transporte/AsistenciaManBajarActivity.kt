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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import sv.com.chmd.transporte.composables.AlumnoAsistenciaManBajarComposable
import sv.com.chmd.transporte.composables.AlumnoAsistenciaManComposable
import sv.com.chmd.transporte.composables.AsistenciasComposable
import sv.com.chmd.transporte.composables.AsistenciasDescensoComposable
import sv.com.chmd.transporte.composables.ConfirmarProcesoCompletoDialog
import sv.com.chmd.transporte.composables.SearchBarAlumnos
import sv.com.chmd.transporte.db.TransporteDB
import sv.com.chmd.transporte.model.Asistencia
import sv.com.chmd.transporte.services.LocalizacionService
import sv.com.chmd.transporte.services.NetworkChangeReceiver
import sv.com.chmd.transporte.ui.theme.CHMDTransporteTheme
import sv.com.chmd.transporte.util.nunitoBold
import sv.com.chmd.transporte.util.nunitoRegular
import sv.com.chmd.transporte.viewmodel.AsistenciaManViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsistenciaManBajarActivity : TransporteActivity() {
    private val asistenciaViewModel: AsistenciaManViewModel by viewModel()
    private val networkChangeReceiver: NetworkChangeReceiver by inject()
    private val sharedPreferences:SharedPreferences by inject()
    var lstAlumnos = mutableStateListOf<Asistencia>()
    var descensos:Int=0
    var totalidad:Int=0
    var inasistencias:Int=0
    var idRuta: String? = ""
    var nombreRuta: String? = ""
    var token: String? = ""
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
        idRuta = intent.getStringExtra("idRuta")
        nombreRuta = intent.getStringExtra("nombreRuta")
        token = sharedPreferences.getString("token", "")
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(
                Intent(
                    this@AsistenciaManBajarActivity,
                    LocalizacionService::class.java
                )
            )
        } else {
            startService(Intent(this@AsistenciaManBajarActivity, LocalizacionService::class.java))
        }
        setContent {
            CHMDTransporteTheme {
                getAsistencia()
                AsistenciaScreen(idRuta)
            }
        }
    }

    fun getAsistencia(){
        if(hayConexion()) {
            asistenciaViewModel.getAsistenciaManBajar(idRuta.toString(), token!!,
                onSuccess = { it ->
                    lstAlumnos.clear()
                    lstAlumnos.addAll(it)
                    try {
                        lstAlumnos.removeAll { it.ascenso == "2" && it.descenso == "2" }
                    }catch (_:Exception){}
                    descensos = it.count { it.ascenso == "1" && it.descenso == "1" }
                    inasistencias = 0
                    totalidad = it.count { it.asistencia == "1" }
                }, onError = {}
            )
        }else{

            lstAlumnos.clear()
            CoroutineScope(Dispatchers.IO).launch {
                val db = TransporteDB.getInstance(this@AsistenciaManBajarActivity)
                val data = db.iAsistenciaDAO.getAsistencia(idRuta.toString())
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
                    lstAlumnos.removeAll { it.ascenso == "2" && it.descenso == "2" }
                    descensos = lstAlumnos.count { it.ascenso == "1" && it.descenso == "1" }
                    inasistencias = 0
                    totalidad = lstAlumnos.count { it.asistencia == "1" }
                }
            }




        }
    }

    @Composable
    @Preview(showBackground = true)
    fun AsistenciaScreen(idRuta: String? = "0") {
        var searchText by remember { mutableStateOf("") }
        var showTodosDialog by remember { mutableStateOf(false) }
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
            topBar = { ToolbarAlumnos(
                onBackClick = { onBackPressed() },
                onUpdateClick = { getAsistencia() },
                onDownClick = {
                    showTodosDialog = true
                    /*asistenciaViewModel.bajarTodos(idRuta.toString(),
                        onSuccess = {getAsistencia()},
                        onError = {}

                    )*/
                },
                onMessageClick = {showMessageDialog=true}) }
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
                        lstAlumnos.count { it.ascenso == "1" && it.descenso == "1" }.toString(),
                        (lstAlumnos.count { it.asistencia.toInt()==1 && it.ascenso.toInt()<2}).toString(),
                        inasistencias.toString())
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .weight(1f)
                    ) {

                        /*
                        .sortedWith(
                            compareBy<Asistencia> { it.orden_in!!.toInt() }
                                .thenBy { it.ascenso.toInt() }
                                .thenBy { it.salida.toInt() }
                                .thenBy { it.descenso_t.toInt() }
                                .thenBy { it.orden_in!!.toInt() }

                        )
                        * */

                        itemsIndexed(filteredList) { index, asistencia ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            val foto = "http://chmd.chmd.edu.mx:65083/CREDENCIALES/alumnos/${asistencia.foto}"
                            Log.d("foto", foto)
                            AlumnoAsistenciaManBajarComposable(
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
                                    val db = TransporteDB.getInstance(this@AsistenciaManBajarActivity)
                                   if(hayConexion()){
                                       if(asistencia.ascenso == "1") {
                                           asistenciaViewModel.setAlumnoBajada(ruta,
                                               idAlumno,
                                               token!!,
                                               onSuccess = {
                                                   Log.d("bajada _al_", it)
                                                   getAsistencia()
                                               },
                                               onError = {
                                                   Log.e("bajada _al_", it.message.toString())
                                               }
                                           )
                                           CoroutineScope(Dispatchers.IO).launch {
                                               db.iAsistenciaDAO.bajaTurnoMan(
                                                   asistencia.id_ruta_h,
                                                   asistencia.id_alumno,
                                                   1,
                                                   getCurrentTime()
                                               )
                                           }
                                       }
                                       if(asistencia.ascenso == "1" && asistencia.descenso == "1") {
                                           asistenciaViewModel.reiniciaBajada(ruta, idAlumno,
                                               onSuccess = {
                                                   Log.d("asistencia _al_", it)
                                                   getAsistencia()
                                               },
                                               onError = {
                                                   Log.e("asistencia _al_", it.message.toString())
                                               }
                                           )

                                           CoroutineScope(Dispatchers.IO).launch {
                                               db.iAsistenciaDAO.reiniciaBajadaMan(
                                                   asistencia.id_ruta_h,
                                                   asistencia.id_alumno,
                                                   1
                                               )
                                           }
                                       }

                                       Thread.sleep(1000)
                                       getAsistencia()

                                   }else{

                                       if(asistencia.ascenso == "1") {
                                           CoroutineScope(Dispatchers.IO).launch {
                                               db.iAsistenciaDAO.bajaTurnoMan(
                                                   asistencia.id_ruta_h,
                                                   asistencia.id_alumno,
                                                   -1,
                                                   getCurrentTime()
                                               )
                                           }
                                       }
                                       if(asistencia.ascenso == "1" && asistencia.descenso == "1") {
                                           CoroutineScope(Dispatchers.IO).launch {
                                               db.iAsistenciaDAO.reiniciaBajadaMan(
                                                   asistencia.id_ruta_h,
                                                   asistencia.id_alumno,
                                                   -1
                                               )
                                           }
                                       }

                                       Thread.sleep(1000)
                                       getAsistencia()

                                   }



                                }

                            )

                        }
                    }
                    //Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        if(descensos == lstAlumnos.count { it.asistencia != "0" }){
                            Toast.makeText(this@AsistenciaManBajarActivity,"Ya se han registrado todos los alumnos",
                                Toast.LENGTH_LONG).show()

                            if(!hayConexion()){
                                val db = TransporteDB.getInstance(this@AsistenciaManBajarActivity)
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.iRutaDAO.cambiaEstatusRuta(estatus = "2", offline = 1, idRuta = idRuta.toString())
                                }
                                Intent(this@AsistenciaManBajarActivity, SeleccionRutaActivity::class.java).also {
                                    startActivity(it)
                                }
                            }else {

                                asistenciaViewModel.cerrarRuta(idRuta.toString(), "2", onSuccess = {
                                    val db =
                                        TransporteDB.getInstance(this@AsistenciaManBajarActivity)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.iRutaDAO.cambiaEstatusRuta(
                                            estatus = "2",
                                            offline = 0,
                                            idRuta = idRuta.toString()
                                        )
                                    }
                                    Intent(
                                        this@AsistenciaManBajarActivity,
                                        SeleccionRutaActivity::class.java
                                    ).also {
                                        startActivity(it)
                                    }
                                },
                                    onError = {})
                            }

                        }else{
                            Toast.makeText(this@AsistenciaManBajarActivity,"No se puede cerrar todavía",
                                Toast.LENGTH_LONG).show()
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

        ConfirmarProcesoCompletoDialog(
            isOpen = showTodosDialog,
            mensaje = "¿Deseas efectuar la bajada completa?",
            onDismiss = { showTodosDialog = false },
            onAccept = {
                if(hayConexion()) {
                    val db = TransporteDB.getInstance(this@AsistenciaManBajarActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                            db.iAsistenciaDAO.bajanTodosMan(idRuta.toString(), 1, getCurrentTime())
                    }
                    asistenciaViewModel.bajarTodos(idRuta.toString(),
                        onSuccess = { getAsistencia() },
                        onError = {}

                    )
                }else{
                    val db = TransporteDB.getInstance(this@AsistenciaManBajarActivity)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.bajanTodosMan(idRuta.toString(), -1, getCurrentTime())
                    }
                    Thread.sleep(1000)
                    getAsistencia()
                }
                showTodosDialog = false
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
        onDownClick:() -> Unit,
        onMessageClick: () -> Unit
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

                IconButton(onClick = { onDownClick() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Down",
                        tint = Color.White,
                        modifier = Modifier.rotate(270f)
                    )
                }

                IconButton(onClick = { onMessageClick() }) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "Message",
                        tint = Color.White
                    )
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



    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }



}


