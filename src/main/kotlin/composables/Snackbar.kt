package composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun infoSnackbar(message: String, visible : Boolean) {
    val snackbarVisible by remember { mutableStateOf(visible) }

    if (snackbarVisible) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(message)
        }
    }
}

