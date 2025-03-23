package sv.com.chmd.transporte.composables

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import sv.com.chmd.transporte.R
import sv.com.chmd.transporte.model.AlumnoRutaDiferenteItem
import sv.com.chmd.transporte.util.nunitoBold
import sv.com.chmd.transporte.util.nunitoRegular
import sv.com.chmd.transporte.util.setAlumnoTextStyle


@Composable
@Preview(showBackground = true)
fun AlumnoOtraRutaComposable(idAlumno: String?="0", imageUrl:String?="", nombre:String?="Rafael David Castro",
                             ruta:String?="Paseo de la Reforma", camion:String?="5"){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .background(Color.Transparent) // Fondo general transparente
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp) // Altura de la línea
                .background(colorResource(id = R.color.textoMasOscuro)) // Color azul
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(0.5f)){
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterVertically)

                     // Llama al callback con idAlumno
            ) {
                AsyncImage(
                    modifier = Modifier
                        .height(64.dp)
                        .width(64.dp)
                        .clip(CircleShape),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "",
                    alignment = Alignment.Center,
                    placeholder = painterResource(id = R.drawable.usuario),
                    error = painterResource(id = R.drawable.usuario),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
            )
            {
                Text(
                    text = nombre ?: "",
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp),
                    color = colorResource(R.color.azulColegio),
                    maxLines = 1,
                    fontFamily = nunitoBold,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(0.5f)
        ){

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterVertically)

                // Llama al callback con idAlumno
            ) {
                Image(
                    modifier = Modifier.padding(24.dp),
                    painter = painterResource(id = R.drawable.bus),
                    contentDescription = "camion")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
            )
            {
                Row{
                    Text(
                        text = ruta ?: "",
                        modifier = Modifier
                            .padding(start = 14.dp, end = 12.dp, bottom = 12.dp),
                        color = colorResource(R.color.textoClaro),
                        maxLines = 1,
                        fontFamily = nunitoBold,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row{
                    Text(
                        text = camion!!,
                        modifier = Modifier
                            .padding(start = 14.dp, end = 12.dp, bottom = 12.dp),
                        color = colorResource(R.color.textoClaro),
                        maxLines = 1,
                        fontFamily = nunitoBold,
                        overflow = TextOverflow.Ellipsis
                    )
                }



            }




        }
    }
}



