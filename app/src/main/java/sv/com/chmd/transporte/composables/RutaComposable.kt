package sv.com.chmd.transporte.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sv.com.chmd.transporte.R
import sv.com.chmd.transporte.util.nunitoRegular

@Composable
@Preview(showBackground = true)
fun RutaItem(
    rutaText: String?="Tecamachalco",
    tipoMovText: String?="S",
    tipoRutaImage: Int?=R.drawable.am,
    modifier: Modifier = Modifier,
    onClick: () -> Unit={},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colorResource(id = R.color.textoMasOscuro))
            .padding(8.dp)
            .clickable(onClick = onClick)



             // Asegúrate de que bg_item_ruta sea un drawable
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = rutaText!!,
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp)
                    .weight(1f), // Ocupa el espacio restante
                color = colorResource(R.color.white),
                maxLines = 1,
                fontSize = 12.sp,
                fontFamily = nunitoRegular,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = tipoMovText!!,
                modifier = Modifier
                    .padding(end = 6.dp),
                color = colorResource(R.color.white),
                fontSize = 12.sp // Asegúrate de importar el tamaño adecuado
            )

            Image(
                painter = painterResource(id = tipoRutaImage!!), // Utiliza el recurso de la imagen correspondiente
                contentDescription = null, // Proporciona una descripción si es necesario
                modifier = Modifier
                    .size(48.dp)
                    .padding(6.dp)
            )
        }
    }
}
