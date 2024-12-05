package sv.com.chmd.transporte.composables

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

object InternetStateDialog {
    val isDialogVisible = mutableStateOf(false)
}

@Composable
fun InternetDialog(onDismiss: () -> Unit) {
    if (InternetStateDialog.isDialogVisible.value) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Aceptar")
                }
            },
            dismissButton = null,
            title = {
                Text("Sin conexión a internet")
            },
            text = {
                Text("Por favor, verifica tu conexión a internet.")
            }
        )
    }
}