@Composable
@Preview(showBackground = true)
fun AlumnoAsistenciaManComposable(
    idAlumno: String? = "0",
    idRuta:String?="0",
    hora: String? = "08:00",
    nombre: String? = "Juan Perez",
    parada: String? = "Tecamachalco",
    direccion: String? = "Av. 12 de octubre",
    imageUrl: String? = "",
    ascenso:String?="0",
    descenso:String?="0",
    asistencia:String?="0",
    modifier: Modifier = Modifier,
    onImageClick: (String, String) -> Unit = { _: String, _: String -> }, // Pasar idAlumno como parámetro
    onInasistenciaClick: (String, String) -> Unit = { _: String, _: String -> } // Pasar idAlumno como parámetro
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
            .height(232.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {

        /*
        * if(items.asistencia=="0" && items.ascenso=="0" && items.descenso=="0"){
            holder.imgFotoEstudiante.isEnabled=false
            holder.llContenedor.setBackgroundColor(Color.parseColor("#ff4122"))
            holder.btnInasistencia.visibility=View.GONE
            //holder.lblInasistencia.text="Inasistencia"
            holder.btnInasistencia.isEnabled = false

        }

        //Entró caminando (color aqua)
        if(items.asistencia=="2"){
            holder.imgFotoEstudiante.isEnabled=false
            holder.llContenedor.setBackgroundColor(Color.parseColor("#7adfb5"))
            holder.lblInasistencia.text="Entró caminando"
            holder.btnInasistencia.setBackgroundColor(Color.parseColor("#7adfb5"))
            holder.btnInasistencia.isEnabled = false

        }
        * */

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        asistencia == "2" -> colorResource(R.color.aqua)
                        asistencia == "0" -> colorResource(R.color.rojoInasistencia)
                        ascenso == "0" && descenso == "0" -> colorResource(R.color.white)
                        ascenso == "1" && descenso == "0" -> colorResource(R.color.amarillo)
                        ascenso == "2" && descenso == "2" -> colorResource(R.color.rosado)
                        ascenso == "1" && descenso == "1" -> colorResource(R.color.verde)


                        else -> colorResource(android.R.color.transparent) // Color por defecto si no coincide
                    }
                )
        ) {



            Row(
                modifier = Modifier
                    .fillMaxSize()

                    .weight(0.6f)

            ) {

                val modifier = if (asistencia!!.toInt() < 2) {
                    Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterVertically)
                        .padding(start = 12.dp)
                        .clickable {
                            onImageClick(idRuta ?: "0", idAlumno ?: "0")
                        } // Llama al callback con idAlumno
                } else {
                    Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterVertically)
                        .padding(start = 12.dp)
                }
                Box(
                    modifier = modifier // Llama al callback con idAlumno
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .height(120.dp)
                            .width(120.dp)
                            .clip(CircleShape),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "",
                        alignment = Alignment.Center,
                        placeholder = painterResource(id = R.drawable.usuario),
                        error = painterResource(id = R.drawable.usuario),
                        contentScale = ContentScale.Crop
                    )



                }
                // Datos del alumno
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.2f)
                        ) {
                            Text(
                                text = parada ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = hora ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.8f)
                        ) {
                            Text(
                                text = direccion ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                                    .weight(1f),
                                style = setAlumnoTextStyle(LocalContext.current),
                                color = colorResource(R.color.azulColegio),
                                fontFamily = nunitoBold
                            )
                        }
                    }
                }
            }

            //Botones de accion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
                    .padding(6.dp)


            ) {
                Column(modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()

                    .background(colorResource(id = R.color.amarillo))){
                }

                Column(modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                    ){
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.weight(1f)) // Deja espacio a la izquierda
                        Image(
                            painter = painterResource(id = R.drawable.waze),
                            contentDescription = null,
                            modifier = Modifier.wrapContentWidth()
                                .clickable {
                                    try {
                                        // Intento de abrir Waze con el deep link para Hawaii
                                        val wazeUrl = "https://waze.com/ul?q="+direccion!!.replace(" ","%20")+"&navigate=yes"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wazeUrl))
                                        context.startActivity(intent)
                                    } catch (ex: ActivityNotFoundException) {
                                        // Si Waze no está instalado, abrir en Google Play
                                        val playStoreUrl = "market://details?id=com.waze"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
                                        context.startActivity(intent)
                                    }
                                },

                        )
                    }

                }
            }
            //Fin botones de accion

            // Fila inferior (nombre e inasistencia)
            Row(
                modifier = Modifier
                    .fillMaxSize()

                    .weight(0.2f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.55f)
                        .padding(start = 12.dp, end = 12.dp)


                ) {
                    Text(
                        text = nombre ?: "",
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(R.color.azulColegio),
                        style = setAlumnoTextStyle(LocalContext.current),
                        fontFamily = nunitoBold
                    )

                }
                if(asistencia !="0") {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.45f)
                            .clickable {
                                onInasistenciaClick(
                                    idRuta ?: "0",
                                    idAlumno ?: "0"
                                )
                            } // Llama al callback con idAlumno
                            .background(colorResource(id = R.color.rojoInasistencia))
                    ) {
                        Text(
                            text = "Inasistencia",
                            modifier = Modifier
                                .padding(start = 12.dp, end = 12.dp)
                                .align(Alignment.Center),
                            color = colorResource(R.color.white),
                            maxLines = 1,
                            fontFamily = nunitoBold,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun AlumnoAsistenciaTarComposable(
    idAlumno: String? = "0",
    idRuta:String?="0",
    hora: String? = "08:00",
    nombre: String? = "Juan Perez",
    orden_out: String? = "1",
    direccion: String? = "Av. 13 de octubre",
    imageUrl: String? = "",
    ascenso_t:String?="0",
    descenso_t:String?="0",
    asistencia:String?="0",
    salida:String?="0",
    modifier: Modifier = Modifier,
    onImageClick: (String, String) -> Unit = { _: String, _: String -> }, // Pasar idAlumno como parámetro
    onInasistenciaClick: (String, String) -> Unit = { _: String, _: String -> } // Pasar idAlumno como parámetro
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
            .height(232.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        ascenso_t == "0" && descenso_t == "0" && salida == "3" -> colorResource(R.color.salio)
                        ascenso_t == "0" && descenso_t == "0" && salida == "2" -> colorResource(R.color.salida)
                        orden_out!!.toInt() > 900 && ascenso_t == "0" -> colorResource(R.color.rosado)
                        orden_out!!.toInt() > 900 && ascenso_t == "1" -> colorResource(R.color.amarillo)
                        ascenso_t == "0" && descenso_t == "0" -> colorResource(R.color.white)
                        ascenso_t == "1" && descenso_t == "0" -> colorResource(R.color.amarillo)
                        ascenso_t == "2" && descenso_t == "2" -> colorResource(R.color.rosado)
                        ascenso_t == "1" && descenso_t == "1" -> colorResource(R.color.verde)

                        else -> colorResource(android.R.color.transparent) // Color por defecto si no coincide
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.6f)
            ) {

                val modifier = if (salida!!.toInt() < 2) {
                    Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterVertically)
                        .padding(start = 12.dp)
                        .clickable {
                            onImageClick(idRuta ?: "0", idAlumno ?: "0")
                        } // Llama al callback con idAlumno
                } else {
                    Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterVertically)
                        .padding(start = 12.dp)
                }

                Box(
                    modifier = modifier
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .height(120.dp)
                            .width(120.dp)
                            .clip(CircleShape),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "",
                        alignment = Alignment.Center,
                        placeholder = painterResource(id = R.drawable.usuario),
                        error = painterResource(id = R.drawable.usuario),
                        contentScale = ContentScale.Crop
                    )
                    if(salida=="3")
                    Image(painterResource(id =R.drawable.footstep_verde), contentDescription = "huellas"
                    )

                }
                // Datos del alumno
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.2f)
                        ) {
                            Text(
                                text = orden_out ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = hora ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.8f)
                        ) {
                            Text(
                                text = direccion ?: "",
                                modifier = Modifier

                                    .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                style = setAlumnoTextStyle(LocalContext.current),
                                fontFamily = nunitoBold
                            )
                        }
                    }
                }
            }

            // Fila inferior (nombre e inasistencia)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.25f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.55f)
                        .padding(start = 12.dp, end = 12.dp)
                ) {
                    Text(
                        text = nombre ?: "",
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(R.color.azulColegio),
                        style = setAlumnoTextStyle(LocalContext.current),
                        fontFamily = nunitoBold
                    )
                }
                if (salida == "3") {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.45f)
                    ) {
                        Text(
                            text = "Por Salir",
                            modifier = Modifier.align(Alignment.Center),
                            color = colorResource(R.color.azulColegio),
                            style = setAlumnoTextStyle(LocalContext.current),
                            fontFamily = nunitoBold
                        )
                    }
                } else if (salida == "2") {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.45f)
                            .background(colorResource(id = R.color.salida)) // Color de fondo personalizado
                    ) {
                        Text(
                            text = "Salió",
                            modifier = Modifier.align(Alignment.Center),
                            color = colorResource(R.color.black),
                            style = setAlumnoTextStyle(LocalContext.current),
                            fontFamily = nunitoBold
                        )
                    }
                } else {
                    if (salida!!.toInt() < 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.45f)
                                .clickable {
                                    onInasistenciaClick(
                                        idRuta ?: "0",
                                        idAlumno ?: "0"
                                    )
                                }
                                .background(colorResource(id = R.color.rojoInasistencia))
                        ) {
                            Text(
                                text = "Inasistencia",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .align(Alignment.Center),
                                color = colorResource(R.color.white),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun AlumnoAsistenciaTarBajarComposable(
    idAlumno: String? = "0",
    idRuta:String?="0",
    hora: String? = "08:00",
    nombre: String? = "Juan Perez",
    orden_out: String? = "1",
    direccion: String? = "Av. 12 de octubre",
    imageUrl: String? = "",
    ascenso_t:String?="0",
    descenso_t:String?="0",
    asistencia:String?="0",
    salida:String?="0",
    ascenso:String?="0",
    descenso: String?="0",
    modifier: Modifier = Modifier,
    onImageClick: (String, String) -> Unit = { _: String, _: String -> }, // Pasar idAlumno como parámetro
    onInasistenciaClick: (String, String) -> Unit = { _: String, _: String -> } // Pasar idAlumno como parámetro
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
            .height(232.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {

        /*
        * if(items.asistencia=="0" && items.ascenso=="0" && items.descenso=="0"){
            holder.imgFotoEstudiante.isEnabled=false
            holder.llContenedor.setBackgroundColor(Color.parseColor("#ff4122"))
            holder.btnInasistencia.visibility=View.GONE
            //holder.lblInasistencia.text="Inasistencia"
            holder.btnInasistencia.isEnabled = false

        }

        //Entró caminando (color aqua)
        if(items.asistencia=="2"){
            holder.imgFotoEstudiante.isEnabled=false
            holder.llContenedor.setBackgroundColor(Color.parseColor("#7adfb5"))
            holder.lblInasistencia.text="Entró caminando"
            holder.btnInasistencia.setBackgroundColor(Color.parseColor("#7adfb5"))
            holder.btnInasistencia.isEnabled = false

        }
        * */

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {

                        ascenso_t == "0" && descenso_t == "0" && salida == "3" -> colorResource(R.color.salio)
                        ascenso_t == "0" && descenso_t == "0" && salida == "2" -> colorResource(R.color.salida)
                        orden_out!!.toInt() > 900 && ascenso_t == "0" -> colorResource(R.color.rosado)
                        orden_out!!.toInt() > 900 && ascenso_t == "1" && descenso_t == "1" -> colorResource(R.color.verde)
                        ascenso_t == "0" && descenso_t == "0" -> colorResource(R.color.white)
                        ascenso_t == "1" && descenso_t == "0" -> colorResource(R.color.amarillo)
                        ascenso_t == "2" && descenso_t == "2" -> colorResource(R.color.rojoInasistencia)
                        ascenso_t == "1" && descenso_t == "1" -> colorResource(R.color.verde)

                        else -> colorResource(android.R.color.transparent) // Color por defecto si no coincide
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.6f)
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterVertically)
                        .padding(start = 12.dp)
                        .clickable {
                            onImageClick(idRuta ?: "0", idAlumno ?: "0")
                        } // Llama al callback con idAlumno
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .height(120.dp)
                            .width(120.dp)
                            .clip(CircleShape),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "",
                        alignment = Alignment.Center,
                        placeholder = painterResource(id = R.drawable.usuario),
                        error = painterResource(id = R.drawable.usuario),
                        contentScale = ContentScale.Crop
                    )
                    if(salida=="3")
                        Image(painterResource(id =R.drawable.footstep_verde), contentDescription = "huellas"
                        )

                }
                // Datos del alumno
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.2f)
                        ) {
                            Text(
                                text = orden_out ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = hora ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.8f)
                        ) {
                            Text(
                                text = direccion ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                style = setAlumnoTextStyle(LocalContext.current),
                                fontFamily = nunitoBold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
                    .padding(6.dp)


            ) {
                Column(modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()

                    .background(colorResource(id = R.color.amarillo))){
                }

                Column(modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                ){
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.weight(1f)) // Deja espacio a la izquierda
                        Image(
                            painter = painterResource(id = R.drawable.waze),
                            contentDescription = null,
                            modifier = Modifier.wrapContentWidth()
                                .clickable {
                                    try {
                                        // Intento de abrir Waze con el deep link para Hawaii
                                        val wazeUrl = "https://waze.com/ul?q="+direccion!!.replace(" ","%20")+"&navigate=yes"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wazeUrl))
                                        context.startActivity(intent)
                                    } catch (ex: ActivityNotFoundException) {
                                        // Si Waze no está instalado, abrir en Google Play
                                        val playStoreUrl = "market://details?id=com.waze"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
                                        context.startActivity(intent)
                                    }
                                },

                            )
                    }

                }
            }
            //Fin botones de accion

            // Fila inferior (nombre e inasistencia)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.25f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.55f)
                        .padding(start = 12.dp, end = 12.dp)
                ) {
                    Text(
                        text = nombre ?: "",
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(R.color.azulColegio),
                        style = setAlumnoTextStyle(LocalContext.current),
                        fontFamily = nunitoBold
                    )
                }

                if (salida == "3") {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.55f)
                            .padding(start = 12.dp, end = 12.dp)
                    ) {
                        Text(
                            text = "Por Salir",
                            modifier = Modifier.align(Alignment.Center),
                            color = colorResource(R.color.azulColegio),
                            style = setAlumnoTextStyle(LocalContext.current),
                            fontFamily = nunitoBold
                        )
                    }
                }



            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun AlumnoAsistenciaManBajarComposable(
    idAlumno: String? = "0",
    idRuta:String?="0",
    hora: String? = "08:00",
    nombre: String? = "Juan Perez",
    parada: String? = "Tecamachalco",
    direccion: String? = "Av. 12 de octubre",
    imageUrl: String? = "",
    ascenso:String?="0",
    descenso:String?="0",
    asistencia:String?="0",
    modifier: Modifier = Modifier,
    onImageClick: (String, String) -> Unit = { _: String, _: String -> }, // Pasar idAlumno como parámetro

) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
            .height(232.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {

        /*
        * if(items.asistencia=="0" && items.ascenso=="0" && items.descenso=="0"){
            holder.imgFotoEstudiante.isEnabled=false
            holder.llContenedor.setBackgroundColor(Color.parseColor("#ff4122"))
            holder.btnInasistencia.visibility=View.GONE
            //holder.lblInasistencia.text="Inasistencia"
            holder.btnInasistencia.isEnabled = false

        }

        //Entró caminando (color aqua)
        if(items.asistencia=="2"){
            holder.imgFotoEstudiante.isEnabled=false
            holder.llContenedor.setBackgroundColor(Color.parseColor("#7adfb5"))
            holder.lblInasistencia.text="Entró caminando"
            holder.btnInasistencia.setBackgroundColor(Color.parseColor("#7adfb5"))
            holder.btnInasistencia.isEnabled = false

        }
        * */

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when {
                        ascenso == "0" && descenso == "0" -> colorResource(R.color.white)
                        ascenso == "1" && descenso == "0" -> colorResource(R.color.amarillo)
                        ascenso == "2" && descenso == "2" -> colorResource(R.color.rosado)
                        ascenso == "1" && descenso == "1" -> colorResource(R.color.verde)
                        asistencia == "2" -> colorResource(R.color.aqua)
                        asistencia == "0" && ascenso == "0" && descenso == "0" -> colorResource(R.color.inasistencia)
                        else -> colorResource(android.R.color.transparent) // Color por defecto si no coincide
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.6f)
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterVertically)
                        .padding(start = 12.dp)
                        .clickable { onImageClick(idRuta ?: "0", idAlumno ?: "0") } // Llama al callback con idAlumno
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .height(120.dp)
                            .width(120.dp)
                            .clip(CircleShape),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "",
                        alignment = Alignment.Center,
                        placeholder = painterResource(id = R.drawable.usuario),
                        error = painterResource(id = R.drawable.usuario),
                        contentScale = ContentScale.Crop
                    )
                }
                // Datos del alumno
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.2f)
                        ) {
                            Text(
                                text = parada ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = hora ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                maxLines = 1,
                                fontFamily = nunitoBold,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.8f)
                        ) {
                            Text(
                                text = direccion ?: "",
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                                    .weight(1f),
                                color = colorResource(R.color.azulColegio),
                                style = setAlumnoTextStyle(LocalContext.current),
                                fontFamily = nunitoBold
                            )
                        }
                    }
                }
            }

            // Fila inferior (nombre e inasistencia)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.25f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(132.dp)
                        .padding(start = 12.dp, end = 12.dp)
                ) {
                    Text(
                        text = nombre ?: "",
                        modifier = Modifier.align(Alignment.Center),
                        style = setAlumnoTextStyle(LocalContext.current),
                        color = colorResource(R.color.azulColegio),
                        fontFamily = nunitoBold

                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()

                ) {

                }
            }
        }
    }
}



