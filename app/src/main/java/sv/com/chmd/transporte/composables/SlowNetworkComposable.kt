package sv.com.chmd.transporte.composables

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.ui.res.painterResource
import sv.com.chmd.transporte.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import sv.com.chmd.transporte.util.nunitoBold


@Composable
    @Preview(showBackground = true)
    fun SlowNetworkScreen(c: Context?=null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.slow),
                contentDescription = "Red lenta",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Tu conexión es muy lenta",
                color = colorResource(R.color.azulColegio),
                maxLines = 1,
                fontFamily = nunitoBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { (c as Activity).onBackPressed() }) {
                Text("Volver",
                    color = colorResource(R.color.white),
                    maxLines = 1,
                    fontFamily = nunitoBold,)
            }
        }
    }

