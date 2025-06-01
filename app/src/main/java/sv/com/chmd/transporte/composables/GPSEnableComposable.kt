package sv.com.chmd.transporte.composables

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sv.com.chmd.transporte.R
import sv.com.chmd.transporte.util.nunitoBold

@Composable
@Preview(showBackground = true)
fun GpsDisabledScreen(c: Context?=null) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.location_off),
            contentDescription = "GPS desactivado",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = "El GPS está desactivado",
            color = colorResource(R.color.azulColegio),
            maxLines = 1,
            fontFamily = nunitoBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                c?.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        ) {
            Text(
                text = "Activar GPS",
                color = colorResource(R.color.white),
                maxLines = 1,
                fontFamily = nunitoBold
            )
        }
    }
}
