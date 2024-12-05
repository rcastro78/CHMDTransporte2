package sv.com.chmd.transporte.util

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import sv.com.chmd.transporte.R

fun setAlumnoTextStyle(context: Context): TextStyle {
    val blueColor = ContextCompat.getColor(context, R.color.textoMasOscuro)

    return TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = Color(blueColor) // Convierte el int en Color
    )
}