//Barra superior
@Preview(showBackground = true)
@Composable
fun AsistenciasComposable(
    ascensos: String = "0",
    total: String = "0",
    inasistencias: String = "0"
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(Color.Gray) // Fondo general transparente
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)

        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(0.45f)
                .background(colorResource(id = R.color.textoMasOscuro))){

                Text(modifier = Modifier.align(Alignment.Center),
                    text = "Ascensos/Total",

                    color = colorResource(R.color.white),
                    fontFamily = nunitoBold
                )
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .weight(0.65f)
                .background(Color.LightGray)){

                Text(modifier = Modifier.align(Alignment.Center),
                    text = "$ascensos/$total",
                    color = colorResource(R.color.textoMasOscuro),
                    fontFamily = nunitoBold
                )

            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)

        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(0.45f)
                .background(colorResource(id = R.color.textoMasOscuro))){

                Text(modifier = Modifier.align(Alignment.Center),
                    text = "Inasistencias",
                    color = colorResource(R.color.white),
                    fontFamily = nunitoBold
                )

            }

            Box(modifier = Modifier
                .fillMaxSize()
                .weight(0.65f)
                .background(Color.LightGray)){

                Text(modifier = Modifier.align(Alignment.Center),
                    text = inasistencias,
                    color = colorResource(R.color.textoMasOscuro),
                    fontFamily = nunitoBold
                )

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AsistenciasDescensoComposable(
    descensos: String = "0",
    total: String = "0",
    inasistencias: String = "0"
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(Color.Gray) // Fondo general transparente
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)

        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(0.45f)
                .background(colorResource(id = R.color.textoMasOscuro))){

                Text(modifier = Modifier.align(Alignment.Center),
                    text = "Descensos/Total",
                    color = colorResource(R.color.white),
                    fontFamily = nunitoBold
                )
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .weight(0.55f)
                .background(Color.LightGray)){

                Text(modifier = Modifier.align(Alignment.Center),
                    text = "$descensos/$total",
                    color = colorResource(R.color.textoMasOscuro),
                    fontFamily = nunitoBold
                )

            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.5f)

        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(0.45f)
                .background(colorResource(id = R.color.textoMasOscuro))){

                Text(modifier = Modifier.align(Alignment.Center),
                    text = "Inasistencias",
                    color = colorResource(R.color.white),
                    fontFamily = nunitoBold
                )

            }

            Box(modifier = Modifier
                .fillMaxSize()
                .weight(0.55f)
                .background(Color.LightGray)){

                Text(modifier = Modifier.align(Alignment.Center),
                    text = inasistencias,
                    color = colorResource(R.color.textoMasOscuro),
                    fontFamily = nunitoBold
                )

            }
        }
    }


}

@Composable
@Preview(showBackground = true)
fun SearchBarAlumnos(
    searchText: String="",
    onSearchTextChange: (String) -> Unit={}
) {
    TextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        placeholder = { Text("Buscar por nombre") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        singleLine = true
    )
}


@Composable
fun ConfirmarInasistenciaDialog(
    isOpen: Boolean,
    nombre: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Inasistencia",
                    color = colorResource(R.color.textoMasOscuro),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            },
            text = {
                Text(
                    text = "¿Deseas marcar la inasistencia de $nombre?",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAccept()
                        onDismiss() // Cierra el diálogo después de aceptar
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
fun ConfirmarProcesoCompletoDialog(
    isOpen: Boolean,
    mensaje:String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Procesar...",
                    color = colorResource(R.color.textoMasOscuro),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            },
            text = {
                Text(
                    text = mensaje,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAccept()
                        onDismiss() // Cierra el diálogo después de aceptar
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

